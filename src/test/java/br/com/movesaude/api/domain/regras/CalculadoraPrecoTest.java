package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Modalidade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraPrecoTest {

    @Test
    void somaTarifaBaseEDistanciaArredondandoCadaLinha() {
        // 45 + 4,20 × 8,4 km = 45 + 35,28 → 45 + 35 = 80
        Cotacao c = CalculadoraPreco.cotar(Fixtures.vanAcessivel("A", 4.8, "45", "4.20"), Modalidade.ACESSIVEL,
                8.4, Fixtures.cadeirante(), false).orElseThrow();
        assertThat(c.trechos()).isEqualTo(1);
        assertThat(c.preco()).isEqualByComparingTo("80");
        assertThat(c.detalhamento()).extracting(Cotacao.LinhaPreco::descricao)
                .containsExactly("Tarifa base", "Distância (8,4 km)", "Acompanhante");
    }

    @Test
    void idaEVoltaCobraOsDoisTrechos() {
        // (45 × 2) + (4,20 × 8,4 × 2 = 70,56 → 71) = 161
        Cotacao c = CalculadoraPreco.cotar(Fixtures.vanAcessivel("A", 4.8, "45", "4.20"), Modalidade.ACESSIVEL,
                8.4, Fixtures.cadeirante(), true).orElseThrow();
        assertThat(c.trechos()).isEqualTo(2);
        assertThat(c.preco()).isEqualByComparingTo("161");
    }

    @Test
    void transporteMedicoSomaOxigenioETecnico() {
        // 180 + (7,50 × 14,5 = 108,75 → 109) + 60 + 90 = 439
        Cotacao c = CalculadoraPreco.cotar(Fixtures.ambulancia(), Modalidade.MEDICO, 14.5,
                Fixtures.macaComOxigenio(), false).orElseThrow();
        assertThat(c.preco()).isEqualByComparingTo("439");
        assertThat(c.detalhamento()).extracting(Cotacao.LinhaPreco::descricao)
                .contains("Oxigênio fornecido", "Técnico(a) de enfermagem");
    }

    @Test
    void semTabelaParaAModalidadeNaoHaCotacao() {
        assertThat(CalculadoraPreco.cotar(Fixtures.sedan("S"), Modalidade.MEDICO, 5, Fixtures.andaSemAjuda(), false))
                .isEmpty();
    }

    @Test
    void comissaoDe18PorCento() {
        assertThat(CalculadoraPreco.comissao(new BigDecimal("161.00"), new BigDecimal("0.18")))
                .isEqualByComparingTo("28.98");
    }
}
