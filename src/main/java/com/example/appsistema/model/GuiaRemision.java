package com.example.appsistema.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.appsistema.component.AuditEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guias_remision")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemision {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGuia;
    
    @Column(nullable = false, unique = true, length = 50)
    private String numeroGuia;
    
    @Column(length = 10)
    private String serieGuia;
    
    @Column(length = 20)
    private String correlativoGuia;
    
    @Column(nullable = false)
    private LocalDate fechaEmision;
    
    private LocalDate fechaTraslado;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGuia tipoGuia;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoGuia estadoGuia = EstadoGuia.Emitida;
    
    @ManyToOne
    @JoinColumn(name = "idEmpresa", nullable = false)
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumn(name = "idOrdenes")
    private OrdenesCompra ordenCompra;
    
    @ManyToOne
    @JoinColumn(name = "idSalida")
    private SalidaInventario salidaInventario;
    
    @ManyToOne
    @JoinColumn(name = "idAlmacenOrigen")
    private Almacen almacenOrigen;
    
    @ManyToOne
    @JoinColumn(name = "idAlmacenDestino")
    private Almacen almacenDestino;
    
    @Column(length = 150)
    private String transportista;
    
    @Column(length = 20)
    private String placaVehiculo;
    
    @Column(length = 20)
    private String licenciaConducir;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoTraslado motivoTraslado;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal pesoTotal;
    
    private Integer numeroPackages = 1;
    
    @Column(length = 600)
    private String observaciones;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @OneToMany(mappedBy = "guiaRemision", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuiaRemisionDetalle> detalles = new ArrayList<>();
    
    public enum TipoGuia {
        Entrada, Salida, Transferencia
    }
    
    public enum EstadoGuia {
        Emitida, En_Transito, Recibida, Anulada
    }
    
    public enum MotivoTraslado {
        Venta, Compra, Transferencia, Devolucion, Otros
    }
    
    public void addDetalle(GuiaRemisionDetalle detalle) {
        detalles.add(detalle);
        detalle.setGuiaRemision(this);
    }
    
    public void removeDetalle(GuiaRemisionDetalle detalle) {
        detalles.remove(detalle);
        detalle.setGuiaRemision(null);
    }
}
