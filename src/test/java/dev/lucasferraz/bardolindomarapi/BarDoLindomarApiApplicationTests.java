package dev.lucasferraz.bardolindomarapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;

class BarDoLindomarApiApplicationTests extends PostgresIntegrationTest {
    @Autowired DataSource dataSource;

    @Test
    void carregaContextoComPostgresql() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
    }
}
