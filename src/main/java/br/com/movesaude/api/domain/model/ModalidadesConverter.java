package br.com.movesaude.api.domain.model;

import br.com.movesaude.api.domain.Modalidade;
import jakarta.persistence.Converter;

@Converter
public class ModalidadesConverter extends EnumSetConverter<Modalidade> {
    public ModalidadesConverter() {
        super(Modalidade.class);
    }
}
