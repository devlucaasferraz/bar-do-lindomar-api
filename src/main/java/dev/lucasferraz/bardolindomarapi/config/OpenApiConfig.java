package dev.lucasferraz.bardolindomarapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@OpenAPIDefinition(info = @Info(
        title = "Bar do Lindomar API",
        version = "0.0.1-SNAPSHOT",
        description = "API do catálogo de produtos do Bar do Lindomar. "
                + "Login disponível; catálogo em implementação."
))
public class OpenApiConfig {
}
