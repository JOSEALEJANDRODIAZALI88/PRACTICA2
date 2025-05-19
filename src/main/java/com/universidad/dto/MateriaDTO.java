package com.universidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaDTO implements Serializable {

    private Long id;
    private String nombreMateria;
    private String codigoUnico;
    private Integer creditos;

    /** Lista de IDs de materias que son prerequisitos para esta materia. */
    private List<Long> prerequisitos;

    /** Lista de IDs de materias para las que esta materia es prerequisito. */
    private List<Long> esPrerequisitoDe;
}
