package dev.lucasferraz.bardolindomarapi.repositories;

import dev.lucasferraz.bardolindomarapi.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {}
