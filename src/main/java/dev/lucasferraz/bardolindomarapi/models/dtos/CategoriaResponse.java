package dev.lucasferraz.bardolindomarapi.models.dtos;

import dev.lucasferraz.bardolindomarapi.models.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.media.Schema;

public record CategoriaResponse(Long id, @Schema(description = "Código aceito pelo filtro de produtos") TipoCategoria codigo,
        @Schema(example = "Bebidas alcoólicas") String nome) {}
