package dev.lucasferraz.bardolindomarapi.models.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProdutoUpdateRequest(
        @NotBlank @Size(max = 120) @Schema(example = "Refrigerante lata") String nome,
        @Size(max = 500) @Schema(example = "Lata de 350 ml") String descricao,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
        @Schema(description = "Preço em reais, com até duas casas decimais", example = "5.50") BigDecimal preco,
        @NotNull @Positive @Schema(description = "ID obtido em GET /categorias", example = "1") Long categoriaId) {}
