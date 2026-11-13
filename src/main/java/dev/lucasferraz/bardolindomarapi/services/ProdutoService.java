package dev.lucasferraz.bardolindomarapi.services;

import dev.lucasferraz.bardolindomarapi.models.Produto;
import dev.lucasferraz.bardolindomarapi.models.dtos.*;
import dev.lucasferraz.bardolindomarapi.models.mappers.ProdutoMapper;
import dev.lucasferraz.bardolindomarapi.repositories.ProdutoRepository;
import dev.lucasferraz.bardolindomarapi.exceptions.RecursoNaoEncontradoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final CategoriaService categorias;
    private final ProdutoMapper mapper;

    public ProdutoService(ProdutoRepository produtos, CategoriaService categorias, ProdutoMapper mapper) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProdutoResponse criar(ProdutoCreateRequest request) {
        var produto = mapper.toEntity(request);
        produto.setCategoria(categorias.buscarEntidade(request.categoriaId()));
        return mapper.toResponse(produtos.saveAndFlush(produto));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProdutoResponse atualizar(Long id, ProdutoUpdateRequest request) {
        var produto = buscarEntidade(id);
        var categoria = categorias.buscarEntidade(request.categoriaId());
        mapper.update(request, produto);
        produto.setCategoria(categoria);
        return mapper.toResponse(produtos.saveAndFlush(produto));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void remover(Long id) {
        produtos.delete(buscarEntidade(id));
        produtos.flush();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> listar(String nome,
            dev.lucasferraz.bardolindomarapi.models.enums.TipoCategoria categoria, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("nome").and(org.springframework.data.domain.Sort.by("id")));
        var result = produtos.findAll(dev.lucasferraz.bardolindomarapi.repositories.ProdutoSpecifications.filtrar(nome, categoria), pageable);
        return new PaginaResponse<>(result.getContent().stream().map(mapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private Produto buscarEntidade(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }
}
