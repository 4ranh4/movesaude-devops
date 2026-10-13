package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Veiculo;

import java.time.LocalTime;
import java.util.List;

/** Resultado do matching para um prestador: compatível ou não, e por quê. */
public record Avaliacao(
        Prestador prestador,
        boolean compativel,
        List<String> motivos,
        Modalidade modalidade,
        Veiculo veiculo,
        Cotacao cotacao,
        LocalTime horario,
        boolean horarioExato,
        double pontuacao) {
}
