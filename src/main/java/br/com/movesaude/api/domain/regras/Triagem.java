package br.com.movesaude.api.domain.regras;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.model.Necessidades;

import java.util.List;

/** Triagem: a partir das necessidades do passageiro, indica a modalidade de transporte. */
public final class Triagem {

    private Triagem() {
    }

    public static Modalidade modalidadeIndicada(Necessidades n) {
        if (n.getMobilidade() == Mobilidade.MACA || n.getOxigenio() == Oxigenio.FORNECER || n.getEquipe() != Equipe.NAO) {
            return Modalidade.MEDICO;
        }
        if (n.getMobilidade() == Mobilidade.CADEIRA_PERMANECE) {
            return Modalidade.ACESSIVEL;
        }
        return Modalidade.ASSISTIDO;
    }

    /** Modalidades que atendem às necessidades, da mais indicada para a alternativa. */
    public static List<Modalidade> modalidadesCompativeis(Necessidades n) {
        Modalidade principal = modalidadeIndicada(n);
        if (principal == Modalidade.ASSISTIDO) {
            return List.of(Modalidade.ASSISTIDO, Modalidade.ACESSIVEL);
        }
        return List.of(principal);
    }

    /** Explicação em linguagem simples de por que a modalidade foi indicada. */
    public static String motivo(Necessidades n) {
        if (n.getMobilidade() == Mobilidade.MACA) {
            return "o passageiro precisa ir deitado, em maca";
        }
        if (n.getOxigenio() == Oxigenio.FORNECER) {
            return "o oxigênio precisa ser fornecido pelo transporte";
        }
        if (n.getEquipe() == Equipe.ENFERMEIRO) {
            return "é preciso um(a) enfermeiro(a) no trajeto";
        }
        if (n.getEquipe() == Equipe.TECNICO) {
            return "é preciso um(a) técnico(a) de enfermagem no trajeto";
        }
        if (n.getMobilidade() == Mobilidade.CADEIRA_PERMANECE) {
            return "o passageiro fica na cadeira de rodas durante o trajeto";
        }
        if (n.getMobilidade() == Mobilidade.CADEIRA_TRANSFERE) {
            return "o passageiro passa para o banco e a cadeira dobrável vai no porta-malas";
        }
        if (n.getMobilidade() == Mobilidade.ANDA_COM_APOIO) {
            return "o passageiro caminha com apoio e precisa de ajuda no embarque";
        }
        return "o passageiro caminha sem ajuda e precisa de um motorista atencioso";
    }
}
