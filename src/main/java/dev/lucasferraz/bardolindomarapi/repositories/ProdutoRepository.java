package dev.lucasferraz.bardolindomarapi.repositories;

import dev.lucasferraz.bardolindomarapi.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {}
