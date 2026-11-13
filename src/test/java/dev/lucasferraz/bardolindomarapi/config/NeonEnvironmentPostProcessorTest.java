package dev.lucasferraz.bardolindomarapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class NeonEnvironmentPostProcessorTest {
    @Test
    void converteCredenciaisSemExpoLasNaUrlJdbc() {
        var connection = NeonEnvironmentPostProcessor.connection(
                "postgresql://user%40neon:p%40ss%3Aword%2Bmore@ep-demo-pooler.neon.tech/neondb?sslmode=require&channel_binding=require");
        assertThat(connection.username()).isEqualTo("user@neon");
        assertThat(connection.password()).isEqualTo("p@ss:word+more");
        assertThat(connection.jdbcUrl()).isEqualTo("jdbc:postgresql://ep-demo-pooler.neon.tech/neondb?sslmode=require&channelBinding=require");
        assertThat(connection.toString()).doesNotContain("user@neon", "p@ss");
    }

    @Test
    void separaPoolingEMigrationsEPreservaConfiguracaoExplicita() {
        var environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://app:secret@ep-demo-pooler.neon.tech/neondb?sslmode=require")
                .withProperty("DATABASE_URL_UNPOOLED", "postgresql://migration:direct@ep-demo.neon.tech/neondb?sslmode=require")
                .withProperty("DB_USER", "usuario-explicito")
                .withProperty("DB_URL", "")
                .withProperty("DB_MIGRATIONS_URL", "");
        new NeonEnvironmentPostProcessor().postProcessEnvironment(environment, null);
        assertThat(environment.getProperty("DB_URL")).contains("ep-demo-pooler.neon.tech").doesNotContain("secret");
        assertThat(environment.getProperty("DB_USER")).isEqualTo("usuario-explicito");
        assertThat(environment.getProperty("DB_MIGRATIONS_URL")).contains("ep-demo.neon.tech").doesNotContain("pooler");
        assertThat(environment.getProperty("DB_MIGRATIONS_USER")).isEqualTo("migration");
    }

    @Test
    void poolingSemConexaoDiretaFalhaComMensagemClara() {
        var environment = new MockEnvironment().withProperty("DATABASE_URL", "postgresql://user:secret@ep-demo-pooler.neon.tech/neondb");
        assertThatThrownBy(() -> new NeonEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("DATABASE_URL_UNPOOLED").hasMessageNotContaining("secret");
    }

    @Test
    void urlInvalidaNaoVazaCredenciais() {
        assertThatThrownBy(() -> NeonEnvironmentPostProcessor.connection("https://user:secret@host/db"))
                .isInstanceOf(IllegalStateException.class).hasMessageNotContaining("secret").hasNoCause();
    }
}
