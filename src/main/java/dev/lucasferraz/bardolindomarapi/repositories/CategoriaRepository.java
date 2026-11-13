package dev.lucasferraz.bardolindomarapi.repositories;

import dev.lucasferraz.bardolindomarapi.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}
