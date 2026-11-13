package dev.lucasferraz.bardolindomarapi.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(String token, @Schema(example = "Bearer") String tipo,
        @Schema(description = "Validade em segundos", example = "28800") long expiraEm,
        UsuarioResponse usuario) {}
