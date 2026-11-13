package dev.lucasferraz.bardolindomarapi;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import dev.lucasferraz.bardolindomarapi.config.AdminBootstrap;
import dev.lucasferraz.bardolindomarapi.repositories.UsuarioRepository;
import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends PostgresIntegrationTest {
    @Autowired JwtEncoder encoder;
    @Autowired UsuarioRepository usuarios;
    @Autowired AdminBootstrap bootstrap;

    @Test
    void loginRetornaTokenEUsuarioSemSenha() throws Exception {
        var response = request("POST", "/auth/login", "{\"email\":\"ADMIN@TESTE.COM\",\"senha\":\"senha-teste-123\"}", null);
        assertThat(response.statusCode()).isEqualTo(200);
        var body = json(response);
        assertThat(body.get("tipo").asString()).isEqualTo("Bearer");
        assertThat(body.get("expiraEm").asLong()).isEqualTo(28800);
        assertThat(body.get("usuario").get("email").asString()).isEqualTo("admin@teste.com");
        assertThat(response.body()).doesNotContain("senha", "senhaHash");
        assertThat(body.get("token").asString()).isNotBlank();
    }

    @Test
    void loginInvalidoERequisicaoMalformadaUsamProblemDetail() throws Exception {
        var wrong = request("POST", "/auth/login", "{\"email\":\"admin@teste.com\",\"senha\":\"incorreta\"}", null);
        assertThat(wrong.statusCode()).isEqualTo(401);
        assertThat(wrong.headers().firstValue("content-type").orElseThrow()).contains("application/problem+json");
        var missing = request("POST", "/auth/login", "{\"email\":\"naoexiste@teste.com\",\"senha\":\"incorreta\"}", null);
        assertThat(missing.statusCode()).isEqualTo(401);
        assertThat(json(missing).get("detail")).isEqualTo(json(wrong).get("detail"));
        var malformed = request("POST", "/auth/login", "{", null);
        assertThat(malformed.statusCode()).isEqualTo(400);
        var invalid = request("POST", "/auth/login", "{\"email\":\"invalido\",\"senha\":\"\"}", null);
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(json(invalid).get("errors").size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void escritaExigeAutenticacaoEPermissao() throws Exception {
        var anonymous = request("POST", "/produtos", "{}", null);
        assertThat(anonymous.statusCode()).isEqualTo(401);
        assertThat(json(anonymous).get("status").asInt()).isEqualTo(401);
        var client = request("POST", "/produtos", "{}", token("CLIENTE", Instant.now().plusSeconds(600), "bar-do-lindomar-api"));
        assertThat(client.statusCode()).isEqualTo(403);
        assertThat(json(client).get("status").asInt()).isEqualTo(403);
        for (String method : new String[]{"PUT", "DELETE"}) {
            assertThat(request(method, "/produtos/1", "{}", null).statusCode()).isEqualTo(401);
            assertThat(request(method, "/produtos/1", "{}", token("CLIENTE", Instant.now().plusSeconds(600), "bar-do-lindomar-api")).statusCode()).isEqualTo(403);
        }
        assertThat(request("POST", "/produtos", "{}", "invalido").statusCode()).isEqualTo(401);
        assertThat(request("POST", "/produtos", "{}", token("ADMIN", Instant.now().minusSeconds(600), "bar-do-lindomar-api")).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/produtos", "{}", token("ADMIN", Instant.now().plusSeconds(600), "outro-emissor")).statusCode()).isEqualTo(401);
    }

    @Test
    void bootstrapPreservaAdministradorExistente() throws Exception {
        var before = usuarios.findByEmail("admin@teste.com").orElseThrow();
        bootstrap.run(new org.springframework.boot.DefaultApplicationArguments());
        var after = usuarios.findByEmail("admin@teste.com").orElseThrow();
        assertThat(after.getId()).isEqualTo(before.getId());
        assertThat(after.getSenhaHash()).isEqualTo(before.getSenhaHash());
        assertThat(usuarios.count()).isEqualTo(1);
    }

    private String token(String role, Instant expires, String issuer) {
        var claims = JwtClaimsSet.builder().issuer(issuer).subject("999")
                .issuedAt(Instant.now().minusSeconds(1200)).expiresAt(expires)
                .claim("roles", java.util.List.of(role)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
