package dev.lucasferraz.bardolindomarapi;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;

class ProdutoReadIntegrationTest extends PostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM produtos");
        jdbc.update("INSERT INTO produtos (nome, descricao, preco, categoria_id) VALUES ('Alpha', '350 ml', 5.50, 1), ('Beta', null, 2.00, 3), ('Beta', null, 3.00, 1), ('100% Suco', null, 4.00, 1)");
    }

    @AfterEach
    void cleanup() { jdbc.update("DELETE FROM produtos"); }

    @Test
    void consultaPublicaRetornaProdutoOu404() throws Exception {
        Long id = jdbc.queryForObject("SELECT id FROM produtos WHERE nome = 'Alpha'", Long.class);
        var response = request("GET", "/produtos/" + id, null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(json(response).get("nome").asString()).isEqualTo("Alpha");
        assertThat(json(response).get("preco").decimalValue()).isEqualByComparingTo("5.50");
        assertThat(request("GET", "/produtos/999999", null, null).statusCode()).isEqualTo(404);
        assertThat(request("GET", "/produtos/abc", null, null).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/produtos/0", null, null).statusCode()).isEqualTo(400);
    }

    @Test
    void paginaOrdenadaTemContratoEstavel() throws Exception {
        var first = request("GET", "/produtos?page=0&size=2", null, null);
        assertThat(first.statusCode()).isEqualTo(200);
        var body = json(first);
        assertThat(body.get("page").asInt()).isZero();
        assertThat(body.get("size").asInt()).isEqualTo(2);
        assertThat(body.get("totalElements").asLong()).isEqualTo(4);
        assertThat(body.get("totalPages").asInt()).isEqualTo(2);
        assertThat(body.get("items").get(0).get("nome").asString()).isEqualTo("100% Suco");
        assertThat(body.get("items").get(1).get("nome").asString()).isEqualTo("Alpha");
        var second = json(request("GET", "/produtos?page=1&size=2", null, null));
        assertThat(second.get("items").size()).isEqualTo(2);
        assertThat(second.get("items").get(0).get("id").asLong()).isLessThan(second.get("items").get(1).get("id").asLong());
        var defaults = json(request("GET", "/produtos", null, null));
        assertThat(defaults.get("size").asInt()).isEqualTo(20);
        var beyond = json(request("GET", "/produtos?page=99", null, null));
        assertThat(beyond.get("items").size()).isZero();
        assertThat(beyond.get("totalElements").asLong()).isEqualTo(4);
    }

    @Test
    void pesquisaCombinaFiltrosEEscapaCuringas() throws Exception {
        var name = json(request("GET", "/produtos?nome=bEt", null, null));
        assertThat(name.get("items").size()).isEqualTo(2);
        var category = json(request("GET", "/produtos?categoria=DOCES", null, null));
        assertThat(category.get("items").size()).isEqualTo(1);
        var combined = json(request("GET", "/produtos?nome=bet&categoria=BEBIDAS", null, null));
        assertThat(combined.get("items").size()).isEqualTo(1);
        var literal = json(request("GET", "/produtos?nome=%25", null, null));
        assertThat(literal.get("items").size()).isEqualTo(1);
        var empty = json(request("GET", "/produtos?nome=inexistente", null, null));
        assertThat(empty.get("items").size()).isZero();
        assertThat(empty.get("totalElements").asLong()).isZero();
    }

    @Test
    void parametrosInvalidosRetornam400() throws Exception {
        for (String query : new String[]{"page=-1", "size=0", "size=101", "page=abc", "categoria=INVALIDA", "nome=" + "x".repeat(121)}) {
            var response = request("GET", "/produtos?" + query, null, null);
            assertThat(response.statusCode()).as(query + " " + response.body()).isEqualTo(400);
            assertThat(response.headers().firstValue("content-type").orElseThrow()).contains("application/problem+json");
        }
    }

    @Test
    void openApiDocumentaRotasSegurancaEDtos() throws Exception {
        var response = request("GET", "/v3/api-docs", null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        var spec = json(response);
        var paths = spec.get("paths");
        assertThat(paths.get("/auth/login").has("post")).isTrue();
        assertThat(paths.get("/categorias").has("get")).isTrue();
        assertThat(paths.get("/produtos").has("get")).isTrue();
        assertThat(paths.get("/produtos/{id}").has("delete")).isTrue();
        assertThat(paths.get("/produtos").get("post").get("security").get(0).has("bearerAuth")).isTrue();
        assertThat(paths.get("/produtos").get("get").has("security")).isFalse();
        assertThat(spec.get("components").get("securitySchemes").get("bearerAuth").get("scheme").asString()).isEqualTo("bearer");
        assertThat(request("GET", "/swagger-ui/index.html", null, null).statusCode()).isEqualTo(200);
    }
}
