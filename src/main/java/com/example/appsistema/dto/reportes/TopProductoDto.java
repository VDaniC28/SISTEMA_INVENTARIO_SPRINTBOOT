package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductoDto {
    
     private Integer idProducto;
    private String nombreProducto;
    private String categoria;
    private Integer cantidadVendida;
    private BigDecimal montoTotal;
    private String imagenURL;
}
