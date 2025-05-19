package com.universidad.registro.controller;

import com.universidad.registro.model.Usuario;
import com.universidad.registro.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✅ CREATE
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> createUsuario(@Valid @RequestBody Usuario usuario) {
        Usuario saved = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ READ - Listar todos
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> users = usuarioRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ✅ READ - Buscar por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }
        return ResponseEntity.ok(opt.get());
    }

    // ✅ UPDATE
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id,
                                           @Valid @RequestBody Usuario updatedUsuario) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Usuario no encontrado");
        }
        Usuario usuario = opt.get();
        usuario.setNombre(updatedUsuario.getNombre());
        usuario.setApellido(updatedUsuario.getApellido());
        usuario.setEmail(updatedUsuario.getEmail());
        usuario.setActivo(updatedUsuario.isActivo());
        Usuario saved = usuarioRepository.save(usuario);
        return ResponseEntity.ok(saved);
    }

    // ✅ DELETE LÓGICO (DESACTIVAR)
    @PatchMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUsuario(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Usuario no encontrado");
        }
        Usuario usuario = opt.get();
        usuario.setActivo(false);
        Usuario saved = usuarioRepository.save(usuario);
        return ResponseEntity.ok(saved);
    }
}
