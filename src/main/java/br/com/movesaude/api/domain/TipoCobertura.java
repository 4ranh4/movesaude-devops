package br.com.movesaude.api.domain;

public enum TipoCobertura {
    /** Sem convênio ou sem benefício aplicável. */
    PARTICULAR,
    /** Todos os trechos cobertos por créditos do convênio. */
    CREDITO,
    /** Parte dos trechos coberta por créditos; o restante com desconto. */
    PARCIAL,
    /** Nenhum trecho coberto, mas com desconto do convênio. */
    DESCONTO
}
