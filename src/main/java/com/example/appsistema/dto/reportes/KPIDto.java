package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPIDto {
    
    private BigDecimal ventasMes;
    private Double porcentajeCrecimiento;
    private Integer productosStock;
    private Integer ordenesPendientes;
    private Integer clientesActivos;
    private Double tasaCumplimiento;
}
