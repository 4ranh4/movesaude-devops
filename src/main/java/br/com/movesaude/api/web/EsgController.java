package br.com.movesaude.api.web;

import br.com.movesaude.api.service.EsgService;
import br.com.movesaude.api.web.dto.EsgDtos.IndicadoresEsg;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "ESG", description = "Indicadores ambientais, sociais e de governança da operação")
public class EsgController {

    private final EsgService service;

    public EsgController(EsgService service) {
        this.service = service;
    }

    @GetMapping("/api/esg/indicadores")
    @Operation(summary = "Indicadores ESG calculados a partir das corridas concluídas e da rede")
    public IndicadoresEsg indicadores() {
        return service.indicadores();
    }
}
