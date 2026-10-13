package br.com.movesaude.api.domain.model;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Guarda um conjunto de enums como texto separado por vírgula (ex.: "ASSISTIDO,ACESSIVEL"). */
abstract class EnumSetConverter<E extends Enum<E>> implements AttributeConverter<Set<E>, String> {

    private final Class<E> tipo;

    protected EnumSetConverter(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(Set<E> valores) {
        if (valores == null || valores.isEmpty()) {
            return "";
        }
        return valores.stream().sorted().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public Set<E> convertToEntityAttribute(String texto) {
        EnumSet<E> valores = EnumSet.noneOf(tipo);
        if (texto == null || texto.isBlank()) {
            return valores;
        }
        Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(s -> valores.add(Enum.valueOf(tipo, s)));
        return valores;
    }
}
