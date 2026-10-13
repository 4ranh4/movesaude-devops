package br.com.movesaude.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCorridaTest {

    @Test
    void fluxoNormalDaCorrida() {
        assertThat(StatusCorrida.EM_TRIAGEM.podeIrPara(StatusCorrida.CONFIRMADA)).isTrue();
        assertThat(StatusCorrida.CONFIRMADA.podeIrPara(StatusCorrida.A_CAMINHO)).isTrue();
        assertThat(StatusCorrida.A_CAMINHO.podeIrPara(StatusCorrida.EM_ANDAMENTO)).isTrue();
        assertThat(StatusCorrida.EM_ANDAMENTO.podeIrPara(StatusCorrida.CONCLUIDA)).isTrue();
    }

    @Test
    void corridaEncerradaNaoMudaMais() {
        assertThat(StatusCorrida.CONCLUIDA.proximos()).isEmpty();
        assertThat(StatusCorrida.CANCELADA.proximos()).isEmpty();
        assertThat(StatusCorrida.EM_ANDAMENTO.podeIrPara(StatusCorrida.CANCELADA)).isFalse();
        assertThat(StatusCorrida.EM_TRIAGEM.podeIrPara(StatusCorrida.CONCLUIDA)).isFalse();
    }
}
