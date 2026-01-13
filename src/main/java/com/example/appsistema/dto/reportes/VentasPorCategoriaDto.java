package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentasPorCategoriaDto {
    
    private String categoria;
    private Integer cantidadVendida;
    private BigDecimal montoTotal;
    private Double porcentaje;
}
