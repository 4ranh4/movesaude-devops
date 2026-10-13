package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.model.Necessidades;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriagemTest {

    @Test
    void macaOxigenioOuEquipeIndicamTransporteMedico() {
        assertThat(Triagem.modalidadeIndicada(Necessidades.simples(Mobilidade.MACA))).isEqualTo(Modalidade.MEDICO);
        assertThat(Triagem.modalidadeIndicada(
                new Necessidades(Mobilidade.ANDA_SEM_AJUDA, 0, Oxigenio.FORNECER, Equipe.NAO, false, false)))
                .isEqualTo(Modalidade.MEDICO);
        assertThat(Triagem.modalidadeIndicada(
                new Necessidades(Mobilidade.ANDA_COM_APOIO, 0, Oxigenio.NAO, Equipe.ENFERMEIRO, false, false)))
                .isEqualTo(Modalidade.MEDICO);
    }

    @Test
    void quemPermaneceNaCadeiraPrecisaDeTransporteAcessivel() {
        assertThat(Triagem.modalidadeIndicada(Fixtures.cadeirante())).isEqualTo(Modalidade.ACESSIVEL);
        assertThat(Triagem.modalidadesCompativeis(Fixtures.cadeirante())).containsExactly(Modalidade.ACESSIVEL);
    }

    @Test
    void transporteAssistidoAceitaVeiculoAcessivelComoAlternativa() {
        List<Modalidade> compativeis = Triagem.modalidadesCompativeis(Fixtures.andaSemAjuda());
        assertThat(compativeis).containsExactly(Modalidade.ASSISTIDO, Modalidade.ACESSIVEL);
    }

    @Test
    void oxigenioProprioNaoExigeTransporteMedico() {
        Necessidades n = new Necessidades(Mobilidade.ANDA_COM_APOIO, 0, Oxigenio.PROPRIO, Equipe.NAO, false, false);
        assertThat(Triagem.modalidadeIndicada(n)).isEqualTo(Modalidade.ASSISTIDO);
        assertThat(Triagem.motivo(n)).contains("caminha com apoio");
    }
}
