package br.com.movesaude.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "passageiro")
public class Passageiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Embedded
    private Necessidades necessidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private PlanoConvenio plano;

    @Column(length = 30)
    private String carteirinha;

    protected Passageiro() {
    }

    public Passageiro(String nome, Necessidades necessidades, PlanoConvenio plano, String carteirinha) {
        this.nome = nome;
        this.necessidades = necessidades;
        this.plano = plano;
        this.carteirinha = carteirinha;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Necessidades getNecessidades() {
        return necessidades;
    }

    public PlanoConvenio getPlano() {
        return plano;
    }

    public String getCarteirinha() {
        return carteirinha;
    }
}
