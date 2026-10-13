package br.com.movesaude.api.web;

import br.com.movesaude.api.config.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;

@RestController
@RequestMapping("/api/info")
@Tag(name = "Info", description = "Versão, ambiente e banco em uso (usado pelo pipeline para validar o deploy)")
public class InfoController {

    private final AppProperties props;
    private final ObjectProvider<BuildProperties> build;
    private final DataSource dataSource;
    private final Instant iniciadaEm = Instant.now();

    public InfoController(AppProperties props, ObjectProvider<BuildProperties> build, DataSource dataSource) {
        this.props = props;
        this.build = build;
        this.dataSource = dataSource;
    }

    public record InfoResponse(String aplicacao, String versao, String ambiente, String commit, String banco,
                               Instant iniciadaEm, Instant agora) {
    }

    @GetMapping
    @Operation(summary = "Informações da instância em execução")
    public InfoResponse info() {
        BuildProperties b = build.getIfAvailable();
        return new InfoResponse("MoveSaúde API", b != null ? b.getVersion() : "dev", props.ambiente(), props.commit(),
                banco(), iniciadaEm, Instant.now());
    }

    private String banco() {
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            return md.getDatabaseProductName() + " " + md.getDatabaseProductVersion();
        } catch (SQLException e) {
            return "indisponível";
        }
    }
}
