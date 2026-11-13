package dev.lucasferraz.bardolindomarapi.controllers;

import java.util.List;
import dev.lucasferraz.bardolindomarapi.models.dtos.CategoriaResponse;
import dev.lucasferraz.bardolindomarapi.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Categorias fixas do catálogo")
public class CategoriaController {
    private final CategoriaService categorias;

    public CategoriaController(CategoriaService categorias) { this.categorias = categorias; }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Consulta pública, ordenada por nome. Novas categorias são adicionadas por enum e migration.")
    @ApiResponse(responseCode = "200", description = "Categorias disponíveis")
    public List<CategoriaResponse> listar() { return categorias.listar(); }
}
