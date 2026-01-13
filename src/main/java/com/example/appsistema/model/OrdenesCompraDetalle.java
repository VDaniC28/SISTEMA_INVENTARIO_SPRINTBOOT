package com.example.appsistema.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true) 
@Table(name = "ordenes_compras_detalle")
public class OrdenesCompraDetalle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalle")
    private Integer idDetalle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idOrdenes", referencedColumnName = "idOrdenes", nullable = false)
    @JsonIgnore
    private OrdenesCompra ordenCompra;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProducto", referencedColumnName = "idProducto")
    private Producto producto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSuministro", referencedColumnName = "idSuministro")
    private Suministro suministro;
    
    @Column(name = "cantidadSolicitada", nullable = false)
    private Integer cantidadSolicitada;
    
    @Column(name = "cantidadRecibida")
    private Integer cantidadRecibida = 0;
    
    @Column(name = "precioUnitario", precision = 10, scale = 4, nullable = false)
    private BigDecimal precioUnitario;
    
    @Column(name = "descuentoUnitario", precision = 10, scale = 4)
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;
    
    @Column(name = "impuestoUnitario", precision = 10, scale = 4)
    private BigDecimal impuestoUnitario = BigDecimal.ZERO;

    @Transient // No se persiste en la BD, solo se usa en la deserialización
    private Integer idItem;
    
    @Transient // No se persiste en la BD
    private String tipoItem;
    
    // Constructores
    public OrdenesCompraDetalle() {}
    
    public OrdenesCompraDetalle(OrdenesCompra ordenCompra, Integer cantidadSolicitada, 
                               BigDecimal precioUnitario) {
        this.ordenCompra = ordenCompra;
        this.cantidadSolicitada = cantidadSolicitada;
        this.precioUnitario = precioUnitario;
    }
    
    // Constructor para producto
    public OrdenesCompraDetalle(OrdenesCompra ordenCompra, Producto producto, 
                               Integer cantidadSolicitada, BigDecimal precioUnitario) {
        this(ordenCompra, cantidadSolicitada, precioUnitario);
        this.producto = producto;
    }
    
    // Constructor para suministro
    public OrdenesCompraDetalle(OrdenesCompra ordenCompra, Suministro suministro, 
                               Integer cantidadSolicitada, BigDecimal precioUnitario) {
        this(ordenCompra, cantidadSolicitada, precioUnitario);
        this.suministro = suministro;
    }
    
    // Getters y Setters
    public Integer getIdDetalle() {
        return idDetalle;
    }
    
    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    
    public OrdenesCompra getOrdenCompra() {
        return ordenCompra;
    }
    
    public void setOrdenCompra(OrdenesCompra ordenCompra) {
        this.ordenCompra = ordenCompra;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public Suministro getSuministro() {
        return suministro;
    }
    
    public void setSuministro(Suministro suministro) {
        this.suministro = suministro;
    }
    
    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }
    
    public void setCantidadSolicitada(Integer cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }
    
    public Integer getCantidadRecibida() {
        return cantidadRecibida;
    }
    
    public void setCantidadRecibida(Integer cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getDescuentoUnitario() {
        return descuentoUnitario;
    }
    
    public void setDescuentoUnitario(BigDecimal descuentoUnitario) {
        this.descuentoUnitario = descuentoUnitario;
    }
    
    public BigDecimal getImpuestoUnitario() {
        return impuestoUnitario;
    }
    
    public void setImpuestoUnitario(BigDecimal impuestoUnitario) {
        this.impuestoUnitario = impuestoUnitario;
    }
    
    // Métodos auxiliares
    
    // Obtener el nombre del item (producto o suministro)
    public String getNombreItem() {
        if (producto != null) {
            return producto.getNombreProducto();
        } else if (suministro != null) {
            return suministro.getNombreSuministro();
        }
        return "Item no definido";
    }
    
    // Obtener el código del item
    public String getCodigoItem() {
        if (producto != null) {
            return producto.getSerialProducto();
        } else if (suministro != null) {
            return suministro.getCodigoSuministro();
        }
        return "";
    }
    
    // Calcular subtotal de la línea (cantidad * precio unitario)
    public BigDecimal getSubtotalLinea() {
        BigDecimal cantidad = new BigDecimal(cantidadSolicitada);
        return precioUnitario.multiply(cantidad);
    }
    
    // Calcular el descuento total de la línea
    public BigDecimal getDescuentoTotal() {
        if (descuentoUnitario == null || descuentoUnitario.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        BigDecimal cantidad = new BigDecimal(cantidadSolicitada);
        return descuentoUnitario.multiply(cantidad);
    }
    
    // Calcular el impuesto total de la línea
    public BigDecimal getImpuestoTotal() {
        if (impuestoUnitario == null || impuestoUnitario.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        BigDecimal cantidad = new BigDecimal(cantidadSolicitada);
        return impuestoUnitario.multiply(cantidad);
    }
    
    // Calcular el total final de la línea
    public BigDecimal getTotalLinea() {
        BigDecimal subtotal = getSubtotalLinea();
        BigDecimal descuento = getDescuentoTotal();
        BigDecimal impuesto = getImpuestoTotal();
        
        return subtotal.subtract(descuento).add(impuesto);
    }
    
    // Obtener cantidad pendiente de recibir
    public Integer getCantidadPendiente() {
        return cantidadSolicitada - cantidadRecibida;
    }
    
    // Verificar si el item está completamente recibido
    public boolean estaCompletamenteRecibido() {
        return cantidadRecibida.equals(cantidadSolicitada);
    }
    
    // Verificar si hay recepción parcial
    public boolean tieneRecepcionParcial() {
        return cantidadRecibida > 0 && cantidadRecibida < cantidadSolicitada;
    }
    
    // Obtener el porcentaje de recepción
    public double getPorcentajeRecepcion() {
        if (cantidadSolicitada == 0) {
            return 0.0;
        }
        return (cantidadRecibida.doubleValue() / cantidadSolicitada.doubleValue()) * 100;
    }
    
    // Verificar si es un producto
    public boolean esProducto() {
        return producto != null;
    }
    
    // Verificar si es un suministro
    public boolean esSuministro() {
        return suministro != null;
    }
    
    @Override
    public String toString() {
        return "OrdenesCompraDetalle{" +
                "idDetalle=" + idDetalle +
                ", cantidadSolicitada=" + cantidadSolicitada +
                ", cantidadRecibida=" + cantidadRecibida +
                ", precioUnitario=" + precioUnitario +
                ", nombreItem='" + getNombreItem() + '\'' +
                '}';
    }

    // 3. Setter para mapear 'cantidad' (nombre común en JS) a 'cantidadSolicitada' (nombre JPA)
    public void setCantidad(Integer cantidad) {
        this.cantidadSolicitada = cantidad;
    }
    
    // Getters y Setters para los campos transitorios (Jackson los necesita)
    public Integer getIdItem() { return idItem; }
    public void setIdItem(Integer idItem) { this.idItem = idItem; }

    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
}
