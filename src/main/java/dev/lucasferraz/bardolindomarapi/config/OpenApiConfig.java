package dev.lucasferraz.bardolindomarapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Bar do Lindomar API",
        version = "0.0.1-SNAPSHOT",
        description = "API do catálogo de produtos do Bar do Lindomar. "
                + "Projeto em fase inicial; endpoints de negócio ainda não implementados."
))
public class OpenApiConfig {
}
