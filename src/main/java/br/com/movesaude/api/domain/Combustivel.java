package br.com.movesaude.api.domain;

/** Combustível do veículo, com fator de emissão estimado (kg de CO2 por km). */
public enum Combustivel {
    FLEX(0.15, false),
    DIESEL(0.27, false),
    HIBRIDO(0.09, true),
    ELETRICO(0.02, true);

    private final double co2KgPorKm;
    private final boolean limpo;

    Combustivel(double co2KgPorKm, boolean limpo) {
        this.co2KgPorKm = co2KgPorKm;
        this.limpo = limpo;
    }

    public double co2KgPorKm() {
        return co2KgPorKm;
    }

    /** Elétricos e híbridos contam como frota limpa nos indicadores ESG. */
    public boolean limpo() {
        return limpo;
    }
}
