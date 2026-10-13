package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.TipoCobertura;

import java.math.BigDecimal;

/** Quanto o passageiro paga, quanto o convênio paga e quantos créditos a corrida consome. */
public record Cobertura(
        TipoCobertura tipo,
        int trechosCobertos,
        BigDecimal clientePaga,
        BigDecimal convenioPaga,
        BigDecimal descontoPct,
        boolean exigeAutorizacao,
        String resumo) {

    public static Cobertura particular(BigDecimal preco) {
        return new Cobertura(TipoCobertura.PARTICULAR, 0, Dinheiro.centavos(preco), Dinheiro.centavos(BigDecimal.ZERO),
                BigDecimal.ZERO, false, "Pagamento particular");
    }
}
