package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentasMensualesDto {
    
    private String mes; // "Enero", "Febrero", etc.
    private Integer numeroMes; // 1-12
    private BigDecimal ventasAnioActual;
    private BigDecimal ventasAnioAnterior;
    private Integer anioActual;
    private Integer anioAnterior;
}
