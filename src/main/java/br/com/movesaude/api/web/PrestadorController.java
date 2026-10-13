package br.com.movesaude.api.web;

import br.com.movesaude.api.service.PrestadorService;
import br.com.movesaude.api.web.dto.PrestadorDtos.PrestadorRequest;
import br.com.movesaude.api.web.dto.PrestadorDtos.PrestadorResponse;
import br.com.movesaude.api.web.dto.PrestadorDtos.VerificacaoRequest;
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
@RequestMapping("/api/prestadores")
@Tag(name = "Prestadores", description = "Rede parceira: vans adaptadas, transporte assistido e remoção")
public class PrestadorController {

    private final PrestadorService service;

    public PrestadorController(PrestadorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os prestadores da rede")
    public List<PrestadorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um prestador, com frota e tabela de preços")
    public PrestadorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um prestador (entra em análise até a verificação)")
    public ResponseEntity<PrestadorResponse> criar(@Valid @RequestBody PrestadorRequest req) {
        PrestadorResponse criado = service.criar(req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @PatchMapping("/{id}/verificacao")
    @Operation(summary = "Registra o resultado da verificação de documentos")
    public PrestadorResponse verificar(@PathVariable Long id, @Valid @RequestBody VerificacaoRequest req) {
        return service.atualizarVerificacao(id, req.status());
    }
}
