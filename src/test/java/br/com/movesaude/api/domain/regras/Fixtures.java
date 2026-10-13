package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Combustivel;
import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.StatusVerificacao;
import br.com.movesaude.api.domain.TipoVeiculo;
import br.com.movesaude.api.domain.model.Necessidades;
import br.com.movesaude.api.domain.model.PlanoConvenio;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Tarifa;
import br.com.movesaude.api.domain.model.Veiculo;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/** Objetos de teste montados em memória, sem banco. */
final class Fixtures {

    private Fixtures() {
    }

    static Prestador vanAcessivel(String nome, double avaliacao, String base, String porKm) {
        Prestador p = new Prestador(nome, "00.000.000/0001-" + Math.abs(nome.hashCode() % 90 + 10), "São Paulo",
                Set.of(Modalidade.ACESSIVEL), LocalTime.of(6, 0), LocalTime.of(20, 0));
        p.definirVerificacao(StatusVerificacao.VERIFICADO);
        p.definirReputacao(avaliacao, 100);
        p.adicionarVeiculo(new Veiculo(TipoVeiculo.VAN_RAMPA, "Van rampa", "ABC1D23", 4, 1, false, false, Combustivel.DIESEL));
        p.adicionarTarifa(new Tarifa(Modalidade.ACESSIVEL, new BigDecimal(base), new BigDecimal(porKm)));
        return p;
    }

    static Prestador sedan(String nome) {
        Prestador p = new Prestador(nome, "11.111.111/0001-11", "São Paulo", Set.of(Modalidade.ASSISTIDO),
                LocalTime.of(8, 0), LocalTime.of(18, 0));
        p.definirVerificacao(StatusVerificacao.VERIFICADO);
        p.adicionarVeiculo(new Veiculo(TipoVeiculo.SEDAN, "Sedã", "SED1A11", 4, 0, false, false, Combustivel.ELETRICO));
        p.adicionarTarifa(new Tarifa(Modalidade.ASSISTIDO, new BigDecimal("20"), new BigDecimal("2.50")));
        return p;
    }

    static Prestador ambulancia() {
        Prestador p = new Prestador("Remoção", "22.222.222/0001-22", "Guarulhos", Set.of(Modalidade.MEDICO),
                LocalTime.of(0, 0), LocalTime.of(23, 59));
        p.definirVerificacao(StatusVerificacao.VERIFICADO);
        p.definirEquipe(true, true);
        p.definirTaxas(new BigDecimal("60"), new BigDecimal("90"), new BigDecimal("150"));
        p.adicionarVeiculo(new Veiculo(TipoVeiculo.AMBULANCIA, "Sprinter", "AMB1B22", 2, 0, true, true, Combustivel.DIESEL));
        p.adicionarTarifa(new Tarifa(Modalidade.MEDICO, new BigDecimal("180"), new BigDecimal("7.50")));
        return p;
    }

    static Necessidades cadeirante() {
        return new Necessidades(Mobilidade.CADEIRA_PERMANECE, 1, Oxigenio.NAO, Equipe.NAO, false, false);
    }

    static Necessidades andaSemAjuda() {
        return Necessidades.simples(Mobilidade.ANDA_SEM_AJUDA);
    }

    static Necessidades macaComOxigenio() {
        return new Necessidades(Mobilidade.MACA, 1, Oxigenio.FORNECER, Equipe.TECNICO, false, false);
    }

    static PlanoConvenio plano(int creditos, String coparticipacao, String desconto, Set<Modalidade> autorizacao) {
        return new PlanoConvenio("teste", "Operadora Teste", "Plano Teste", creditos, new BigDecimal(coparticipacao),
                new BigDecimal(desconto), EnumSet.allOf(Modalidade.class),
                EnumSet.of(Finalidade.CONSULTA, Finalidade.EXAME, Finalidade.HEMODIALISE), autorizacao);
    }
}
