package dev.lucasferraz.bardolindomarapi.repositories;

import java.util.Optional;
import dev.lucasferraz.bardolindomarapi.models.Role;
import dev.lucasferraz.bardolindomarapi.models.enums.NomeRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNome(NomeRole nome);
}
