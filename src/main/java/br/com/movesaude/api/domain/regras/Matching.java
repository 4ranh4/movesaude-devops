package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.TipoVeiculo;
import br.com.movesaude.api.domain.model.Necessidades;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Veiculo;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Matching da central (MVP Concierge): filtra os prestadores compatíveis com as necessidades
 * do passageiro e sugere até três opções variadas (melhor geral, mais barata, mais bem avaliada).
 */
public final class Matching {

    /** Quanto o embarque pode ser adiado em relação ao horário pedido. */
    static final Duration ATRASO_MAXIMO = Duration.ofMinutes(90);

    /** O embarque precisa acontecer pelo menos 30 minutos antes do fim do expediente. */
    static final Duration MARGEM_FIM_EXPEDIENTE = Duration.ofMinutes(30);

    private Matching() {
    }

    /** Retorna o motivo da incompatibilidade, ou vazio se o veículo atende. */
    public static Optional<String> problemaVeiculo(Veiculo veiculo, Modalidade modalidade, Necessidades n) {
        if (veiculo.getTipo().modalidade() != modalidade) {
            return Optional.of("Tipo de veículo diferente da modalidade");
        }
        if (n.getMobilidade() == Mobilidade.CADEIRA_PERMANECE && veiculo.getVagasCadeira() < 1) {
            return Optional.of("Sem vaga para cadeira de rodas");
        }
        if (n.getMobilidade() == Mobilidade.MACA && !veiculo.isMaca()) {
            return Optional.of("Sem maca no veículo");
        }
        if (n.getOxigenio() == Oxigenio.FORNECER && !veiculo.isOxigenio()) {
            return Optional.of("Sem oxigênio a bordo");
        }
        boolean sentado = n.getMobilidade() != Mobilidade.CADEIRA_PERMANECE && n.getMobilidade() != Mobilidade.MACA;
        int lugares = n.getAcompanhantes() + (sentado ? 1 : 0);
        if (veiculo.getAssentos() < lugares) {
            return Optional.of("Menos de " + lugares + " lugares disponíveis");
        }
        if (n.isObesidade() && veiculo.getTipo() == TipoVeiculo.SEDAN) {
            return Optional.of("Espaço interno insuficiente");
        }
        return Optional.empty();
    }

    static Optional<String> problemaEquipe(Prestador prestador, Necessidades n) {
        if (n.getEquipe() == Equipe.TECNICO && !prestador.isPossuiTecnico() && !prestador.isPossuiEnfermeiro()) {
            return Optional.of("Sem técnico(a) de enfermagem na equipe");
        }
        if (n.getEquipe() == Equipe.ENFERMEIRO && !prestador.isPossuiEnfermeiro()) {
            return Optional.of("Sem enfermeiro(a) na equipe");
        }
        return Optional.empty();
    }

    /** Embarque possível dentro do expediente do prestador, ou o motivo de não atender. */
    record Disponibilidade(LocalTime horario, boolean exato, String motivo) {
        boolean ok() {
            return motivo == null;
        }
    }

    static Disponibilidade disponibilidade(Prestador prestador, LocalTime pedido) {
        LocalTime embarque = pedido.isBefore(prestador.getHoraInicio()) ? prestador.getHoraInicio() : pedido;
        if (Duration.between(pedido, embarque).compareTo(ATRASO_MAXIMO) > 0) {
            return new Disponibilidade(null, false, "Sem horário livre próximo ao solicitado");
        }
        if (embarque.isAfter(prestador.getHoraFim().minus(MARGEM_FIM_EXPEDIENTE))) {
            return new Disponibilidade(null, false,
                    "Fora do horário de atendimento (" + prestador.getHoraInicio() + "–" + prestador.getHoraFim() + ")");
        }
        return new Disponibilidade(embarque, embarque.equals(pedido), null);
    }

    public static Avaliacao avaliar(Prestador prestador, Solicitacao s) {
        List<String> motivos = new ArrayList<>();
        if (!prestador.verificado()) {
            motivos.add("Cadastro ainda não verificado");
        }
        if (prestador.isPausado()) {
            motivos.add("Pausou o recebimento de solicitações");
        }

        List<Modalidade> desejadas = Triagem.modalidadesCompativeis(s.necessidades());
        List<Modalidade> oferecidas = desejadas.stream().filter(prestador.getModalidades()::contains).toList();
        Modalidade modalidade = null;
        Veiculo veiculo = null;

        if (oferecidas.isEmpty()) {
            motivos.add("Não oferece " + desejadas.getFirst().descricao().toLowerCase());
        } else {
            String primeiroProblema = null;
            busca:
            for (Modalidade m : oferecidas) {
                for (Veiculo v : prestador.getVeiculos()) {
                    Optional<String> problema = problemaVeiculo(v, m, s.necessidades());
                    if (problema.isEmpty()) {
                        modalidade = m;
                        veiculo = v;
                        break busca;
                    }
                    if (primeiroProblema == null && v.getTipo().modalidade() == m) {
                        primeiroProblema = problema.get();
                    }
                }
            }
            if (veiculo == null) {
                motivos.add(primeiroProblema != null ? primeiroProblema : "Sem veículo compatível");
            }
        }

        problemaEquipe(prestador, s.necessidades()).ifPresent(motivos::add);

        Disponibilidade disp = disponibilidade(prestador, s.hora());
        if (!disp.ok()) {
            motivos.add(disp.motivo());
        }

        Cotacao cotacao = null;
        if (modalidade != null) {
            cotacao = CalculadoraPreco.cotar(prestador, modalidade, s.distanciaKm(), s.necessidades(), s.idaEVolta())
                    .orElse(null);
            if (cotacao == null) {
                motivos.add("Sem tabela de preço para esta modalidade");
            }
        }

        boolean compativel = motivos.isEmpty();
        double pontuacao = -1;
        if (compativel) {
            pontuacao = prestador.getAvaliacao() * 20
                    + (disp.exato() ? 12 : 0)
                    + (modalidade == desejadas.getFirst() ? 10 : 0)
                    + Math.min(prestador.getCorridasConcluidas(), 1500) / 150.0
                    - cotacao.preco().doubleValue() / 25;
        }
        return new Avaliacao(prestador, compativel, List.copyOf(motivos), modalidade, veiculo, cotacao,
                disp.horario(), disp.exato(), pontuacao);
    }

    /** Avalia todos os prestadores: compatíveis primeiro, pela pontuação. */
    public static List<Avaliacao> avaliarTodos(List<Prestador> prestadores, Solicitacao s) {
        return prestadores.stream()
                .map(p -> avaliar(p, s))
                .sorted(Comparator.comparing(Avaliacao::compativel).reversed()
                        .thenComparing(Comparator.comparingDouble(Avaliacao::pontuacao).reversed()))
                .toList();
    }

    /**
     * Sugestão da central: a melhor opção geral, a mais barata e a mais bem avaliada,
     * completando pela pontuação. Garante variedade real na comparação.
     */
    public static List<Avaliacao> sugerirOpcoes(List<Avaliacao> avaliacoes, int maximo) {
        List<Avaliacao> candidatas = avaliacoes.stream().filter(Avaliacao::compativel).toList();
        if (candidatas.isEmpty()) {
            return List.of();
        }
        List<Avaliacao> porPontuacao = candidatas.stream()
                .sorted(Comparator.comparingDouble(Avaliacao::pontuacao).reversed())
                .toList();
        List<Avaliacao> escolhidas = new ArrayList<>();
        escolhidas.add(porPontuacao.getFirst());

        candidatas.stream()
                .sorted(Comparator.comparing((Avaliacao a) -> a.cotacao().preco()))
                .filter(a -> !escolhidas.contains(a))
                .findFirst()
                .ifPresent(a -> adicionar(escolhidas, a, maximo));
        candidatas.stream()
                .sorted(Comparator.comparingDouble((Avaliacao a) -> a.prestador().getAvaliacao()).reversed()
                        .thenComparing(a -> a.cotacao().preco()))
                .filter(a -> !escolhidas.contains(a))
                .findFirst()
                .ifPresent(a -> adicionar(escolhidas, a, maximo));
        porPontuacao.forEach(a -> adicionar(escolhidas, a, maximo));
        return List.copyOf(escolhidas);
    }

    private static void adicionar(List<Avaliacao> escolhidas, Avaliacao a, int maximo) {
        if (escolhidas.size() < maximo && !escolhidas.contains(a)) {
            escolhidas.add(a);
        }
    }
}
