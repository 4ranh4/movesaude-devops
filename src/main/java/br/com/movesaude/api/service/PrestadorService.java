package br.com.movesaude.api.service;

import br.com.movesaude.api.domain.StatusVerificacao;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Tarifa;
import br.com.movesaude.api.domain.model.Veiculo;
import br.com.movesaude.api.repository.PrestadorRepository;
import br.com.movesaude.api.web.dto.PrestadorDtos.PrestadorRequest;
import br.com.movesaude.api.web.dto.PrestadorDtos.PrestadorResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PrestadorService {

    private final PrestadorRepository repository;

    public PrestadorService(PrestadorRepository repository) {
        this.repository = repository;
    }

    public List<PrestadorResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(PrestadorResponse::de).toList();
    }

    public PrestadorResponse buscar(Long id) {
        return PrestadorResponse.de(entidade(id));
    }

    /** Novos prestadores entram "em análise": só recebem corridas depois da verificação de documentos. */
    @Transactional
    public PrestadorResponse criar(PrestadorRequest req) {
        if (!req.horaFim().isAfter(req.horaInicio())) {
            throw new RegraNegocioException("horaFim deve ser depois de horaInicio");
        }
        if (repository.existsByCnpj(req.cnpj())) {
            throw new RegraNegocioException("Já existe um prestador com o CNPJ " + req.cnpj());
        }
        req.tarifas().stream()
                .filter(t -> !req.modalidades().contains(t.modalidade()))
                .findFirst()
                .ifPresent(t -> {
                    throw new RegraNegocioException("Tarifa para modalidade não oferecida: " + t.modalidade());
                });

        Prestador p = new Prestador(req.nomeFantasia(), req.cnpj(), req.cidade(), req.modalidades(),
                req.horaInicio(), req.horaFim());
        p.definirEquipe(req.possuiTecnico(), req.possuiEnfermeiro());
        p.definirTaxas(valor(req.taxaOxigenio()), valor(req.taxaTecnico()), valor(req.taxaEnfermeiro()));
        req.veiculos().forEach(v -> p.adicionarVeiculo(new Veiculo(v.tipo(), v.modelo(), v.placa(), v.assentos(),
                v.vagasCadeira(), v.maca(), v.oxigenio(), v.combustivel())));
        req.tarifas().forEach(t -> p.adicionarTarifa(new Tarifa(t.modalidade(), t.base(), t.porKm())));
        return PrestadorResponse.de(repository.save(p));
    }

    @Transactional
    public PrestadorResponse atualizarVerificacao(Long id, StatusVerificacao status) {
        Prestador p = entidade(id);
        p.definirVerificacao(status);
        return PrestadorResponse.de(p);
    }

    Prestador entidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Prestador", id));
    }

    private static BigDecimal valor(BigDecimal v) {
        return Objects.requireNonNullElse(v, BigDecimal.ZERO);
    }
}
