package dev.lucasferraz.bardolindomarapi.models.mappers;

import dev.lucasferraz.bardolindomarapi.models.Role;
import dev.lucasferraz.bardolindomarapi.models.dtos.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleMapper {
    RoleResponse toResponse(Role model);
}
