package dev.lucasferraz.bardolindomarapi.models;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank @Email @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @NotBlank @Size(max = 100)
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    protected Usuario() {}

    public Usuario(String nome, String email, String senhaHash, Set<Role> roles) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.roles = new HashSet<>(roles);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
}
