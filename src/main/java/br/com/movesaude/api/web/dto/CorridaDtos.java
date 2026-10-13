package br.com.movesaude.api.web.dto;

import br.com.movesaude.api.domain.Combustivel;
import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.StatusCorrida;
import br.com.movesaude.api.domain.model.Corrida;
import br.com.movesaude.api.domain.model.Veiculo;
import br.com.movesaude.api.domain.regras.Cobertura;
import br.com.movesaude.api.domain.regras.Cotacao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class CorridaDtos {

    private CorridaDtos() {
    }

    public record CorridaRequest(
            @NotNull Long passageiroId,
            @NotBlank @Size(max = 200) String origem,
            @NotBlank @Size(max = 200) String destino,
            @Positive @DecimalMax("300") double distanciaKm,
            @NotNull @FutureOrPresent LocalDate data,
            @NotNull LocalTime hora,
            boolean idaEVolta,
            @NotNull Finalidade finalidade) {
    }

    public record ConfirmacaoRequest(@NotNull Long prestadorId) {
    }

    public record StatusRequest(@NotNull StatusCorrida status) {
    }

    public record CorridaResponse(
            Long id,
            Long passageiroId,
            String passageiro,
            String origem,
            String destino,
            double distanciaKm,
            LocalDate data,
            LocalTime hora,
            boolean idaEVolta,
            Finalidade finalidade,
            Modalidade modalidadeIndicada,
            StatusCorrida status,
            Long prestadorId,
            String prestador,
            String veiculo,
            Modalidade modalidadeAtendida,
            LocalTime horarioConfirmado,
            BigDecimal preco,
            BigDecimal clientePaga,
            BigDecimal convenioPaga,
            int trechosCobertos,
            BigDecimal comissao,
            String resumoCobertura,
            Instant criadaEm,
            Instant atualizadaEm) {

        public static CorridaResponse de(Corrida c) {
            Veiculo v = c.getVeiculo();
            return new CorridaResponse(c.getId(), c.getPassageiro().getId(), c.getPassageiro().getNome(),
                    c.getOrigem(), c.getDestino(), c.getDistanciaKm(), c.getData(), c.getHora(), c.isIdaEVolta(),
                    c.getFinalidade(), c.getModalidadeIndicada(), c.getStatus(),
                    c.getPrestador() != null ? c.getPrestador().getId() : null,
                    c.getPrestador() != null ? c.getPrestador().getNomeFantasia() : null,
                    v != null ? v.getModelo() + " (" + v.getPlaca() + ")" : null,
                    c.getModalidadeAtendida(), c.getHorarioConfirmado(), c.getPreco(), c.getClientePaga(),
                    c.getConvenioPaga(), c.getTrechosCobertos(), c.getComissao(), c.getResumoCobertura(),
                    c.getCriadaEm(), c.getAtualizadaEm());
        }
    }

    public record OpcaoResponse(
            Long prestadorId,
            String prestador,
            double avaliacao,
            Modalidade modalidade,
            Long veiculoId,
            String veiculo,
            Combustivel combustivel,
            LocalTime horario,
            boolean horarioExato,
            BigDecimal preco,
            List<Cotacao.LinhaPreco> detalhamento,
            Cobertura cobertura,
            boolean recomendada) {
    }

    public record DescartadoResponse(Long prestadorId, String prestador, List<String> motivos) {
    }

    public record OpcoesResponse(
            Long corridaId,
            Modalidade modalidadeIndicada,
            String motivoModalidade,
            List<OpcaoResponse> opcoes,
            List<DescartadoResponse> descartados) {
    }
}
