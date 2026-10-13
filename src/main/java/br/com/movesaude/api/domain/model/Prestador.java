package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.StatusVerificacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Empresa parceira que executa as corridas (a MoveSaúde não tem frota própria). */
@Entity
@Table(name = "prestador")
public class Prestador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_fantasia", nullable = false, length = 120)
    private String nomeFantasia;

    @Column(nullable = false, length = 18, unique = true)
    private String cnpj;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Convert(converter = ModalidadesConverter.class)
    @Column(nullable = false, length = 60)
    private Set<Modalidade> modalidades = EnumSet.noneOf(Modalidade.class);

    @Enumerated(EnumType.STRING)
    @Column(name = "status_verificacao", nullable = false, length = 20)
    private StatusVerificacao statusVerificacao = StatusVerificacao.EM_ANALISE;

    @Column(nullable = false)
    private boolean pausado;

    @Column(name = "possui_tecnico", nullable = false)
    private boolean possuiTecnico;

    @Column(name = "possui_enfermeiro", nullable = false)
    private boolean possuiEnfermeiro;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "taxa_oxigenio", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaOxigenio = BigDecimal.ZERO;

    @Column(name = "taxa_tecnico", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaTecnico = BigDecimal.ZERO;

    @Column(name = "taxa_enfermeiro", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEnfermeiro = BigDecimal.ZERO;

    @Column(nullable = false)
    private double avaliacao = 5.0;

    @Column(name = "corridas_concluidas", nullable = false)
    private int corridasConcluidas;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Veiculo> veiculos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "prestador_tarifa", joinColumns = @JoinColumn(name = "prestador_id"))
    private List<Tarifa> tarifas = new ArrayList<>();

    protected Prestador() {
    }

    public Prestador(String nomeFantasia, String cnpj, String cidade, Set<Modalidade> modalidades,
                     LocalTime horaInicio, LocalTime horaFim) {
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.cidade = cidade;
        this.modalidades = modalidades.isEmpty() ? EnumSet.noneOf(Modalidade.class) : EnumSet.copyOf(modalidades);
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public void adicionarVeiculo(Veiculo veiculo) {
        veiculo.vincular(this);
        veiculos.add(veiculo);
    }

    public void adicionarTarifa(Tarifa tarifa) {
        tarifas.add(tarifa);
    }

    public Optional<Tarifa> tarifaPara(Modalidade modalidade) {
        return tarifas.stream().filter(t -> t.getModalidade() == modalidade).findFirst();
    }

    public boolean verificado() {
        return statusVerificacao == StatusVerificacao.VERIFICADO;
    }

    public void registrarCorridaConcluida() {
        corridasConcluidas++;
    }

    public void definirEquipe(boolean possuiTecnico, boolean possuiEnfermeiro) {
        this.possuiTecnico = possuiTecnico;
        this.possuiEnfermeiro = possuiEnfermeiro;
    }

    public void definirTaxas(BigDecimal oxigenio, BigDecimal tecnico, BigDecimal enfermeiro) {
        this.taxaOxigenio = oxigenio;
        this.taxaTecnico = tecnico;
        this.taxaEnfermeiro = enfermeiro;
    }

    public void definirVerificacao(StatusVerificacao status) {
        this.statusVerificacao = status;
    }

    public void definirPausado(boolean pausado) {
        this.pausado = pausado;
    }

    public void definirReputacao(double avaliacao, int corridasConcluidas) {
        this.avaliacao = avaliacao;
        this.corridasConcluidas = corridasConcluidas;
    }

    public Long getId() {
        return id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getCidade() {
        return cidade;
    }

    public Set<Modalidade> getModalidades() {
        return modalidades;
    }

    public StatusVerificacao getStatusVerificacao() {
        return statusVerificacao;
    }

    public boolean isPausado() {
        return pausado;
    }

    public boolean isPossuiTecnico() {
        return possuiTecnico;
    }

    public boolean isPossuiEnfermeiro() {
        return possuiEnfermeiro;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public BigDecimal getTaxaOxigenio() {
        return taxaOxigenio;
    }

    public BigDecimal getTaxaTecnico() {
        return taxaTecnico;
    }

    public BigDecimal getTaxaEnfermeiro() {
        return taxaEnfermeiro;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public int getCorridasConcluidas() {
        return corridasConcluidas;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }
}
