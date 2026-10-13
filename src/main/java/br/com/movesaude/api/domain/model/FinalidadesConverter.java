package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Finalidade;
import jakarta.persistence.Converter;

@Converter
public class FinalidadesConverter extends EnumSetConverter<Finalidade> {
    public FinalidadesConverter() {
        super(Finalidade.class);
    }
}
