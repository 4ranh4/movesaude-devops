package br.com.movesaude.api.domain;

/** Modalidade de transporte indicada pela triagem das necessidades do passageiro. */
public enum Modalidade {
    ASSISTIDO("Transporte assistido"),
    ACESSIVEL("Transporte acessível"),
    MEDICO("Transporte médico não emergencial");

    private final String descricao;

    Modalidade(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
