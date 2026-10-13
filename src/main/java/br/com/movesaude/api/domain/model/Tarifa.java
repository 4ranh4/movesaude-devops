package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Modalidade;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

/** Tabela de preço do prestador para uma modalidade: tarifa base + valor por km, por trecho. */
@Embeddable
public class Tarifa {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal base;

    @Column(name = "por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal porKm;

    protected Tarifa() {
    }

    public Tarifa(Modalidade modalidade, BigDecimal base, BigDecimal porKm) {
        this.modalidade = modalidade;
        this.base = base;
        this.porKm = porKm;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public BigDecimal getBase() {
        return base;
    }

    public BigDecimal getPorKm() {
        return porKm;
    }
}
