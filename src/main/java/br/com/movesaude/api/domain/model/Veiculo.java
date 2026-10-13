package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Combustivel;
import br.com.movesaude.api.domain.TipoVeiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVeiculo tipo;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(nullable = false, length = 10, unique = true)
    private String placa;

    /** Assentos de passageiro, sem contar o motorista. */
    @Column(nullable = false)
    private int assentos;

    @Column(name = "vagas_cadeira", nullable = false)
    private int vagasCadeira;

    @Column(nullable = false)
    private boolean maca;

    @Column(nullable = false)
    private boolean oxigenio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Combustivel combustivel;

    protected Veiculo() {
    }

    public Veiculo(TipoVeiculo tipo, String modelo, String placa, int assentos, int vagasCadeira,
                   boolean maca, boolean oxigenio, Combustivel combustivel) {
        this.tipo = tipo;
        this.modelo = modelo;
        this.placa = placa;
        this.assentos = assentos;
        this.vagasCadeira = vagasCadeira;
        this.maca = maca;
        this.oxigenio = oxigenio;
        this.combustivel = combustivel;
    }

    void vincular(Prestador prestador) {
        this.prestador = prestador;
    }

    public Long getId() {
        return id;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public String getModelo() {
        return modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public int getAssentos() {
        return assentos;
    }

    public int getVagasCadeira() {
        return vagasCadeira;
    }

    public boolean isMaca() {
        return maca;
    }

    public boolean isOxigenio() {
        return oxigenio;
    }

    public Combustivel getCombustivel() {
        return combustivel;
    }
}
