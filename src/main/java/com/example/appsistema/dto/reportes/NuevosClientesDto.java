package com.example.appsistema.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevosClientesDto {
    
    private String mes;
    private Integer numeroMes;
    private Integer cantidadNuevos;
    private Integer anio;
}
