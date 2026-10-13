package br.com.movesaude.api.web.dto;

import br.com.movesaude.api.domain.Equipe;
import br.com.movesaude.api.domain.Finalidade;
import br.com.movesaude.api.domain.Mobilidade;
import br.com.movesaude.api.domain.Modalidade;
import br.com.movesaude.api.domain.Oxigenio;
import br.com.movesaude.api.domain.model.Necessidades;
import br.com.movesaude.api.domain.model.Passageiro;
import br.com.movesaude.api.domain.model.PlanoConvenio;
import br.com.movesaude.api.domain.regras.Triagem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public final class PassageiroDtos {

    private PassageiroDtos() {
    }

    public record NecessidadesDto(
            @NotNull Mobilidade mobilidade,
            @Min(0) @Max(3) int acompanhantes,
            Oxigenio oxigenio,
            Equipe equipe,
            boolean obesidade,
            boolean caoGuia) {

        public Necessidades paraEntidade() {
            return new Necessidades(mobilidade, acompanhantes,
                    oxigenio != null ? oxigenio : Oxigenio.NAO,
                    equipe != null ? equipe : Equipe.NAO,
                    obesidade, caoGuia);
        }

        static NecessidadesDto de(Necessidades n) {
            return new NecessidadesDto(n.getMobilidade(), n.getAcompanhantes(), n.getOxigenio(), n.getEquipe(),
                    n.isObesidade(), n.isCaoGuia());
        }
    }

    public record PassageiroRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotNull @Valid NecessidadesDto necessidades,
            @Size(max = 40) String planoId,
            @Size(max = 30) String carteirinha) {
    }

    public record PlanoResponse(
            String id,
            String operadora,
            String nome,
            int creditosMensais,
            BigDecimal coparticipacaoPorTrecho,
            BigDecimal descontoPct,
            Set<Modalidade> modalidadesCobertas,
            Set<Finalidade> finalidadesElegiveis,
            Set<Modalidade> autorizacaoPara) {

        public static PlanoResponse de(PlanoConvenio p) {
            return new PlanoResponse(p.getId(), p.getOperadora(), p.getNome(), p.getCreditosMensais(),
                    p.getCoparticipacaoPorTrecho(), p.getDescontoPct(), Set.copyOf(p.getModalidadesCobertas()),
                    Set.copyOf(p.getFinalidadesElegiveis()), Set.copyOf(p.getAutorizacaoPara()));
        }
    }

    public record PassageiroResponse(
            Long id,
            String nome,
            NecessidadesDto necessidades,
            Modalidade modalidadeIndicada,
            String motivoModalidade,
            String planoId,
            String plano,
            String carteirinha) {

        public static PassageiroResponse de(Passageiro p) {
            PlanoConvenio plano = p.getPlano();
            return new PassageiroResponse(p.getId(), p.getNome(), NecessidadesDto.de(p.getNecessidades()),
                    Triagem.modalidadeIndicada(p.getNecessidades()), Triagem.motivo(p.getNecessidades()),
                    plano != null ? plano.getId() : null,
                    plano != null ? plano.getOperadora() + " · " + plano.getNome() : null,
                    p.getCarteirinha());
        }
    }

    public record CreditosResponse(
            Long passageiroId,
            String mes,
            String plano,
            int creditosMensais,
            int usados,
            int restantes) {
    }
}
