package dev.lucasferraz.bardolindomarapi.models.dtos;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaginaResponse<T>(List<T> items, @Schema(description = "Página atual, iniciando em zero") int page,
        int size, long totalElements, int totalPages) {}
