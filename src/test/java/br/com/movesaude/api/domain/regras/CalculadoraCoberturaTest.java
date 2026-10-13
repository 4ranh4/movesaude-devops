package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.TipoCobertura;
import br.com.movesaude.api.domain.model.PlanoConvenio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraCoberturaTest {

    private static final BigDecimal PRECO = new BigDecimal("160.00");

    @Test
    void creditosDisponiveisCobremACorridaComCoparticipacao() {
        PlanoConvenio plano = Fixtures.plano(8, "10", "0.15", Set.of());
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.CONSULTA, Modalidade.ACESSIVEL, 2, PRECO, 0);

        assertThat(c.tipo()).isEqualTo(TipoCobertura.CREDITO);
        assertThat(c.trechosCobertos()).isEqualTo(2);
        assertThat(c.clientePaga()).isEqualByComparingTo("20.00");
        assertThat(c.convenioPaga()).isEqualByComparingTo("140.00");
        assertThat(c.resumo()).isEqualTo("Coberto pelo convênio: usa 2 créditos + coparticipação de R$ 20,00");
    }

    @Test
    void semCoparticipacaoOPassageiroPagaZero() {
        PlanoConvenio plano = Fixtures.plano(30, "0", "0.20", Set.of());
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.HEMODIALISE, Modalidade.ACESSIVEL, 2, PRECO, 14);
        assertThat(c.clientePaga()).isEqualByComparingTo("0");
        assertThat(c.convenioPaga()).isEqualByComparingTo("160");
    }

    @Test
    void ultimoCreditoDoMesCobreUmTrechoEORestanteTemDesconto() {
        PlanoConvenio plano = Fixtures.plano(8, "10", "0.15", Set.of());
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.CONSULTA, Modalidade.ACESSIVEL, 2, PRECO, 7);

        // 1 trecho por crédito (R$ 10) + 1 trecho de R$ 80 com 15% de desconto (R$ 68) = R$ 78
        assertThat(c.tipo()).isEqualTo(TipoCobertura.PARCIAL);
        assertThat(c.trechosCobertos()).isEqualTo(1);
        assertThat(c.clientePaga()).isEqualByComparingTo("78.00");
        assertThat(c.descontoPct()).isEqualByComparingTo("0.15");
    }

    @Test
    void creditosEsgotadosAplicamSoODesconto() {
        PlanoConvenio plano = Fixtures.plano(8, "10", "0.15", Set.of());
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.CONSULTA, Modalidade.ACESSIVEL, 2, PRECO, 8);
        assertThat(c.tipo()).isEqualTo(TipoCobertura.DESCONTO);
        assertThat(c.clientePaga()).isEqualByComparingTo("136.00");
        assertThat(c.resumo()).startsWith("Créditos do mês esgotados");
    }

    @Test
    void finalidadeForaDoPlanoNaoUsaCreditos() {
        PlanoConvenio plano = Fixtures.plano(8, "10", "0.15", Set.of());
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.TERAPIA, Modalidade.ACESSIVEL, 1, PRECO, 0);
        assertThat(c.trechosCobertos()).isZero();
        assertThat(c.resumo()).contains("terapia não entra nos créditos");
    }

    @Test
    void transporteMedicoPodeExigirAutorizacaoPrevia() {
        PlanoConvenio plano = Fixtures.plano(12, "15", "0.20", Set.of(Modalidade.MEDICO));
        Cobertura c = CalculadoraCobertura.calcular(plano, Finalidade.EXAME, Modalidade.MEDICO, 1, PRECO, 0);
        assertThat(c.exigeAutorizacao()).isTrue();
    }

    @Test
    void semConvenioEhParticular() {
        Cobertura c = CalculadoraCobertura.calcular(null, Finalidade.CONSULTA, Modalidade.ASSISTIDO, 1, PRECO, 0);
        assertThat(c.tipo()).isEqualTo(TipoCobertura.PARTICULAR);
        assertThat(c.clientePaga()).isEqualByComparingTo("160");
        assertThat(c.convenioPaga()).isEqualByComparingTo("0");
    }
}
