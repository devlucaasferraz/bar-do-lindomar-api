package dev.lucasferraz.bardolindomarapi.controllers;

import java.net.URI;
import dev.lucasferraz.bardolindomarapi.models.dtos.*;
import dev.lucasferraz.bardolindomarapi.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Parameter;
import dev.lucasferraz.bardolindomarapi.models.enums.TipoCategoria;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Catálogo e manutenção de produtos")
public class ProdutoController {
    private final ProdutoService produtos;

    public ProdutoController(ProdutoService produtos) { this.produtos = produtos; }

    @GetMapping
    @Operation(summary = "Listar e pesquisar produtos", description = "Consulta pública paginada, ordenada por nome e ID. Filtros combináveis; nome ignora maiúsculas/minúsculas.")
    @ApiResponse(responseCode = "200", description = "Página de produtos")
    @ApiResponse(responseCode = "400", description = "Filtro ou paginação inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PaginaResponse<ProdutoResponse> listar(
            @RequestParam(required = false) @Size(max = 120) @Parameter(description = "Trecho literal do nome") String nome,
            @RequestParam(required = false) @Parameter(description = "Código exato de categoria") TipoCategoria categoria,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return produtos.listar(nome, categoria, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar produto por ID", description = "Consulta pública de um produto.")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ProdutoResponse buscar(@PathVariable @Positive Long id) {
        return produtos.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Criar produto")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Operação realizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Exige ADMIN", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto ou categoria não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de integridade", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoCreateRequest request) {
        var produto = produtos.criar(request);
        return ResponseEntity.created(URI.create("/produtos/" + produto.id())).body(produto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir dados editáveis do produto")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Exige ADMIN", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto ou categoria não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de integridade", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ProdutoResponse atualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProdutoUpdateRequest request) {
        return produtos.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto definitivamente")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Operação realizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Exige ADMIN", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto ou categoria não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de integridade", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> remover(@PathVariable @Positive Long id) {
        produtos.remover(id);
        return ResponseEntity.noContent().build();
    }
}
