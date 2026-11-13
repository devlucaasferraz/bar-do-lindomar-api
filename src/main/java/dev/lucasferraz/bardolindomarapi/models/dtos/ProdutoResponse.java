package dev.lucasferraz.bardolindomarapi.models.dtos;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProdutoResponse(Long id, String nome, String descricao,
        @Schema(description = "Preço em reais", example = "5.50") BigDecimal preco,
        CategoriaResponse categoria) {}
