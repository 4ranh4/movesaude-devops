package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.TipoCobertura;
import br.com.movesaude.api.domain.model.PlanoConvenio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Aplica o benefício mobilidade do convênio: usa os créditos que restam no mês e,
 * no que não couber, aplica o desconto do plano.
 */
public final class CalculadoraCobertura {

    private CalculadoraCobertura() {
    }

    static Optional<String> motivoInelegivel(PlanoConvenio plano, Finalidade finalidade, Modalidade modalidade) {
        if (plano.getCreditosMensais() <= 0) {
            return Optional.of("plano sem créditos de viagem");
        }
        if (!plano.getFinalidadesElegiveis().contains(finalidade)) {
            return Optional.of(finalidade.name().toLowerCase().replace('_', ' ') + " não entra nos créditos");
        }
        if (!plano.getModalidadesCobertas().contains(modalidade)) {
            return Optional.of(modalidade.descricao().toLowerCase() + " não entra nos créditos");
        }
        return Optional.empty();
    }

    public static Cobertura calcular(PlanoConvenio plano, Finalidade finalidade, Modalidade modalidade,
                                     int trechos, BigDecimal preco, int creditosUsadosNoMes) {
        if (plano == null) {
            return Cobertura.particular(preco);
        }
        Optional<String> motivo = motivoInelegivel(plano, finalidade, modalidade);
        int restantes = Math.max(0, plano.getCreditosMensais() - creditosUsadosNoMes);
        int cobertos = motivo.isPresent() ? 0 : Math.min(restantes, trechos);
        int descobertos = trechos - cobertos;

        BigDecimal desconto = plano.getDescontoPct();
        BigDecimal porTrecho = preco.divide(BigDecimal.valueOf(trechos), 4, RoundingMode.HALF_UP);
        BigDecimal copart = plano.getCoparticipacaoPorTrecho().min(porTrecho);
        BigDecimal clientePaga = Dinheiro.centavos(copart.multiply(BigDecimal.valueOf(cobertos))
                .add(porTrecho.multiply(BigDecimal.valueOf(descobertos)).multiply(BigDecimal.ONE.subtract(desconto))));
        BigDecimal convenioPaga = Dinheiro.centavos(preco.subtract(clientePaga));

        TipoCobertura tipo;
        String resumo;
        if (cobertos == trechos) {
            tipo = TipoCobertura.CREDITO;
            resumo = "Coberto pelo convênio: usa " + cobertos + (cobertos == 1 ? " crédito" : " créditos");
            if (copart.signum() > 0) {
                resumo += " + coparticipação de " + Dinheiro.formatar(copart.multiply(BigDecimal.valueOf(cobertos)));
            }
        } else if (cobertos > 0) {
            tipo = TipoCobertura.PARCIAL;
            resumo = cobertos + (cobertos == 1 ? " trecho coberto" : " trechos cobertos")
                    + " por crédito; o restante com " + Dinheiro.percentual(desconto) + " de desconto";
        } else if (desconto.signum() > 0) {
            tipo = TipoCobertura.DESCONTO;
            resumo = motivo.map(m -> Dinheiro.percentual(desconto) + " de desconto do convênio (" + m + ")")
                    .orElse("Créditos do mês esgotados: " + Dinheiro.percentual(desconto) + " de desconto");
        } else {
            tipo = TipoCobertura.PARTICULAR;
            resumo = motivo.map(m -> "Sem cobertura do convênio (" + m + ")").orElse("Créditos do mês esgotados");
        }

        return new Cobertura(tipo, cobertos, clientePaga, convenioPaga,
                descobertos > 0 ? desconto : BigDecimal.ZERO,
                cobertos > 0 && plano.getAutorizacaoPara().contains(modalidade),
                resumo);
    }
}
