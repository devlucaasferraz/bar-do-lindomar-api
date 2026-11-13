package dev.lucasferraz.bardolindomarapi.models.mappers;

import dev.lucasferraz.bardolindomarapi.models.Produto;
import dev.lucasferraz.bardolindomarapi.models.dtos.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProdutoMapper {
    ProdutoResponse toResponse(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Produto toEntity(ProdutoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    void update(ProdutoUpdateRequest request, @MappingTarget Produto produto);
}
