package com.example.appsistema.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SalidaInventarioDetalleDTO {
    private Integer idDetalleSalida;
    private Integer idSalida;
    private Integer idProducto;
    private Integer idSuministro;
    private Integer idAlmacen;
    private String tipoItem;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoUnitario;
    private BigDecimal impuestoUnitario;
    private BigDecimal subtotalLinea;
    private String lote;
    
    // Para vistas
    private String nombreItem;
    private String nombreAlmacen;
}
