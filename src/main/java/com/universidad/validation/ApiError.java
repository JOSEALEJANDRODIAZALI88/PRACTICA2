package com.universidad.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private int status;
    private String message;
    private Object errors; // Puede ser String o Map<String, String> para validaciones.
    private LocalDateTime timestamp = LocalDateTime.now();
}
