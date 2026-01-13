package com.example.appsistema.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPorCobrarDTO {
    
    private Integer idCliente;
    private String nombreCliente;
    private String numeroDocumento;
    private String tipoDocumento;
    private String telefono;
    private String email;
    private Integer cantidadCompras;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaUltimaCompra;
    private List<DetalleVentaDTO> ventas;
}
