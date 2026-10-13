package br.com.movesaude.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Configurações da aplicação, vindas de variáveis de ambiente.
 *
 * @param ambiente nome do ambiente (local, staging, production), exibido em /api/info
 * @param commit   commit em execução, usado pelo pipeline para confirmar o deploy
 * @param comissao taxa cobrada do prestador sobre cada corrida (0.18 = 18%)
 */
@ConfigurationProperties(prefix = "movesaude")
public record AppProperties(String ambiente, String commit, BigDecimal comissao) {

    public AppProperties {
        // A imagem construída sem GIT_SHA (ex.: build feito pelo Render) deixa APP_COMMIT vazio;
        // nesse caso vale o commit que o Render injeta em tempo de execução.
        if (commit == null || commit.isBlank()) {
            String render = System.getenv("RENDER_GIT_COMMIT");
            commit = render != null && !render.isBlank() ? render : "desconhecido";
        }
    }
}
