package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprasMensualesDto {
    
    private String mes;
    private Integer numeroMes;
    private Integer cantidadOrdenes;
    private BigDecimal montoTotal;
    private Integer anio;
}
