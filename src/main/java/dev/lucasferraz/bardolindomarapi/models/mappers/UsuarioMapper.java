package dev.lucasferraz.bardolindomarapi.models.mappers;

import dev.lucasferraz.bardolindomarapi.models.Usuario;
import dev.lucasferraz.bardolindomarapi.models.dtos.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = RoleMapper.class)
public interface UsuarioMapper {
    UsuarioResponse toResponse(Usuario model);
}
