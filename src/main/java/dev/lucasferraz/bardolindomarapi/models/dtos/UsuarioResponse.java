package dev.lucasferraz.bardolindomarapi.models.dtos;

import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuário autenticado, sem senha ou hash")
public record UsuarioResponse(Long id, String nome, String email, Set<RoleResponse> roles) {}
