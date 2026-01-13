package com.example.appsistema.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="salidas_inventario_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalidaInventarioDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalleSalida")
    private Integer idDetalleSalida;
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @Column(name = "precioUnitario", precision = 10, scale = 4, nullable = false)
    private BigDecimal precioUnitario;
    
    @Column(name = "descuentoUnitario", precision = 10, scale = 4)
    private BigDecimal descuentoUnitario;
    
    @Column(name = "impuestoUnitario", precision = 10, scale = 4)
    private BigDecimal impuestoUnitario;
    
    @Column(name = "subtotalLinea", precision = 12, scale = 4, nullable = false)
    private BigDecimal subtotalLinea;
    
    @Column(name = "lote", length = 50)
    private String lote;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSalida")
    private SalidaInventario salidaInventario;

    // Producto solo estará presente si NO es un suministro (según tu constraint CHECK en la BD).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProducto") // Asume que tienes una clase 'Producto'
    private Producto producto;

    // Suministro solo estará presente si NO es un producto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSuministro") // Asume que tienes una clase 'Suministro'
    private Suministro suministro;

    // 4. Relación con Almacen (idAlmacen)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAlmacen") // Asume que tienes una clase 'Almacen'
    private Almacen almacen;
    
    @PrePersist
    protected void onCreate() {
        if (descuentoUnitario == null) {
            descuentoUnitario = BigDecimal.ZERO;
        }
        if (impuestoUnitario == null) {
            impuestoUnitario = BigDecimal.ZERO;
        }
    }
    
    // Método auxiliar para calcular el subtotal de la línea
    public BigDecimal calcularSubtotalLinea() {
        BigDecimal precioConDescuento = precioUnitario.subtract(descuentoUnitario);
        BigDecimal precioConImpuesto = precioConDescuento.add(impuestoUnitario);
        return precioConImpuesto.multiply(new BigDecimal(cantidad));
    }
}
