package dev.lucasferraz.bardolindomarapi.services;

import java.util.List;
import dev.lucasferraz.bardolindomarapi.models.Categoria;
import dev.lucasferraz.bardolindomarapi.models.dtos.CategoriaResponse;
import dev.lucasferraz.bardolindomarapi.models.mappers.CategoriaMapper;
import dev.lucasferraz.bardolindomarapi.repositories.CategoriaRepository;
import dev.lucasferraz.bardolindomarapi.exceptions.RecursoNaoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository categorias, CategoriaMapper mapper) {
        this.categorias = categorias;
        this.mapper = mapper;
    }

    public List<CategoriaResponse> listar() {
        return categorias.findAll(Sort.by("nome").and(Sort.by("id"))).stream().map(mapper::toResponse).toList();
    }

    public Categoria buscarEntidade(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));
    }
}
