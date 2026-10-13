package br.com.movesaude.api.service;

import br.com.movesaude.api.domain.model.Passageiro;
import br.com.movesaude.api.domain.model.PlanoConvenio;
import br.com.movesaude.api.repository.CorridaRepository;
import br.com.movesaude.api.repository.PassageiroRepository;
import br.com.movesaude.api.repository.PlanoConvenioRepository;
import br.com.movesaude.api.web.dto.PassageiroDtos.CreditosResponse;
import br.com.movesaude.api.web.dto.PassageiroDtos.PassageiroRequest;
import br.com.movesaude.api.web.dto.PassageiroDtos.PassageiroResponse;
import br.com.movesaude.api.web.dto.PassageiroDtos.PlanoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PassageiroService {

    private final PassageiroRepository passageiros;
    private final PlanoConvenioRepository planos;
    private final CorridaRepository corridas;

    public PassageiroService(PassageiroRepository passageiros, PlanoConvenioRepository planos,
                             CorridaRepository corridas) {
        this.passageiros = passageiros;
        this.planos = planos;
        this.corridas = corridas;
    }

    public List<PassageiroResponse> listar() {
        return passageiros.findAllByOrderByIdAsc().stream().map(PassageiroResponse::de).toList();
    }

    public PassageiroResponse buscar(Long id) {
        return PassageiroResponse.de(entidade(id));
    }

    @Transactional
    public PassageiroResponse criar(PassageiroRequest req) {
        PlanoConvenio plano = null;
        if (req.planoId() != null && !req.planoId().isBlank()) {
            plano = planos.findById(req.planoId()).orElseThrow(() -> new NaoEncontradoException("Plano", req.planoId()));
            if (req.carteirinha() == null || req.carteirinha().isBlank()) {
                throw new RegraNegocioException("Informe a carteirinha para vincular o convênio");
            }
        }
        Passageiro p = new Passageiro(req.nome(), req.necessidades().paraEntidade(), plano, req.carteirinha());
        return PassageiroResponse.de(passageiros.save(p));
    }

    public List<PlanoResponse> planos() {
        return planos.findAllByOrderByOperadoraAscNomeAsc().stream().map(PlanoResponse::de).toList();
    }

    public CreditosResponse creditos(Long id, YearMonth mes) {
        Passageiro p = entidade(id);
        PlanoConvenio plano = p.getPlano();
        if (plano == null) {
            return new CreditosResponse(id, mes.toString(), null, 0, 0, 0);
        }
        int usados = creditosUsados(id, mes);
        return new CreditosResponse(id, mes.toString(), plano.getOperadora() + " · " + plano.getNome(),
                plano.getCreditosMensais(), usados, Math.max(0, plano.getCreditosMensais() - usados));
    }

    int creditosUsados(Long passageiroId, YearMonth mes) {
        return corridas.creditosUsados(passageiroId, mes.atDay(1), mes.atEndOfMonth());
    }

    Passageiro entidade(Long id) {
        return passageiros.findById(id).orElseThrow(() -> new NaoEncontradoException("Passageiro", id));
    }
}
