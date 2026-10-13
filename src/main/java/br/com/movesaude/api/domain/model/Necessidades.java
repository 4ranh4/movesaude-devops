package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Oxigenio;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Necessidades de acessibilidade registradas uma vez no cadastro do passageiro. */
@Embeddable
public class Necessidades {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Mobilidade mobilidade;

    @Column(nullable = false)
    private int acompanhantes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Oxigenio oxigenio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Equipe equipe;

    @Column(nullable = false)
    private boolean obesidade;

    @Column(name = "cao_guia", nullable = false)
    private boolean caoGuia;

    protected Necessidades() {
    }

    public Necessidades(Mobilidade mobilidade, int acompanhantes, Oxigenio oxigenio, Equipe equipe,
                        boolean obesidade, boolean caoGuia) {
        this.mobilidade = mobilidade;
        this.acompanhantes = acompanhantes;
        this.oxigenio = oxigenio;
        this.equipe = equipe;
        this.obesidade = obesidade;
        this.caoGuia = caoGuia;
    }

    public static Necessidades simples(Mobilidade mobilidade) {
        return new Necessidades(mobilidade, 0, Oxigenio.NAO, Equipe.NAO, false, false);
    }

    public Mobilidade getMobilidade() {
        return mobilidade;
    }

    public int getAcompanhantes() {
        return acompanhantes;
    }

    public Oxigenio getOxigenio() {
        return oxigenio;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public boolean isObesidade() {
        return obesidade;
    }

    public boolean isCaoGuia() {
        return caoGuia;
    }
}
