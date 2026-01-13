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
public class DetalleVentaDTO {
    
    private Integer idSalida;
    private String numeroSalida;
    private LocalDate fechaSalida;
    private String tipoComprobante;
    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private String estadoSalida;
    private List<ProductoVendidoDTO> productos;
}
