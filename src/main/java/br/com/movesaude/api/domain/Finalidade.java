package br.com.movesaude.api.domain;

public enum Finalidade {
    CONSULTA,
    EXAME,
    TERAPIA,
    HEMODIALISE,
    ONCOLOGIA,
    ALTA_HOSPITALAR,
    OUTRO;

    /** Tratamentos contínuos: faltar a eles agrava o quadro, por isso entram no indicador social. */
    public boolean tratamentoContinuo() {
        return this == TERAPIA || this == HEMODIALISE || this == ONCOLOGIA;
    }
}
