package com.example.appsistema.dto.reportes;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProveedorDto {
    
    private Integer idProveedor;
    private String nombreProveedor;
    private String tipoDocumento;
    private String numeroDocumento;
    private Integer cantidadOrdenes;
    private BigDecimal montoTotal;
    private String email;
}
