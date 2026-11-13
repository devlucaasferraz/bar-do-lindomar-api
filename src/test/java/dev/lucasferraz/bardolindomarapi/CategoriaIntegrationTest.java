package dev.lucasferraz.bardolindomarapi;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class CategoriaIntegrationTest extends PostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void categoriasSaoPublicasEIncluemEnumDefinido() throws Exception {
        var response = request("GET", "/categorias", null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        var body = json(response);
        assertThat(body.size()).isEqualTo(4);
        var codes = new HashSet<String>();
        body.forEach(item -> {
            assertThat(item.get("id").asLong()).isPositive();
            assertThat(item.get("nome").asString()).isNotBlank();
            codes.add(item.get("codigo").asString());
        });
        assertThat(codes).containsExactlyInAnyOrder("BEBIDAS", "BEBIDAS_ALCOOLICAS", "DOCES", "SALGADOS");
    }

    @Test
    void bancoImpedeCodigoDuplicado() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO categorias (codigo, nome) VALUES ('DOCES', 'Duplicado')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
