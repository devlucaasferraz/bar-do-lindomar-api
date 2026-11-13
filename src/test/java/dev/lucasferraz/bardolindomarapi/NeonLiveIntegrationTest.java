package dev.lucasferraz.bardolindomarapi;

import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.address=127.0.0.1")
@ActiveProfiles("prod")
@EnabledIfEnvironmentVariable(named = "NEON_LIVE_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NeonLiveIntegrationTest {
    private static final String EMAIL = "neon-e2e-" + UUID.randomUUID() + "@example.invalid";
    private static final String PASSWORD = UUID.randomUUID().toString();
    private static final String PRODUCT_NAME = "Neon E2E " + UUID.randomUUID();
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    @Value("${local.server.port}") int port;
    private Long userId;
    private Long productId;

    @DynamicPropertySource
    static void credentials(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> UUID.randomUUID().toString() + UUID.randomUUID());
        registry.add("app.admin.nome", () -> "Teste temporário Neon");
        registry.add("app.admin.email", () -> EMAIL);
        registry.add("app.admin.senha", () -> PASSWORD);
    }

    @BeforeAll
    void temporaryAdmin() {
        var ids = jdbc.queryForList("SELECT id FROM usuarios WHERE email = ?", Long.class, EMAIL);
        if (ids.isEmpty()) {
            userId = jdbc.queryForObject("INSERT INTO usuarios (nome, email, senha_hash) VALUES (?, ?, ?) RETURNING id",
                    Long.class, "Teste temporário Neon", EMAIL, passwords.encode(PASSWORD));
            jdbc.update("INSERT INTO usuarios_roles (usuario_id, role_id) SELECT ?, id FROM roles WHERE nome = 'ADMIN'", userId);
        } else userId = ids.getFirst();
    }

    @AfterAll
    void cleanup() {
        try {
            if (productId != null) jdbc.update("DELETE FROM produtos WHERE id = ? AND nome LIKE ?", productId, PRODUCT_NAME + "%");
        } finally {
            if (userId != null) jdbc.update("DELETE FROM usuarios WHERE id = ? AND email = ?", userId, EMAIL);
        }
    }

    @Test
    void apiUsaPostgresNeonDePontaAPonta() throws Exception {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class)).isEqualTo(3);
        var categories = request("GET", "/categorias", null, null);
        assertThat(categories.statusCode()).isEqualTo(200);
        long categoryId = json(categories).get(0).get("id").asLong();
        assertThat(json(categories).size()).isGreaterThanOrEqualTo(4);
        String loginBody = "{\"email\":\"" + EMAIL + "\",\"senha\":\"" + PASSWORD + "\"}";
        var login = request("POST", "/auth/login", loginBody, null);
        assertThat(login.statusCode()).isEqualTo(200);
        String token = json(login).get("token").asString();
        String product = "{\"nome\":\"" + PRODUCT_NAME + "\",\"preco\":5.50,\"categoriaId\":" + categoryId + "}";
        assertThat(request("POST", "/produtos", product, null).statusCode()).isEqualTo(401);
        var created = request("POST", "/produtos", product, token);
        assertThat(created.statusCode()).isEqualTo(201);
        productId = json(created).get("id").asLong();
        assertThat(jdbc.queryForObject("SELECT nome FROM produtos WHERE id = ?", String.class, productId)).isEqualTo(PRODUCT_NAME);
        assertThat(request("GET", "/produtos/" + productId, null, null).statusCode()).isEqualTo(200);
        var page = request("GET", "/produtos?nome=" + java.net.URLEncoder.encode(PRODUCT_NAME, java.nio.charset.StandardCharsets.UTF_8), null, null);
        assertThat(json(page).get("totalElements").asLong()).isEqualTo(1);
        var updated = request("PUT", "/produtos/" + productId, product.replace("5.50", "6.00"), token);
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(json(updated).get("preco").decimalValue()).isEqualByComparingTo("6.00");
        assertThat(request("GET", "/v3/api-docs", null, null).statusCode()).isEqualTo(200);
        assertThat(request("DELETE", "/produtos/" + productId, null, token).statusCode()).isEqualTo(204);
        assertThat(request("GET", "/produtos/" + productId, null, null).statusCode()).isEqualTo(404);
        System.out.println("NEON E2E OK: Flyway, login JWT, categorias públicas, CRUD persistido, filtros e Swagger; limpeza temporária ao encerrar.");
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private JsonNode json(HttpResponse<String> response) { return JsonMapper.builder().build().readTree(response.body()); }
}
