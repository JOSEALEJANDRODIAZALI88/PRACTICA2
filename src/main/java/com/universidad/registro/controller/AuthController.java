package com.universidad.registro.controller;

// Spring Web y Spring Security
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Java Collections y Validaciones
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Clases de tu proyecto
import com.universidad.registro.dto.AuthDTO.JwtResponse;
import com.universidad.registro.dto.AuthDTO.LoginRequest;
import com.universidad.registro.dto.AuthDTO.MessageResponse;
import com.universidad.registro.dto.AuthDTO.SignupRequest;
import com.universidad.registro.model.Rol;
import com.universidad.registro.model.Usuario;
import com.universidad.registro.repository.RolRepository;
import com.universidad.registro.repository.UsuarioRepository;
import com.universidad.registro.security.JwtUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RolRepository rolRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

        return ResponseEntity.ok(new JwtResponse(jwt, "Bearer",
                usuario.getId(),
                userDetails.getUsername(),
                usuario.getEmail(),
                new HashSet<>(roles)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El nombre de usuario ya está en uso."));
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso."));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(signUpRequest.getUsername());
        usuario.setEmail(signUpRequest.getEmail());
        usuario.setPassword(encoder.encode(signUpRequest.getPassword()));
        usuario.setNombre(signUpRequest.getNombre());
        usuario.setApellido(signUpRequest.getApellido());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Rol> roles = new HashSet<>();

        if (strRoles == null) {
            Rol estudianteRol = rolRepository.findByNombre(Rol.NombreRol.ROL_ESTUDIANTE)
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
            roles.add(estudianteRol);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Rol adminRol = rolRepository.findByNombre(Rol.NombreRol.ROL_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(adminRol);
                        break;
                    case "docente":
                        Rol docRol = rolRepository.findByNombre(Rol.NombreRol.ROL_DOCENTE)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(docRol);
                        break;
                    default:
                        Rol estudianteRol = rolRepository.findByNombre(Rol.NombreRol.ROL_ESTUDIANTE)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(estudianteRol);
                }
            });
        }

        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente!"));
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(new JwtResponse(
                    null, "Bearer",
                    usuario.getId(),
                    userDetails.getUsername(),
                    usuario.getEmail(),
                    roles));
        }

        return ResponseEntity.ok(new MessageResponse("No hay sesión activa"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente!"));
    }
}
