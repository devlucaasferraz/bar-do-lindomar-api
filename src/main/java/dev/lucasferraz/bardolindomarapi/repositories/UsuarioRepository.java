package dev.lucasferraz.bardolindomarapi.repositories;

import java.util.Optional;
import dev.lucasferraz.bardolindomarapi.models.Usuario;
import dev.lucasferraz.bardolindomarapi.models.enums.NomeRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByEmail(String email);
    boolean existsByRolesNome(NomeRole nome);
}
