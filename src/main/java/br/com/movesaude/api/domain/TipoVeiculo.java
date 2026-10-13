package br.com.movesaude.api.domain;

/**
 * Tipos de veículo da rede. Cada tipo atende uma modalidade e tem um fator de emissão de
 * referência (kg de CO2 por km) usado para estimar o CO2 evitado pela frota limpa.
 */
public enum TipoVeiculo {
    SEDAN("Sedã confortável", Modalidade.ASSISTIDO, 0.15),
    MINIVAN("Minivan com degrau baixo", Modalidade.ASSISTIDO, 0.17),
    VAN_RAMPA("Van adaptada com rampa", Modalidade.ACESSIVEL, 0.27),
    VAN_ELEVADOR("Van adaptada com elevador", Modalidade.ACESSIVEL, 0.27),
    AMBULANCIA("Ambulância de remoção (tipo A)", Modalidade.MEDICO, 0.30);

    private final String descricao;
    private final Modalidade modalidade;
    private final double co2ReferenciaKgPorKm;

    TipoVeiculo(String descricao, Modalidade modalidade, double co2ReferenciaKgPorKm) {
        this.descricao = descricao;
        this.modalidade = modalidade;
        this.co2ReferenciaKgPorKm = co2ReferenciaKgPorKm;
    }

    public String descricao() {
        return descricao;
    }

    public Modalidade modalidade() {
        return modalidade;
    }

    public double co2ReferenciaKgPorKm() {
        return co2ReferenciaKgPorKm;
    }
}
