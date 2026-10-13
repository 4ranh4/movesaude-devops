package br.com.movesaude.api.domain.regras;

import java.math.BigDecimal;
import java.util.List;

/** Preço total da corrida (todos os trechos) e o detalhamento linha a linha. */
public record Cotacao(BigDecimal preco, int trechos, List<LinhaPreco> detalhamento) {

    public record LinhaPreco(String descricao, BigDecimal valor) {
    }
}
