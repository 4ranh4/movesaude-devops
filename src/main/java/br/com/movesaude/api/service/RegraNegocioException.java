package br.com.movesaude.api.service;

/** Pedido válido no formato, mas que viola uma regra do negócio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
