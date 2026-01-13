package com.example.appsistema.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ordenes_compras")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
public class OrdenesCompra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrdenes")
    private Integer idOrdenes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fechaOrden")
    private LocalDate fechaOrden;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd") 
    @Column(name = "fechaEntregaEsperada")
    private LocalDate fechaEntregaEsperada;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd") 
    @Column(name = "fechaEntregaReal")
    private LocalDate fechaEntregaReal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProveedor", referencedColumnName = "idProveedor")
    private Proveedor proveedor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAlmacen", referencedColumnName = "idAlmacen")
    private Almacen almacen;
    
    @Column(name = "montoSubtotal", precision = 12, scale = 4, nullable = false)
    private BigDecimal montoSubtotal;
    
    @Column(name = "impuestos", precision = 12, scale = 4)
    private BigDecimal impuestos = BigDecimal.ZERO;
    
    @Column(name = "descuentos", precision = 12, scale = 4)
    private BigDecimal descuentos = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estadoOrden", nullable = false)
    private EstadoOrden estadoOrden;
    
    // Relación uno a muchos con los detalles
    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenesCompraDetalle> detalles;
    
    // Enum para el estado de la orden
    // ✅ CORRECCIÓN 1: Se añadieron los nuevos estados de BDD
    public enum EstadoOrden {
        Pendiente, 
        Confirmada, 
        RecepcionParcial, // Estado para recepción incompleta
        Recibida,         // Estado final de recepción completa
        Cancelada
    }
    
    // Constructores, Getters y Setters...
    
    // Se omiten por espacio, asumiendo que el resto de Getters/Setters son correctos.
    // ...
    
    public Integer getIdOrdenes() {
        return idOrdenes;
    }
    
    public void setIdOrdenes(Integer idOrdenes) {
        this.idOrdenes = idOrdenes;
    }
    
    public LocalDate getFechaOrden() {
        return fechaOrden;
    }
    
    public void setFechaOrden(LocalDate fechaOrden) {
        this.fechaOrden = fechaOrden;
    }
    
    public LocalDate getFechaEntregaEsperada() {
        return fechaEntregaEsperada;
    }
    
    public void setFechaEntregaEsperada(LocalDate fechaEntregaEsperada) {
        this.fechaEntregaEsperada = fechaEntregaEsperada;
    }
    
    public LocalDate getFechaEntregaReal() {
        return fechaEntregaReal;
    }
    
    public void setFechaEntregaReal(LocalDate fechaEntregaReal) {
        this.fechaEntregaReal = fechaEntregaReal;
    }
    
    public Proveedor getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    
    public Almacen getAlmacen() {
        return almacen;
    }
    
    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }
    
    public BigDecimal getMontoSubtotal() {
        return montoSubtotal;
    }
    
    public void setMontoSubtotal(BigDecimal montoSubtotal) {
        this.montoSubtotal = montoSubtotal;
    }
    
    public BigDecimal getImpuestos() {
        return impuestos;
    }
    
    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }
    
    public BigDecimal getDescuentos() {
        return descuentos;
    }
    
    public void setDescuentos(BigDecimal descuentos) {
        this.descuentos = descuentos;
    }
    
    public EstadoOrden getEstadoOrden() {
        return estadoOrden;
    }
    
    public void setEstadoOrden(EstadoOrden estadoOrden) {
        this.estadoOrden = estadoOrden;
    }
    
    public List<OrdenesCompraDetalle> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<OrdenesCompraDetalle> detalles) {
        this.detalles = detalles;
    }
    
    // Método auxiliar para calcular el monto total
    public BigDecimal getMontoTotal() {
        BigDecimal total = montoSubtotal != null ? montoSubtotal : BigDecimal.ZERO;
        if (impuestos != null) {
            total = total.add(impuestos);
        }
        if (descuentos != null) {
            total = total.subtract(descuentos);
        }
        return total;
    }
    
    // Método para obtener el número de orden formateado
    public String getNumeroOrdenFormateado() {
        if (idOrdenes != null) {
            return String.format("#OC-%d-%03d", 
                fechaOrden != null ? fechaOrden.getYear() : LocalDate.now().getYear(), 
                idOrdenes);
        }
        return "";
    }
    
    // --- LÓGICA DE ESTADOS ACTUALIZADA ---
    
    // ✅ CORRECCIÓN 2: Solo se edita si está Pendiente. (Mantiene la lógica)
    public boolean puedeSerEditada() {
        return estadoOrden == EstadoOrden.Pendiente;
    }
    
    // ✅ CORRECCIÓN 3: Solo se confirma si está Pendiente. (Mantiene la lógica)
    public boolean puedeSerConfirmada() {
        return estadoOrden == EstadoOrden.Pendiente;
    }
    
    // ✅ CORRECCIÓN 4: Solo se cancela si está Pendiente o Confirmada (Se amplió la lógica para mayor flexibilidad, ajusta si es necesario).
    public boolean puedeSerCancelada() {
        // Se puede cancelar si aún no ha sido recibida completamente
        return estadoOrden == EstadoOrden.Pendiente || 
               estadoOrden == EstadoOrden.Confirmada ||
               estadoOrden == EstadoOrden.RecepcionParcial;
    }
    
    // ✅ CORRECCIÓN 5: Se puede hacer recepción si está Confirmada o ya está en Recepción Parcial.
    public boolean puedeHacerRecepcion() {
        return estadoOrden == EstadoOrden.Confirmada || estadoOrden == EstadoOrden.RecepcionParcial;
    }
    
    @Override
    public String toString() {
        return "OrdenesCompra{" +
                "idOrdenes=" + idOrdenes +
                ", fechaOrden=" + fechaOrden +
                ", estadoOrden=" + estadoOrden +
                ", montoSubtotal=" + montoSubtotal +
                '}';
    }
}