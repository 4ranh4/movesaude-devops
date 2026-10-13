package br.com.movesaude.api.web.dto;

import br.com.movesaude.api.domain.Combustivel;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.StatusVerificacao;
import br.com.movesaude.api.domain.TipoVeiculo;
import br.com.movesaude.api.domain.model.Prestador;
import br.com.movesaude.api.domain.model.Tarifa;
import br.com.movesaude.api.domain.model.Veiculo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public final class PrestadorDtos {

    private PrestadorDtos() {
    }

    public record TarifaDto(
            @NotNull Modalidade modalidade,
            @NotNull @PositiveOrZero BigDecimal base,
            @NotNull @PositiveOrZero BigDecimal porKm) {

        static TarifaDto de(Tarifa t) {
            return new TarifaDto(t.getModalidade(), t.getBase(), t.getPorKm());
        }
    }

    public record VeiculoDto(
            Long id,
            @NotNull TipoVeiculo tipo,
            @NotBlank @Size(max = 80) String modelo,
            @NotBlank @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "placa deve seguir o padrão ABC1D23")
            String placa,
            @Min(0) @Max(20) int assentos,
            @Min(0) @Max(4) int vagasCadeira,
            boolean maca,
            boolean oxigenio,
            @NotNull Combustivel combustivel) {

        static VeiculoDto de(Veiculo v) {
            return new VeiculoDto(v.getId(), v.getTipo(), v.getModelo(), v.getPlaca(), v.getAssentos(),
                    v.getVagasCadeira(), v.isMaca(), v.isOxigenio(), v.getCombustivel());
        }
    }

    public record PrestadorRequest(
            @NotBlank @Size(max = 120) String nomeFantasia,
            @NotBlank @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "CNPJ deve seguir o padrão 00.000.000/0000-00")
            String cnpj,
            @NotBlank @Size(max = 80) String cidade,
            @NotEmpty Set<Modalidade> modalidades,
            @NotNull LocalTime horaInicio,
            @NotNull LocalTime horaFim,
            boolean possuiTecnico,
            boolean possuiEnfermeiro,
            @PositiveOrZero BigDecimal taxaOxigenio,
            @PositiveOrZero BigDecimal taxaTecnico,
            @PositiveOrZero BigDecimal taxaEnfermeiro,
            @NotEmpty List<@Valid VeiculoDto> veiculos,
            @NotEmpty List<@Valid TarifaDto> tarifas) {
    }

    public record VerificacaoRequest(@NotNull StatusVerificacao status) {
    }

    public record PrestadorResponse(
            Long id,
            String nomeFantasia,
            String cnpj,
            String cidade,
            Set<Modalidade> modalidades,
            StatusVerificacao statusVerificacao,
            boolean pausado,
            boolean possuiTecnico,
            boolean possuiEnfermeiro,
            LocalTime horaInicio,
            LocalTime horaFim,
            double avaliacao,
            int corridasConcluidas,
            List<VeiculoDto> veiculos,
            List<TarifaDto> tarifas) {

        public static PrestadorResponse de(Prestador p) {
            return new PrestadorResponse(p.getId(), p.getNomeFantasia(), p.getCnpj(), p.getCidade(),
                    Set.copyOf(p.getModalidades()), p.getStatusVerificacao(), p.isPausado(), p.isPossuiTecnico(),
                    p.isPossuiEnfermeiro(), p.getHoraInicio(), p.getHoraFim(), p.getAvaliacao(),
                    p.getCorridasConcluidas(),
                    p.getVeiculos().stream().map(VeiculoDto::de).toList(),
                    p.getTarifas().stream().map(TarifaDto::de).toList());
        }
    }
}
