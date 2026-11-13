package dev.lucasferraz.bardolindomarapi.models;

import dev.lucasferraz.bardolindomarapi.models.enums.NomeRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NomeRole nome;

    protected Role() {}

    public Long getId() { return id; }
    public NomeRole getNome() { return nome; }
}
