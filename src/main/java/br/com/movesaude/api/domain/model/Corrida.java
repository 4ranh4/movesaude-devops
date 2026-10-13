package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.StatusCorrida;
import br.com.movesaude.api.domain.regras.Cobertura;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "corrida")
public class Corrida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passageiro_id")
    private Passageiro passageiro;

    @Column(nullable = false, length = 200)
    private String origem;

    @Column(nullable = false, length = 200)
    private String destino;

    @Column(name = "distancia_km", nullable = false)
    private double distanciaKm;

    @Column(name = "data_corrida", nullable = false)
    private LocalDate data;

    @Column(name = "hora_embarque", nullable = false)
    private LocalTime hora;

    @Column(name = "ida_e_volta", nullable = false)
    private boolean idaEVolta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Finalidade finalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_indicada", nullable = false, length = 20)
    private Modalidade modalidadeIndicada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCorrida status = StatusCorrida.EM_TRIAGEM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_atendida", length = 20)
    private Modalidade modalidadeAtendida;

    @Column(name = "horario_confirmado")
    private LocalTime horarioConfirmado;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "cliente_paga", precision = 10, scale = 2)
    private BigDecimal clientePaga;

    @Column(name = "convenio_paga", precision = 10, scale = 2)
    private BigDecimal convenioPaga;

    @Column(name = "trechos_cobertos", nullable = false)
    private int trechosCobertos;

    @Column(precision = 10, scale = 2)
    private BigDecimal comissao;

    @Column(name = "resumo_cobertura", length = 200)
    private String resumoCobertura;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected Corrida() {
    }

    public Corrida(Passageiro passageiro, String origem, String destino, double distanciaKm, LocalDate data,
                   LocalTime hora, boolean idaEVolta, Finalidade finalidade, Modalidade modalidadeIndicada) {
        this.passageiro = passageiro;
        this.origem = origem;
        this.destino = destino;
        this.distanciaKm = distanciaKm;
        this.data = data;
        this.hora = hora;
        this.idaEVolta = idaEVolta;
        this.finalidade = finalidade;
        this.modalidadeIndicada = modalidadeIndicada;
        this.criadaEm = Instant.now();
        this.atualizadaEm = this.criadaEm;
    }

    public int trechos() {
        return idaEVolta ? 2 : 1;
    }

    public void confirmar(Prestador prestador, Veiculo veiculo, Modalidade modalidade, LocalTime horario,
                          BigDecimal preco, Cobertura cobertura, BigDecimal comissao) {
        this.prestador = prestador;
        this.veiculo = veiculo;
        this.modalidadeAtendida = modalidade;
        this.horarioConfirmado = horario;
        this.preco = preco;
        this.clientePaga = cobertura.clientePaga();
        this.convenioPaga = cobertura.convenioPaga();
        this.trechosCobertos = cobertura.trechosCobertos();
        this.resumoCobertura = cobertura.resumo();
        this.comissao = comissao;
        mudarStatus(StatusCorrida.CONFIRMADA);
    }

    public void mudarStatus(StatusCorrida novo) {
        this.status = novo;
        this.atualizadaEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Passageiro getPassageiro() {
        return passageiro;
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public boolean isIdaEVolta() {
        return idaEVolta;
    }

    public Finalidade getFinalidade() {
        return finalidade;
    }

    public Modalidade getModalidadeIndicada() {
        return modalidadeIndicada;
    }

    public StatusCorrida getStatus() {
        return status;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public Modalidade getModalidadeAtendida() {
        return modalidadeAtendida;
    }

    public LocalTime getHorarioConfirmado() {
        return horarioConfirmado;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public BigDecimal getClientePaga() {
        return clientePaga;
    }

    public BigDecimal getConvenioPaga() {
        return convenioPaga;
    }

    public int getTrechosCobertos() {
        return trechosCobertos;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public String getResumoCobertura() {
        return resumoCobertura;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getAtualizadaEm() {
        return atualizadaEm;
    }
}
