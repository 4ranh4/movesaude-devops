package br.com.movesaude.api.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Indicadores ESG da operação, calculados a partir das corridas e da rede de prestadores. */
public final class EsgDtos {

    private EsgDtos() {
    }

    public record IndicadoresEsg(Instant geradoEm, Ambiental ambiental, Social social, Governanca governanca,
                                 String metodologia) {
    }

    /** E: emissões da frota parceira nas corridas concluídas. */
    public record Ambiental(
            double kmPercorridos,
            double kmFrotaLimpa,
            double co2EmitidoKg,
            double co2EvitadoKg,
            int veiculosNaRede,
            double frotaLimpaPct) {
    }

    /** S: acesso ao cuidado para pessoas com deficiência, idosos e pacientes em tratamento. */
    public record Social(
            long corridasTotal,
            long corridasConcluidas,
            double taxaConclusaoPct,
            long corridasAcessiveisOuMedicas,
            long corridasTratamentoContinuo,
            long passageirosAtendidos,
            long creditosConvenioUtilizados,
            BigDecimal valorSubsidiadoConvenio) {
    }

    /** G: verificação dos prestadores e transparência da comissão. */
    public record Governanca(
            long prestadores,
            long prestadoresVerificados,
            double prestadoresVerificadosPct,
            BigDecimal comissaoPlataformaPct,
            String politica) {
    }
}
