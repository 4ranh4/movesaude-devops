package br.com.movesaude.api.service;

import br.com.movesaude.api.config.AppProperties;
import br.com.movesaude.api.domain.StatusCorrida;
import br.com.movesaude.api.domain.model.Corrida;
import br.com.movesaude.api.domain.model.Passageiro;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.regras.Avaliacao;
import br.com.movesaude.api.domain.regras.CalculadoraCobertura;
import br.com.movesaude.api.domain.regras.CalculadoraPreco;
import br.com.movesaude.api.domain.regras.Cobertura;
import br.com.movesaude.api.domain.regras.Matching;
import br.com.movesaude.api.domain.regras.Solicitacao;
import br.com.movesaude.api.domain.regras.Triagem;
import br.com.movesaude.api.repository.CorridaRepository;
import br.com.movesaude.api.repository.PrestadorRepository;
import br.com.movesaude.api.web.dto.CorridaDtos.CorridaRequest;
import br.com.movesaude.api.web.dto.CorridaDtos.CorridaResponse;
import br.com.movesaude.api.web.dto.CorridaDtos.DescartadoResponse;
import br.com.movesaude.api.web.dto.CorridaDtos.OpcaoResponse;
import br.com.movesaude.api.web.dto.CorridaDtos.OpcoesResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CorridaService {

    static final int MAXIMO_OPCOES = 3;

    private final CorridaRepository corridas;
    private final PrestadorRepository prestadores;
    private final PassageiroService passageiroService;
    private final PrestadorService prestadorService;
    private final AppProperties props;

    public CorridaService(CorridaRepository corridas, PrestadorRepository prestadores,
                          PassageiroService passageiroService, PrestadorService prestadorService,
                          AppProperties props) {
        this.corridas = corridas;
        this.prestadores = prestadores;
        this.passageiroService = passageiroService;
        this.prestadorService = prestadorService;
        this.props = props;
    }

    @Transactional
    public CorridaResponse solicitar(CorridaRequest req) {
        Passageiro passageiro = passageiroService.entidade(req.passageiroId());
        Corrida corrida = new Corrida(passageiro, req.origem(), req.destino(), req.distanciaKm(), req.data(),
                req.hora(), req.idaEVolta(), req.finalidade(), Triagem.modalidadeIndicada(passageiro.getNecessidades()));
        return CorridaResponse.de(corridas.save(corrida));
    }

    public List<CorridaResponse> listar() {
        return corridas.findAllByOrderByIdDesc().stream().map(CorridaResponse::de).toList();
    }

    public CorridaResponse buscar(Long id) {
        return CorridaResponse.de(entidade(id));
    }

    /** Opções sugeridas pela central (até 3) e prestadores descartados, com o motivo. */
    public OpcoesResponse opcoes(Long id) {
        Corrida corrida = entidade(id);
        exigirStatus(corrida, StatusCorrida.EM_TRIAGEM);

        List<Avaliacao> avaliacoes = Matching.avaliarTodos(prestadores.findAllByOrderByIdAsc(), solicitacao(corrida));
        List<Avaliacao> sugeridas = Matching.sugerirOpcoes(avaliacoes, MAXIMO_OPCOES);
        int usados = passageiroService.creditosUsados(corrida.getPassageiro().getId(), YearMonth.from(corrida.getData()));

        List<OpcaoResponse> opcoes = new ArrayList<>();
        for (int i = 0; i < sugeridas.size(); i++) {
            Avaliacao a = sugeridas.get(i);
            Cobertura cobertura = cobertura(corrida, a, usados);
            opcoes.add(new OpcaoResponse(a.prestador().getId(), a.prestador().getNomeFantasia(),
                    a.prestador().getAvaliacao(), a.modalidade(), a.veiculo().getId(),
                    a.veiculo().getTipo().descricao() + " · " + a.veiculo().getModelo(), a.veiculo().getCombustivel(),
                    a.horario(), a.horarioExato(), a.cotacao().preco(), a.cotacao().detalhamento(), cobertura, i == 0));
        }
        List<DescartadoResponse> descartados = avaliacoes.stream()
                .filter(a -> !a.compativel())
                .map(a -> new DescartadoResponse(a.prestador().getId(), a.prestador().getNomeFantasia(), a.motivos()))
                .toList();

        return new OpcoesResponse(corrida.getId(), corrida.getModalidadeIndicada(),
                Triagem.motivo(corrida.getPassageiro().getNecessidades()), opcoes, descartados);
    }

    @Transactional
    public CorridaResponse confirmar(Long id, Long prestadorId) {
        Corrida corrida = entidade(id);
        exigirStatus(corrida, StatusCorrida.EM_TRIAGEM);
        Prestador prestador = prestadorService.entidade(prestadorId);

        Avaliacao a = Matching.avaliar(prestador, solicitacao(corrida));
        if (!a.compativel()) {
            throw new RegraNegocioException("Prestador incompatível com a corrida: " + String.join("; ", a.motivos()));
        }
        int usados = passageiroService.creditosUsados(corrida.getPassageiro().getId(), YearMonth.from(corrida.getData()));
        Cobertura cobertura = cobertura(corrida, a, usados);
        corrida.confirmar(prestador, a.veiculo(), a.modalidade(), a.horario(), a.cotacao().preco(), cobertura,
                CalculadoraPreco.comissao(a.cotacao().preco(), props.comissao()));
        return CorridaResponse.de(corrida);
    }

    @Transactional
    public CorridaResponse atualizarStatus(Long id, StatusCorrida novo) {
        Corrida corrida = entidade(id);
        if (!corrida.getStatus().podeIrPara(novo)) {
            throw new RegraNegocioException("Transição inválida: " + corrida.getStatus() + " → " + novo
                    + ". Permitidas: " + corrida.getStatus().proximos());
        }
        corrida.mudarStatus(novo);
        if (novo == StatusCorrida.CONCLUIDA) {
            corrida.getPrestador().registrarCorridaConcluida();
        }
        return CorridaResponse.de(corrida);
    }

    private Cobertura cobertura(Corrida corrida, Avaliacao a, int creditosUsados) {
        return CalculadoraCobertura.calcular(corrida.getPassageiro().getPlano(), corrida.getFinalidade(),
                a.modalidade(), a.cotacao().trechos(), a.cotacao().preco(), creditosUsados);
    }

    private static Solicitacao solicitacao(Corrida c) {
        return new Solicitacao(c.getPassageiro().getNecessidades(), c.getHora(), c.isIdaEVolta(), c.getDistanciaKm());
    }

    private static void exigirStatus(Corrida corrida, StatusCorrida esperado) {
        if (corrida.getStatus() != esperado) {
            throw new RegraNegocioException("A corrida está " + corrida.getStatus() + "; esta ação exige " + esperado);
        }
    }

    private Corrida entidade(Long id) {
        return corridas.findById(id).orElseThrow(() -> new NaoEncontradoException("Corrida", id));
    }
}
