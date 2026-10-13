package br.com.movesaude.api.domain.regras;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Arredondamento e formatação de valores em reais. */
public final class Dinheiro {

    private Dinheiro() {
    }

    public static BigDecimal centavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal reaisInteiros(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.UNNECESSARY);
    }

    public static String formatar(BigDecimal valor) {
        return "R$ " + centavos(valor).toPlainString().replace('.', ',');
    }

    public static String percentual(BigDecimal fracao) {
        return fracao.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }
}
