package br.com.movesaude.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Título, versão e descrição exibidos no Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI movesaudeOpenApi(AppProperties props, ObjectProvider<BuildProperties> build) {
        BuildProperties b = build.getIfAvailable();
        return new OpenAPI().info(new Info()
                .title("MoveSaúde API · " + props.ambiente())
                .version(b != null ? b.getVersion() : "dev")
                .description("Mobilidade acessível e transporte de saúde (Cidades ESG Inteligentes). "
                        + "Triagem da modalidade, matching com prestadores verificados, créditos do convênio "
                        + "e indicadores ESG. Dados de demonstração fictícios.")
                .license(new License().name("Projeto acadêmico FIAP · 2TDS")));
    }
}
