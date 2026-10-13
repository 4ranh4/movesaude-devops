package br.com.movesaude.api.web;

import br.com.movesaude.api.service.PassageiroService;
import br.com.movesaude.api.web.dto.PassageiroDtos.CreditosResponse;
import br.com.movesaude.api.web.dto.PassageiroDtos.PassageiroRequest;
import br.com.movesaude.api.web.dto.PassageiroDtos.PassageiroResponse;
import br.com.movesaude.api.web.dto.PassageiroDtos.PlanoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "Passageiros e convênios", description = "Necessidades de acessibilidade e benefício mobilidade")
public class PassageiroController {

    private final PassageiroService service;

    public PassageiroController(PassageiroService service) {
        this.service = service;
    }

    @GetMapping("/api/passageiros")
    @Operation(summary = "Lista os passageiros")
    public List<PassageiroResponse> listar() {
        return service.listar();
    }

    @GetMapping("/api/passageiros/{id}")
    @Operation(summary = "Detalha um passageiro e a modalidade indicada pela triagem")
    public PassageiroResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/api/passageiros")
    @Operation(summary = "Cadastra um passageiro com as necessidades de acessibilidade")
    public ResponseEntity<PassageiroResponse> criar(@Valid @RequestBody PassageiroRequest req) {
        PassageiroResponse criado = service.criar(req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @GetMapping("/api/passageiros/{id}/creditos")
    @Operation(summary = "Créditos do convênio usados e restantes no mês (formato AAAA-MM)")
    public CreditosResponse creditos(@PathVariable Long id, @RequestParam(required = false) YearMonth mes) {
        return service.creditos(id, mes != null ? mes : YearMonth.now());
    }

    @GetMapping("/api/convenios/planos")
    @Operation(summary = "Planos de convênio parceiros (fictícios) e regras do benefício")
    public List<PlanoResponse> planos() {
        return service.planos();
    }
}
