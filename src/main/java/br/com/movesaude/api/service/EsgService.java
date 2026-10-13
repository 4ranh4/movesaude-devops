package br.com.movesaude.api.service;

import br.com.movesaude.api.config.AppProperties;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.StatusCorrida;
import br.com.movesaude.api.domain.model.Corrida;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Veiculo;
import br.com.movesaude.api.repository.CorridaRepository;
import br.com.movesaude.api.repository.PrestadorRepository;
import br.com.movesaude.api.web.dto.EsgDtos.Ambiental;
import br.com.movesaude.api.web.dto.EsgDtos.Governanca;
import br.com.movesaude.api.web.dto.EsgDtos.IndicadoresEsg;
import br.com.movesaude.api.web.dto.EsgDtos.Social;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class EsgService {

    static final String METODOLOGIA = "Estimativas ilustrativas: CO2 calculado por km rodado com fatores médios "
            + "por combustível, comparado a um veículo a combustão do mesmo tipo. Dados de demonstração.";

    private final CorridaRepository corridas;
    private final PrestadorRepository prestadores;
    private final AppProperties props;

    public EsgService(CorridaRepository corridas, PrestadorRepository prestadores, AppProperties props) {
        this.corridas = corridas;
        this.prestadores = prestadores;
        this.props = props;
    }

    public IndicadoresEsg indicadores() {
        List<Corrida> todas = corridas.findAll();
        List<Corrida> concluidas = todas.stream().filter(c -> c.getStatus() == StatusCorrida.CONCLUIDA).toList();
        List<Prestador> rede = prestadores.findAll();
        return new IndicadoresEsg(Instant.now(), ambiental(concluidas, rede), social(todas, concluidas),
                governanca(rede), METODOLOGIA);
    }

    private static Ambiental ambiental(List<Corrida> concluidas, List<Prestador> rede) {
        double km = 0;
        double kmLimpo = 0;
        double emitido = 0;
        double referencia = 0;
        for (Corrida c : concluidas) {
            Veiculo v = c.getVeiculo();
            if (v == null) {
                continue;
            }
            double rodado = c.getDistanciaKm() * c.trechos();
            km += rodado;
            if (v.getCombustivel().limpo()) {
                kmLimpo += rodado;
            }
            emitido += rodado * v.getCombustivel().co2KgPorKm();
            referencia += rodado * v.getTipo().co2ReferenciaKgPorKm();
        }
        List<Veiculo> frota = rede.stream().flatMap(p -> p.getVeiculos().stream()).toList();
        long limpos = frota.stream().filter(v -> v.getCombustivel().limpo()).count();
        return new Ambiental(umaCasa(km), umaCasa(kmLimpo), umaCasa(emitido), umaCasa(Math.max(0, referencia - emitido)),
                frota.size(), pct(limpos, frota.size()));
    }

    private static Social social(List<Corrida> todas, List<Corrida> concluidas) {
        long acessiveis = concluidas.stream()
                .filter(c -> c.getModalidadeAtendida() == Modalidade.ACESSIVEL || c.getModalidadeAtendida() == Modalidade.MEDICO)
                .count();
        long tratamento = concluidas.stream().filter(c -> c.getFinalidade().tratamentoContinuo()).count();
        long passageiros = concluidas.stream().map(c -> c.getPassageiro().getId()).distinct().count();
        long creditos = concluidas.stream().mapToLong(Corrida::getTrechosCobertos).sum();
        BigDecimal subsidio = concluidas.stream()
                .map(Corrida::getConvenioPaga)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Social(todas.size(), concluidas.size(), pct(concluidas.size(), todas.size()), acessiveis, tratamento,
                passageiros, creditos, subsidio);
    }

    private Governanca governanca(List<Prestador> rede) {
        long verificados = rede.stream().filter(Prestador::verificado).count();
        return new Governanca(rede.size(), verificados, pct(verificados, rede.size()),
                props.comissao().multiply(BigDecimal.valueOf(100)).stripTrailingZeros(),
                "Prestadores só recebem corridas depois da verificação de documentos; "
                        + "preço detalhado linha a linha e comissão única para todos.");
    }

    private static double pct(long parte, long total) {
        return total == 0 ? 0 : umaCasa(100.0 * parte / total);
    }

    private static double umaCasa(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
