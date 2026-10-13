package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Modalidade;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Benefício mobilidade oferecido por uma operadora de saúde (modelo B2B2C).
 * Os créditos mensais são contados em trechos: ida e volta usa 2 créditos.
 */
@Entity
@Table(name = "plano_convenio")
public class PlanoConvenio {

    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false, length = 80)
    private String operadora;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "creditos_mensais", nullable = false)
    private int creditosMensais;

    @Column(name = "coparticipacao_por_trecho", nullable = false, precision = 10, scale = 2)
    private BigDecimal coparticipacaoPorTrecho;

    /** Desconto (0 a 1) aplicado aos trechos que não são cobertos por crédito. */
    @Column(name = "desconto_pct", nullable = false, precision = 4, scale = 2)
    private BigDecimal descontoPct;

    @Convert(converter = ModalidadesConverter.class)
    @Column(name = "modalidades_cobertas", nullable = false, length = 60)
    private Set<Modalidade> modalidadesCobertas;

    @Convert(converter = FinalidadesConverter.class)
    @Column(name = "finalidades_elegiveis", nullable = false, length = 200)
    private Set<Finalidade> finalidadesElegiveis;

    /** Modalidades que exigem autorização prévia (pedido médico) para usar créditos. */
    @Convert(converter = ModalidadesConverter.class)
    @Column(name = "autorizacao_para", nullable = false, length = 60)
    private Set<Modalidade> autorizacaoPara;

    protected PlanoConvenio() {
    }

    public PlanoConvenio(String id, String operadora, String nome, int creditosMensais,
                         BigDecimal coparticipacaoPorTrecho, BigDecimal descontoPct,
                         Set<Modalidade> modalidadesCobertas, Set<Finalidade> finalidadesElegiveis,
                         Set<Modalidade> autorizacaoPara) {
        this.id = id;
        this.operadora = operadora;
        this.nome = nome;
        this.creditosMensais = creditosMensais;
        this.coparticipacaoPorTrecho = coparticipacaoPorTrecho;
        this.descontoPct = descontoPct;
        this.modalidadesCobertas = modalidadesCobertas;
        this.finalidadesElegiveis = finalidadesElegiveis;
        this.autorizacaoPara = autorizacaoPara;
    }

    public String getId() {
        return id;
    }

    public String getOperadora() {
        return operadora;
    }

    public String getNome() {
        return nome;
    }

    public int getCreditosMensais() {
        return creditosMensais;
    }

    public BigDecimal getCoparticipacaoPorTrecho() {
        return coparticipacaoPorTrecho;
    }

    public BigDecimal getDescontoPct() {
        return descontoPct;
    }

    public Set<Modalidade> getModalidadesCobertas() {
        return modalidadesCobertas;
    }

    public Set<Finalidade> getFinalidadesElegiveis() {
        return finalidadesElegiveis;
    }

    public Set<Modalidade> getAutorizacaoPara() {
        return autorizacaoPara;
    }
}
