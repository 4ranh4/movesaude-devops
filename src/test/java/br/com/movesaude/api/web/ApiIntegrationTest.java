package br.com.movesaude.api.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração: sobem a aplicação inteira com H2, aplicam as migrations do Flyway
 * (estrutura + dados de demonstração) e exercitam a API pela camada HTTP.
 * Cada teste roda em uma transação desfeita no final.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private long criarCorridaDaLucia() throws Exception {
        String dia = LocalDate.now().plusDays(3).toString();
        String resposta = mvc.perform(post("/api/corridas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"passageiroId": %d, "origem": "Rua Voluntários da Pátria, 1200", "destino": "Clínica Renal Norte",
                         "distanciaKm": 8.4, "data": "%s", "hora": "10:00", "idaEVolta": true, "finalidade": "HEMODIALISE"}
                        """.formatted(idPassageiro("Lúcia Ferreira"), dia)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("EM_TRIAGEM"))
                .andExpect(jsonPath("$.modalidadeIndicada").value("ACESSIVEL"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long idPassageiro(String nome) throws Exception {
        String lista = mvc.perform(get("/api/passageiros")).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.<java.util.List<Object>>read(lista, "$[?(@.nome == '" + nome + "')].id").getFirst())
                .longValue();
    }

    @Test
    void infoMostraAmbienteEBanco() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao").value("MoveSaúde API"))
                .andExpect(jsonPath("$.ambiente").value("test"))
                .andExpect(jsonPath("$.commit").value("teste"))
                .andExpect(jsonPath("$.banco").value(containsString("H2")));
    }

    @Test
    void healthCheckEstaUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void listaPrestadoresDaRedeComFrotaETarifas() throws Exception {
        mvc.perform(get("/api/prestadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nomeFantasia").value("Acessa Vans"))
                .andExpect(jsonPath("$[0].veiculos", hasSize(2)))
                .andExpect(jsonPath("$[0].tarifas[0].modalidade").value("ACESSIVEL"));
    }

    @Test
    void prestadorInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/prestadores/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Não encontrado"));
    }

    @Test
    void cadastraPrestadorQueEntraEmAnalise() throws Exception {
        mvc.perform(post("/api/prestadores").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nomeFantasia": "Leva Bem", "cnpj": "67.890.123/0001-06", "cidade": "Santo André",
                         "modalidades": ["ASSISTIDO"], "horaInicio": "07:00", "horaFim": "19:00",
                         "veiculos": [{"tipo": "SEDAN", "modelo": "BYD Seal", "placa": "LVB1A23", "assentos": 4,
                                       "vagasCadeira": 0, "maca": false, "oxigenio": false, "combustivel": "ELETRICO"}],
                         "tarifas": [{"modalidade": "ASSISTIDO", "base": 22, "porKm": 2.6}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusVerificacao").value("EM_ANALISE"))
                .andExpect(jsonPath("$.veiculos[0].combustivel").value("ELETRICO"));
    }

    @Test
    void cadastroInvalidoRetorna400ComOsCampos() throws Exception {
        mvc.perform(post("/api/prestadores").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nomeFantasia": "", "cnpj": "123", "cidade": "SP", "modalidades": [],
                         "horaInicio": "07:00", "horaFim": "19:00", "veiculos": [], "tarifas": []}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros", hasItem(containsString("cnpj"))))
                .andExpect(jsonPath("$.erros", hasItem(containsString("nomeFantasia"))));
    }

    @Test
    void creditosDoConvenioNoMes() throws Exception {
        mvc.perform(get("/api/passageiros/{id}/creditos", idPassageiro("Lúcia Ferreira")).param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditosMensais").value(30))
                .andExpect(jsonPath("$.usados").value(2))
                .andExpect(jsonPath("$.restantes").value(28));
    }

    @Test
    void fluxoCompletoDaCorridaAcessivel() throws Exception {
        long corrida = criarCorridaDaLucia();

        // Matching: só vans acessíveis verificadas; a "Rota Inclusiva" (em análise) é descartada
        mvc.perform(get("/api/corridas/{id}/opcoes", corrida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opcoes", hasSize(2)))
                .andExpect(jsonPath("$.opcoes[*].modalidade", everyItem(org.hamcrest.Matchers.is("ACESSIVEL"))))
                .andExpect(jsonPath("$.opcoes[0].recomendada").value(true))
                .andExpect(jsonPath("$.opcoes[0].cobertura.clientePaga").value(0))
                .andExpect(jsonPath("$.opcoes[*].prestador", not(hasItem("Rota Inclusiva"))))
                .andExpect(jsonPath("$.descartados[?(@.prestador == 'Rota Inclusiva')].motivos[0]")
                        .value(hasItem("Cadastro ainda não verificado")));

        String opcoes = mvc.perform(get("/api/corridas/{id}/opcoes", corrida)).andReturn().getResponse().getContentAsString();
        long prestador = ((Number) JsonPath.read(opcoes, "$.opcoes[0].prestadorId")).longValue();

        mvc.perform(post("/api/corridas/{id}/confirmacao", corrida).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prestadorId\": " + prestador + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.trechosCobertos").value(2))
                .andExpect(jsonPath("$.clientePaga").value(0));

        for (String s : new String[]{"A_CAMINHO", "EM_ANDAMENTO", "CONCLUIDA"}) {
            mvc.perform(patch("/api/corridas/{id}/status", corrida).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"" + s + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(s));
        }

        mvc.perform(patch("/api/corridas/{id}/status", corrida).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"A_CAMINHO\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value(containsString("Transição inválida")));

        mvc.perform(get("/api/esg/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.social.corridasConcluidas").value(5))
                .andExpect(jsonPath("$.social.corridasTratamentoContinuo").value(2));
    }

    @Test
    void naoConfirmaComPrestadorIncompativel() throws Exception {
        long corrida = criarCorridaDaLucia();
        String lista = mvc.perform(get("/api/prestadores")).andReturn().getResponse().getContentAsString();
        long sedan = ((Number) JsonPath.<java.util.List<Object>>read(lista,
                "$[?(@.nomeFantasia == 'Mobi Saúde Express')].id").getFirst()).longValue();

        mvc.perform(post("/api/corridas/{id}/confirmacao", corrida).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prestadorId\": " + sedan + "}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value(containsString("Não oferece transporte acessível")));
    }

    @Test
    void indicadoresEsgDosDadosDeDemonstracao() throws Exception {
        mvc.perform(get("/api/esg/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.social.corridasConcluidas").value(4))
                .andExpect(jsonPath("$.social.corridasAcessiveisOuMedicas").value(2))
                .andExpect(jsonPath("$.social.passageirosAtendidos").value(4))
                .andExpect(jsonPath("$.ambiental.veiculosNaRede").value(8))
                .andExpect(jsonPath("$.ambiental.frotaLimpaPct").value(37.5))
                .andExpect(jsonPath("$.governanca.prestadoresVerificados").value(4))
                .andExpect(jsonPath("$.governanca.prestadoresVerificadosPct").value(80.0));
    }

    @Test
    void documentacaoOpenApiDisponivel() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/corridas']").exists());
    }
}
