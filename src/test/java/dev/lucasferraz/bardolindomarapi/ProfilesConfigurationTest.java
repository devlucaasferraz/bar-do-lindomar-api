package dev.lucasferraz.bardolindomarapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilesConfigurationTest {
    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withInitializer(context -> {
                    var sources = context.getEnvironment().getPropertySources();
                    sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                    sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                })
                .withInitializer(new ConfigDataApplicationContextInitializer());
    }

    @Test
    void devEhOPerfilPadraoComBancoLocal() {
        runner().run(context -> {
            var environment = context.getEnvironment();
            assertThat(environment.getDefaultProfiles()).containsExactly("dev");
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://localhost:5432/lindomar");
            assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("lindomar");
            assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("lindomar_dev");
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        });
    }

    @Test
    void prodUsaConexaoESegredoFornecidos() {
        runner().withPropertyValues("spring.profiles.active=prod",
                "DB_URL=jdbc:postgresql://banco-producao:5432/catalogo", "DB_USER=usuario_prod",
                "DB_PASSWORD=senha-exclusiva-do-teste", "JWT_SECRET=segredo-de-teste-com-pelo-menos-32-bytes")
                .run(context -> {
                    var environment = context.getEnvironment();
                    assertThat(environment.getActiveProfiles()).containsExactly("prod");
                    assertThat(environment.getProperty("spring.datasource.url"))
                            .isEqualTo("jdbc:postgresql://banco-producao:5432/catalogo");
                    assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("usuario_prod");
                    assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("senha-exclusiva-do-teste");
                    assertThat(environment.getProperty("app.jwt.secret")).isEqualTo("segredo-de-teste-com-pelo-menos-32-bytes");
                });
    }

    @Test
    void prodNaoHerdaCredenciaisLocaisQuandoFaltaConfiguracao() {
        runner().withPropertyValues("spring.profiles.active=prod").run(context -> {
            for (String property : new String[]{"spring.datasource.url", "spring.datasource.username",
                    "spring.datasource.password", "app.jwt.secret"}) {
                assertThatThrownBy(() -> context.getEnvironment().getRequiredProperty(property))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        });
    }
}
