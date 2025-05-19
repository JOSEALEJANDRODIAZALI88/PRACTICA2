package com.universidad.registro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres.")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "El nombre es obligatorio.")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = true)
    private LocalDate fechaAlta = LocalDate.now();

    private LocalDate fechaBaja;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
}
