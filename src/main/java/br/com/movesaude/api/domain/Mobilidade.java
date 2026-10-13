package br.com.movesaude.api.domain;

public enum Mobilidade {
    ANDA_SEM_AJUDA,
    ANDA_COM_APOIO,
    /** Passa para o banco; a cadeira dobrável vai no porta-malas. */
    CADEIRA_TRANSFERE,
    /** Permanece na cadeira de rodas durante o trajeto. */
    CADEIRA_PERMANECE,
    MACA
}
