package dev.lucasferraz.bardolindomarapi.models.mappers;

import dev.lucasferraz.bardolindomarapi.models.Categoria;
import dev.lucasferraz.bardolindomarapi.models.dtos.CategoriaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoriaMapper {
    CategoriaResponse toResponse(Categoria categoria);
}
