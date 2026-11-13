package dev.lucasferraz.bardolindomarapi;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class PostgresIntegrationTest {
    @org.springframework.beans.factory.annotation.Value("${local.server.port}")
    protected int port;

    protected java.net.http.HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? java.net.http.HttpRequest.BodyPublishers.noBody()
                : java.net.http.HttpRequest.BodyPublishers.ofString(body));
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            return client.send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
        }
    }

    protected tools.jackson.databind.JsonNode json(java.net.http.HttpResponse<String> response) {
        return tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
    }

    protected String adminToken() throws Exception {
        return json(request("POST", "/auth/login", "{\"email\":\"admin@teste.com\",\"senha\":\"senha-teste-123\"}", null))
                .get("token").asString();
    }

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "segredo-exclusivo-dos-testes-com-mais-de-32-bytes");
        registry.add("app.admin.nome", () -> "Admin Teste");
        registry.add("app.admin.email", () -> "admin@teste.com");
        registry.add("app.admin.senha", () -> "senha-teste-123");
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
