package com.universidad.controller;

import com.universidad.model.Estudiante;
import com.universidad.model.Materia;
import com.universidad.service.IEstudianteService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import com.universidad.dto.EstudianteDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
@Validated
public class EstudianteController {

    private final IEstudianteService estudianteService;
    private static final Logger logger = LoggerFactory.getLogger(EstudianteController.class);

    @Autowired
    public EstudianteController(IEstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @Operation(summary = "Obtener todos los estudiantes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estudiantes obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> obtenerTodosLosEstudiantes() {
        long inicio = System.currentTimeMillis();
        logger.info("[ESTUDIANTE] Inicio obtenerTodosLosEstudiantes: {}", inicio);
        List<EstudianteDTO> estudiantes = estudianteService.obtenerTodosLosEstudiantes();
        long fin = System.currentTimeMillis();
        logger.info("[ESTUDIANTE] Fin obtenerTodosLosEstudiantes: {} (Duracion: {} ms)", fin, (fin - inicio));
        return ResponseEntity.ok(estudiantes);
    }

    @Operation(summary = "Obtener un estudiante por número de inscripción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante encontrado"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/inscripcion/{numeroInscripcion}")
    public ResponseEntity<EstudianteDTO> obtenerEstudiantePorNumeroInscripcion(
            @PathVariable String numeroInscripcion) {
        long inicio = System.currentTimeMillis();
        logger.info("[ESTUDIANTE] Inicio obtenerEstudiantePorNumeroInscripcion: {}", inicio);
        EstudianteDTO estudiante = estudianteService.obtenerEstudiantePorNumeroInscripcion(numeroInscripcion);
        long fin = System.currentTimeMillis();
        logger.info("[ESTUDIANTE] Fin obtenerEstudiantePorNumeroInscripcion: {} (Duracion: {} ms)", fin, (fin - inicio));
        return ResponseEntity.ok(estudiante);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}/materias")
    public ResponseEntity<List<Materia>> obtenerMateriasDeEstudiante(@PathVariable("id") Long estudianteId) {
        List<Materia> materias = estudianteService.obtenerMateriasDeEstudiante(estudianteId);
        return ResponseEntity.ok(materias);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}/lock")
    public ResponseEntity<Estudiante> getEstudianteConBloqueo(@PathVariable Long id) {
        Estudiante estudiante = estudianteService.obtenerEstudianteConBloqueo(id);
        return ResponseEntity.ok(estudiante);
    }

    @Operation(summary = "Crear un nuevo estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación de datos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EstudianteDTO> crearEstudiante(@Valid @RequestBody EstudianteDTO estudianteDTO) {
        EstudianteDTO nuevoEstudiante = estudianteService.crearEstudiante(estudianteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEstudiante);
    }

    @Operation(summary = "Actualizar un estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación de datos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EstudianteDTO> actualizarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteDTO estudianteDTO) {
        EstudianteDTO estudianteActualizado = estudianteService.actualizarEstudiante(id, estudianteDTO);
        return ResponseEntity.ok(estudianteActualizado);
    }

    @Operation(summary = "Dar de baja (eliminar lógicamente) un estudiante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante dado de baja exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/baja")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EstudianteDTO> eliminarEstudiante(
            @PathVariable Long id,
            @RequestBody EstudianteDTO estudianteDTO) {
        EstudianteDTO estudianteEliminado = estudianteService.eliminarEstudiante(id, estudianteDTO);
        return ResponseEntity.ok(estudianteEliminado);
    }

    @Operation(summary = "Obtener estudiantes activos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estudiantes activos obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/activos")
    public ResponseEntity<List<EstudianteDTO>> obtenerEstudianteActivo() {
        List<EstudianteDTO> estudiantesActivos = estudianteService.obtenerEstudianteActivo();
        return ResponseEntity.ok(estudiantesActivos);
    }
}
