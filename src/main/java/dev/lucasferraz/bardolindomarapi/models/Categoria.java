package dev.lucasferraz.bardolindomarapi.models;

import dev.lucasferraz.bardolindomarapi.models.enums.TipoCategoria;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 40)
    private TipoCategoria codigo;

    @NotBlank @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nome;

    protected Categoria() {}

    public Long getId() { return id; }
    public TipoCategoria getCodigo() { return codigo; }
    public String getNome() { return nome; }
}
