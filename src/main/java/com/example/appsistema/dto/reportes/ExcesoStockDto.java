package com.example.appsistema.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcesoStockDto {
    
    private Integer idProducto;
    private String nombreProducto;
    private String categoria;
    private Integer stockActual;
    private Integer stockMaximo;
    private Integer exceso; // stockActual - stockMaximo
    private Double porcentajeExceso;
    private String imagenURL;
}
