package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Combustivel;
import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.StatusVerificacao;
import br.com.movesaude.api.domain.TipoVeiculo;
import br.com.movesaude.api.domain.model.Necessidades;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Tarifa;
import br.com.movesaude.api.domain.model.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingTest {

    private static Solicitacao pedido(Necessidades n, LocalTime hora) {
        return new Solicitacao(n, hora, false, 8.0);
    }

    @Test
    void vanComRampaAtendeCadeirante() {
        Avaliacao a = Matching.avaliar(Fixtures.vanAcessivel("Acessa", 4.8, "45", "4.20"),
                pedido(Fixtures.cadeirante(), LocalTime.of(10, 0)));
        assertThat(a.compativel()).isTrue();
        assertThat(a.modalidade()).isEqualTo(Modalidade.ACESSIVEL);
        assertThat(a.horario()).isEqualTo(LocalTime.of(10, 0));
        assertThat(a.horarioExato()).isTrue();
    }

    @Test
    void sedanNaoAtendeQuemPermaneceNaCadeira() {
        Avaliacao a = Matching.avaliar(Fixtures.sedan("Sedã"), pedido(Fixtures.cadeirante(), LocalTime.of(10, 0)));
        assertThat(a.compativel()).isFalse();
        assertThat(a.motivos()).contains("Não oferece transporte acessível");
    }

    @Test
    void veiculoSemVagaParaCadeiraEhDescartado() {
        Prestador p = new Prestador("Sem vaga", "33.333.333/0001-33", "São Paulo", Set.of(Modalidade.ACESSIVEL),
                LocalTime.of(6, 0), LocalTime.of(20, 0));
        p.definirVerificacao(StatusVerificacao.VERIFICADO);
        p.adicionarVeiculo(new Veiculo(TipoVeiculo.VAN_RAMPA, "Van", "SVG1A11", 4, 0, false, false, Combustivel.FLEX));
        p.adicionarTarifa(new Tarifa(Modalidade.ACESSIVEL, BigDecimal.TEN, BigDecimal.ONE));

        Avaliacao a = Matching.avaliar(p, pedido(Fixtures.cadeirante(), LocalTime.of(10, 0)));
        assertThat(a.compativel()).isFalse();
        assertThat(a.motivos()).contains("Sem vaga para cadeira de rodas");
    }

    @Test
    void prestadorNaoVerificadoOuPausadoNaoRecebeCorridas() {
        Prestador emAnalise = Fixtures.vanAcessivel("Novo", 5.0, "40", "4");
        emAnalise.definirVerificacao(StatusVerificacao.EM_ANALISE);
        Prestador pausado = Fixtures.vanAcessivel("Pausado", 4.5, "40", "4");
        pausado.definirPausado(true);

        assertThat(Matching.avaliar(emAnalise, pedido(Fixtures.cadeirante(), LocalTime.of(10, 0))).motivos())
                .contains("Cadastro ainda não verificado");
        assertThat(Matching.avaliar(pausado, pedido(Fixtures.cadeirante(), LocalTime.of(10, 0))).motivos())
                .contains("Pausou o recebimento de solicitações");
    }

    @Test
    void foraDoExpedienteNaoAtende() {
        Avaliacao a = Matching.avaliar(Fixtures.vanAcessivel("Acessa", 4.8, "45", "4.20"),
                pedido(Fixtures.cadeirante(), LocalTime.of(19, 45)));
        assertThat(a.compativel()).isFalse();
        assertThat(a.motivos()).anyMatch(m -> m.startsWith("Fora do horário de atendimento"));
    }

    @Test
    void pedidoAntesDaAberturaEhAtendidoNoInicioDoExpediente() {
        Avaliacao a = Matching.avaliar(Fixtures.sedan("Sedã"), pedido(Fixtures.andaSemAjuda(), LocalTime.of(7, 0)));
        assertThat(a.compativel()).isTrue();
        assertThat(a.horario()).isEqualTo(LocalTime.of(8, 0));
        assertThat(a.horarioExato()).isFalse();
    }

    @Test
    void transporteMedicoExigeEquipeDeEnfermagem() {
        Prestador semEquipe = Fixtures.ambulancia();
        semEquipe.definirEquipe(false, false);
        Necessidades n = new Necessidades(Mobilidade.MACA, 0, Oxigenio.NAO, Equipe.ENFERMEIRO, false, false);
        assertThat(Matching.avaliar(semEquipe, pedido(n, LocalTime.of(10, 0))).motivos())
                .contains("Sem enfermeiro(a) na equipe");
        assertThat(Matching.avaliar(Fixtures.ambulancia(), pedido(n, LocalTime.of(10, 0))).compativel()).isTrue();
    }

    @Test
    void sugereAteTresOpcoesComAMelhorPrimeiro() {
        List<Prestador> rede = List.of(
                Fixtures.vanAcessivel("Barata", 4.0, "30", "3"),
                Fixtures.vanAcessivel("Bem avaliada", 5.0, "60", "5"),
                Fixtures.vanAcessivel("Equilibrada", 4.8, "40", "4"),
                Fixtures.vanAcessivel("Quarta", 4.1, "50", "5"),
                Fixtures.sedan("Incompatível"));

        List<Avaliacao> avaliacoes = Matching.avaliarTodos(rede, pedido(Fixtures.cadeirante(), LocalTime.of(10, 0)));
        List<Avaliacao> opcoes = Matching.sugerirOpcoes(avaliacoes, 3);

        assertThat(opcoes).hasSize(3);
        assertThat(opcoes.getFirst().pontuacao()).isEqualTo(avaliacoes.getFirst().pontuacao());
        assertThat(opcoes).extracting(o -> o.prestador().getNomeFantasia()).contains("Barata", "Bem avaliada");
        assertThat(opcoes).noneMatch(o -> o.prestador().getNomeFantasia().equals("Incompatível"));
        assertThat(avaliacoes.getLast().compativel()).isFalse();
    }
}
