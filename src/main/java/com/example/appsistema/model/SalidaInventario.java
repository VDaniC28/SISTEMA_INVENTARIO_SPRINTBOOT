package com.example.appsistema.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.appsistema.component.AuditEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="salida_inventario")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalidaInventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSalida")
    private Integer idSalida;
    
    @Column(name = "numeroSalida", unique = true, nullable = false, length = 50)
    private String numeroSalida;
    
    @Column(name = "fechaSalida", nullable = false)
    private LocalDate fechaSalida;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoSalida", nullable = false)
    private TipoSalida tipoSalida;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoComprobante")
    private TipoComprobante tipoComprobante;
    
    @Column(name = "subtotal", precision = 12, scale = 4, nullable = false)
    private BigDecimal subtotal;
    
    @Column(name = "impuestos", precision = 12, scale = 4)
    private BigDecimal impuestos;
    
    @Column(name = "descuentos", precision = 12, scale = 4)
    private BigDecimal descuentos;
    
    @Column(name = "montoTotal", precision = 12, scale = 4, nullable = false)
    private BigDecimal montoTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estadoSalida")
    private EstadoSalida estadoSalida;

    // El usuario que registra la venta/movimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario") // Asume que tienes una clase 'Usuario'
    private Usuario usuario;

    // El almacén de donde sale el stock
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAlmacen") // Asume que tienes una clase 'Almacen'
    private Almacen almacen;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente")
    private Cliente cliente;
    
    @OneToMany(mappedBy = "salidaInventario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalidaInventarioDetalle> detalles = new ArrayList<>();
    
    // Enums
    public enum TipoSalida {
        Venta, Transferencia, Devolucion
    }
    
    public enum TipoComprobante {
        Boleta, Factura, Guia, Nota
    }
    
    public enum EstadoSalida {
        Pendiente, Completada, Anulada
    }

    public void addDetalle(SalidaInventarioDetalle detalle) {
        detalles.add(detalle);
        detalle.setSalidaInventario(this);
    }
    
    public void removeDetalle(SalidaInventarioDetalle detalle) {
        detalles.remove(detalle);
        detalle.setSalidaInventario(null);
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaSalida == null) {
            fechaSalida = LocalDate.now();
        }
        if (estadoSalida == null) {
            estadoSalida = EstadoSalida.Pendiente;
        }
        if (tipoSalida == null) {
            tipoSalida = TipoSalida.Venta;
        }
        if (tipoComprobante == null) {
            tipoComprobante = TipoComprobante.Boleta;
        }
        if (impuestos == null) {
            impuestos = BigDecimal.ZERO;
        }
        if (descuentos == null) {
            descuentos = BigDecimal.ZERO;
        }
    }
}
