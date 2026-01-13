package com.example.appsistema.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SalidaInventarioDTO {
    private Integer idSalida;
    private Integer idUsuario;
    private Integer idCliente;
    private Integer idAlmacen;
    private String numeroSalida;
    private LocalDate fechaSalida;
    private String tipoSalida;
    private String tipoComprobante;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal descuentos;
    private BigDecimal montoTotal;
    private String estadoSalida;
    private List<SalidaInventarioDetalleDTO> detalles;
    
    // Para vistas
    private String nombreCliente;
    private String nombreAlmacen;
    private String nombreUsuario;
    private String tipoDocumentoCliente; 
    private String numeroDocumentoCliente; 
    private String direccionCliente;
    private String direccionAlmacen; 
}
