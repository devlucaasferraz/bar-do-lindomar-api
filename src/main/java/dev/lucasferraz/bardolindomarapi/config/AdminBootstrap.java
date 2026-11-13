package dev.lucasferraz.bardolindomarapi.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import dev.lucasferraz.bardolindomarapi.models.Usuario;
import dev.lucasferraz.bardolindomarapi.models.enums.NomeRole;
import dev.lucasferraz.bardolindomarapi.repositories.*;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    private final Validator validator;
    private final String nome;
    private final String email;
    private final String senha;

    public AdminBootstrap(UsuarioRepository usuarios, RoleRepository roles, PasswordEncoder passwords,
            Validator validator, @Value("${app.admin.nome}") String nome,
            @Value("${app.admin.email}") String email, @Value("${app.admin.senha}") String senha) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwords = passwords;
        this.validator = validator;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRolesNome(NomeRole.ADMIN)) return;
        if (nome.isBlank() || email.isBlank() || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Defina ADMIN_NOME, ADMIN_EMAIL e ADMIN_SENHA (mínimo 8 caracteres e máximo 72 bytes) para criar o administrador inicial.");
        }
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmail(normalized).isPresent()) {
            throw new IllegalStateException("O e-mail inicial já pertence a um usuário sem permissão administrativa.");
        }
        var role = roles.findByNome(NomeRole.ADMIN).orElseThrow();
        var admin = new Usuario(nome.strip(), normalized, passwords.encode(senha), Set.of(role));
        if (!validator.validate(admin).isEmpty()) {
            throw new IllegalStateException("Nome ou e-mail do administrador inicial inválido.");
        }
        usuarios.saveAndFlush(admin);
    }
}
