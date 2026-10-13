package br.com.movesaude.api.domain;

import java.util.Set;

/** Ciclo de vida da corrida, com as transições permitidas. */
public enum StatusCorrida {
    EM_TRIAGEM,
    CONFIRMADA,
    A_CAMINHO,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    public Set<StatusCorrida> proximos() {
        return switch (this) {
            case EM_TRIAGEM -> Set.of(CONFIRMADA, CANCELADA);
            case CONFIRMADA -> Set.of(A_CAMINHO, CANCELADA);
            case A_CAMINHO -> Set.of(EM_ANDAMENTO, CANCELADA);
            case EM_ANDAMENTO -> Set.of(CONCLUIDA);
            case CONCLUIDA, CANCELADA -> Set.of();
        };
    }

    public boolean podeIrPara(StatusCorrida destino) {
        return proximos().contains(destino);
    }
}
