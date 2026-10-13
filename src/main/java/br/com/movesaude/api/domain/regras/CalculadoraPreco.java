package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.model.Necessidades;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Tarifa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cotação pela tabela do prestador. Cada linha é arredondada para reais inteiros,
 * então o total é exatamente a soma das linhas.
 */
public final class CalculadoraPreco {

    private CalculadoraPreco() {
    }

    public static Optional<Cotacao> cotar(Prestador prestador, Modalidade modalidade, double distanciaKm,
                                          Necessidades necessidades, boolean idaEVolta) {
        Optional<Tarifa> tarifa = prestador.tarifaPara(modalidade);
        if (tarifa.isEmpty()) {
            return Optional.empty();
        }
        int trechos = idaEVolta ? 2 : 1;
        BigDecimal t = BigDecimal.valueOf(trechos);
        String sufixo = trechos == 2 ? " × 2 trechos" : "";
        String km = BigDecimal.valueOf(distanciaKm).stripTrailingZeros().toPlainString().replace('.', ',');

        List<Cotacao.LinhaPreco> linhas = new ArrayList<>();
        linhas.add(linha("Tarifa base" + sufixo, tarifa.get().getBase().multiply(t)));
        linhas.add(linha("Distância (" + km + " km)" + sufixo,
                tarifa.get().getPorKm().multiply(BigDecimal.valueOf(distanciaKm)).multiply(t)));
        if (necessidades.getOxigenio() == Oxigenio.FORNECER) {
            linhas.add(linha("Oxigênio fornecido" + sufixo, prestador.getTaxaOxigenio().multiply(t)));
        }
        if (necessidades.getEquipe() == Equipe.TECNICO) {
            linhas.add(linha("Técnico(a) de enfermagem" + sufixo, prestador.getTaxaTecnico().multiply(t)));
        }
        if (necessidades.getEquipe() == Equipe.ENFERMEIRO) {
            linhas.add(linha("Enfermeiro(a)" + sufixo, prestador.getTaxaEnfermeiro().multiply(t)));
        }
        if (necessidades.getAcompanhantes() > 0) {
            String rotulo = necessidades.getAcompanhantes() > 1
                    ? necessidades.getAcompanhantes() + " acompanhantes"
                    : "Acompanhante";
            linhas.add(linha(rotulo, BigDecimal.ZERO));
        }

        BigDecimal preco = linhas.stream().map(Cotacao.LinhaPreco::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Optional.of(new Cotacao(Dinheiro.centavos(preco), trechos, List.copyOf(linhas)));
    }

    /** Comissão da MoveSaúde, cobrada do prestador sobre cada corrida. */
    public static BigDecimal comissao(BigDecimal preco, BigDecimal taxa) {
        return Dinheiro.centavos(preco.multiply(taxa));
    }

    private static Cotacao.LinhaPreco linha(String descricao, BigDecimal valor) {
        return new Cotacao.LinhaPreco(descricao, Dinheiro.reaisInteiros(valor));
    }
}
