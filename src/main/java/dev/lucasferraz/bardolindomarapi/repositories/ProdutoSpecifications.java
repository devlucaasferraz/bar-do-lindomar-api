package dev.lucasferraz.bardolindomarapi.repositories;

import java.util.ArrayList;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import dev.lucasferraz.bardolindomarapi.models.Produto;
import dev.lucasferraz.bardolindomarapi.models.enums.TipoCategoria;
import org.springframework.data.jpa.domain.Specification;

public final class ProdutoSpecifications {
    private ProdutoSpecifications() {}

    public static Specification<Produto> filtrar(String nome, TipoCategoria categoria) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (nome != null && !nome.isBlank()) {
                String termo = nome.strip().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.like(cb.lower(root.get("nome")), "%" + termo + "%", '\\'));
            }
            if (categoria != null) predicates.add(cb.equal(root.get("categoria").get("codigo"), categoria));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
