package dev.lucasferraz.bardolindomarapi.models.dtos;

import dev.lucasferraz.bardolindomarapi.models.enums.NomeRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record RoleResponse(Long id, @Schema(description = "Permissão do usuário") NomeRole nome) {}
