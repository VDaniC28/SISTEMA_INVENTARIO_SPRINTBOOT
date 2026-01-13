package com.example.appsistema.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RotacionInventarioDto {
    
    private String categoria;
    private Integer stockActual;
    private Integer cantidadVendida;
    private Double indiceRotacion; // cantidadVendida / stockActual
    private String clasificacion; // "Alta", "Media", "Baja"
}
