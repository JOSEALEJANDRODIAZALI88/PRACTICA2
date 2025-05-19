package com.universidad.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maneja excepciones para la capa de "validation".
 * Aplica solo a controladores en com.universidad.validation.*
 */
@RestControllerAdvice(value = "validationGlobalExceptionHandler")
public class ValidationGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult()
                .getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (e1, e2) -> e2
                ));

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos de entrada",
                errores,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String nombre = ex.getName();
        String tipo = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        Object valor = ex.getValue();
        String mensaje = String.format("El parámetro '%s' debería ser de tipo '%s', pero se recibió: '%s'",
                nombre, tipo, valor);

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de tipo de datos",
                mensaje,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (e1, e2) -> e2
                ));

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los parámetros",
                errores,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ex.printStackTrace();
        String mensajeUsuario = "Error en el formato de los datos enviados.";
        String detalles = ex.getMostSpecificCause().getMessage();
        if (detalles != null && detalles.contains("Cannot deserialize value of type") && detalles.contains("java.time.LocalDate")) {
            if (detalles.contains("from Null value")) {
                mensajeUsuario = "El campo de fecha obligatoria no puede ser nulo y debe tener formato yyyy-MM-dd.";
            } else {
                mensajeUsuario = "El campo de fecha debe tener formato yyyy-MM-dd.";
            }
        }
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                mensajeUsuario,
                detalles,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        String mensaje = "Violación de restricción de datos. Puede que algún valor ya exista o no cumpla una restricción única.";
        String detalles = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (detalles.contains("duplicate key value") && detalles.contains("email")) {
            mensaje = "El email ya está registrado. Debe ingresar un email único.";
        }
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                mensaje,
                detalles,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos de entrada",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // Excepción interna para recursos no disponibles
    public static class RecursoNoDisponibleException extends RuntimeException {
        public RecursoNoDisponibleException(String mensaje) {
            super(mensaje);
        }
    }
}
