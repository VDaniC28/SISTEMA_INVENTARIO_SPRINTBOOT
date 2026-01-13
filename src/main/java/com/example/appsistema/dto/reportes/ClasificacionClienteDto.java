package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionClienteDto {
    
    private String clasificacion; // "BUEN PAGADOR", "MAL PAGADOR"
    private Integer cantidadClientes;
    private BigDecimal montoTotal;
    private Double porcentaje;
}
