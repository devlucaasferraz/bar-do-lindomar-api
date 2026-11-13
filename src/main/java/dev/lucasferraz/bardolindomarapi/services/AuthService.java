package dev.lucasferraz.bardolindomarapi.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import dev.lucasferraz.bardolindomarapi.models.dtos.*;
import dev.lucasferraz.bardolindomarapi.models.mappers.UsuarioMapper;
import dev.lucasferraz.bardolindomarapi.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final UsuarioMapper mapper;
    private final JwtEncoder encoder;
    private final String issuer;
    private final long ttl;
    private final String dummyHash;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwords, UsuarioMapper mapper,
            JwtEncoder encoder, @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-seconds}") long ttl) {
        if (ttl <= 0) throw new IllegalStateException("JWT_TTL_SECONDS deve ser positivo.");
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.mapper = mapper;
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
        this.dummyHash = passwords.encode("verificacao-interna-sem-usuario");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var usuario = usuarios.findByEmail(request.email().strip().toLowerCase(Locale.ROOT));
        if (request.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        boolean senhaValida = passwords.matches(request.senha(), usuario.map(u -> u.getSenhaHash()).orElse(dummyHash));
        if (usuario.isEmpty() || !senhaValida) throw new BadCredentialsException("E-mail ou senha inválidos.");
        var user = usuario.orElseThrow();
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .issuedAt(now).expiresAt(now.plusSeconds(ttl))
                .claim("roles", user.getRoles().stream().map(role -> role.getNome().name()).sorted().toList())
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", ttl, mapper.toResponse(user));
    }
}
