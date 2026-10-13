package br.com.movesaude.api.web;

import br.com.movesaude.api.service.CorridaService;
import br.com.movesaude.api.web.dto.CorridaDtos.ConfirmacaoRequest;
import br.com.movesaude.api.web.dto.CorridaDtos.CorridaRequest;
import br.com.movesaude.api.web.dto.CorridaDtos.CorridaResponse;
import br.com.movesaude.api.web.dto.CorridaDtos.OpcoesResponse;
import br.com.movesaude.api.web.dto.CorridaDtos.StatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/corridas")
@Tag(name = "Corridas", description = "Solicitação, matching da central, confirmação e acompanhamento")
public class CorridaController {

    private final CorridaService service;

    public CorridaController(CorridaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as corridas, da mais recente para a mais antiga")
    public List<CorridaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma corrida")
    public CorridaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Solicita uma corrida; a triagem indica a modalidade pelas necessidades do passageiro")
    public ResponseEntity<CorridaResponse> solicitar(@Valid @RequestBody CorridaRequest req) {
        CorridaResponse criada = service.solicitar(req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @GetMapping("/{id}/opcoes")
    @Operation(summary = "Até 3 opções compatíveis, com preço e cobertura do convênio")
    public OpcoesResponse opcoes(@PathVariable Long id) {
        return service.opcoes(id);
    }

    @PostMapping("/{id}/confirmacao")
    @Operation(summary = "Confirma a corrida com o prestador escolhido e aplica os créditos do convênio")
    public CorridaResponse confirmar(@PathVariable Long id, @Valid @RequestBody ConfirmacaoRequest req) {
        return service.confirmar(id, req.prestadorId());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Avança o status: CONFIRMADA → A_CAMINHO → EM_ANDAMENTO → CONCLUIDA (ou CANCELADA)")
    public CorridaResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.atualizarStatus(id, req.status());
    }
}
