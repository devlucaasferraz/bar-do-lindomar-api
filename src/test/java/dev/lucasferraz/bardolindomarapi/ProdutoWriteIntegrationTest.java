package dev.lucasferraz.bardolindomarapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;

class ProdutoWriteIntegrationTest extends PostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void adminCriaAtualizaERemoveProduto() throws Exception {
        var token = adminToken();
        var created = request("POST", "/produtos", produto("Produto teste", "5.50", "1"), token);
        assertThat(created.statusCode()).isEqualTo(201);
        var body = json(created);
        long id = body.get("id").asLong();
        assertThat(created.headers().firstValue("location").orElseThrow()).endsWith("/produtos/" + id);
        assertThat(body.get("categoria").get("codigo").asString()).isEqualTo("BEBIDAS");
        assertThat(body.get("preco").decimalValue()).isEqualByComparingTo("5.50");
        var updated = request("PUT", "/produtos/" + id, produto("Nome alterado", "0", "3"), token);
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(json(updated).get("id").asLong()).isEqualTo(id);
        assertThat(json(updated).get("nome").asString()).isEqualTo("Nome alterado");
        assertThat(json(updated).get("descricao").isNull()).isTrue();
        assertThat(json(updated).get("categoria").get("codigo").asString()).isEqualTo("DOCES");
        var deleted = request("DELETE", "/produtos/" + id, null, token);
        assertThat(deleted.statusCode()).isEqualTo(204);
        assertThat(deleted.body()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM produtos WHERE id = ?", Long.class, id)).isZero();
        assertThat(request("DELETE", "/produtos/" + id, null, token).statusCode()).isEqualTo(404);
        assertThat(request("PUT", "/produtos/" + id, produto("Ausente", "1", "1"), token).statusCode()).isEqualTo(404);
    }

    @Test
    void validaDadosCategoriaEIdentificadores() throws Exception {
        var token = adminToken();
        for (var price : new String[]{"-1", "1.001", "10000000000", "null"}) {
            var response = request("POST", "/produtos", produto("Teste", price, "1"), token);
            assertThat(response.statusCode()).as(response.body()).isEqualTo(400);
            assertThat(json(response).get("errors").size()).isPositive();
        }
        assertThat(request("POST", "/produtos", produto(" ", "1", "1"), token).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/produtos", produto("x".repeat(121), "1", "1"), token).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/produtos", produto("Teste", "1", "null"), token).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/produtos", produto("Teste", "1", "99999"), token).statusCode()).isEqualTo(404);
        assertThat(request("DELETE", "/produtos/abc", null, token).statusCode()).isEqualTo(400);
        assertThat(request("DELETE", "/produtos/0", null, token).statusCode()).isEqualTo(400);
    }

    @Test
    void bancoProtegePrecoEChaveEstrangeira() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO produtos (nome, preco, categoria_id) VALUES ('Inválido', -1, 1)"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO produtos (nome, preco, categoria_id) VALUES ('Inválido', 1, 99999)"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private String produto(String nome, String preco, String categoria) {
        return "{\"nome\":\"" + nome + "\",\"preco\":" + preco + ",\"categoriaId\":" + categoria + "}";
    }
}
