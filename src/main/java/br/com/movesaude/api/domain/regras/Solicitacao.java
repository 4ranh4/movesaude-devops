package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.model.Necessidades;

import java.time.LocalTime;

/** Dados da corrida que o matching precisa para avaliar um prestador. */
public record Solicitacao(Necessidades necessidades, LocalTime hora, boolean idaEVolta, double distanciaKm) {
}
