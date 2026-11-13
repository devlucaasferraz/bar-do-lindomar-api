package dev.lucasferraz.bardolindomarapi.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) @Schema(example = "admin@exemplo.com") String email,
        @NotBlank @Size(max = 72) @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY) String senha) {}
