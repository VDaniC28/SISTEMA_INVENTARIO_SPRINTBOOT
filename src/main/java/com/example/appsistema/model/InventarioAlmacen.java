package com.example.appsistema.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="inventario_almacenes",
 uniqueConstraints = {
        @UniqueConstraint(name = "uk_almacen_producto_lote", columnNames = {"idAlmacen", "idProducto", "lote"}),
        @UniqueConstraint(name = "uk_almacen_suministro_lote", columnNames = {"idAlmacen", "idSuministro", "lote"})
    }
)
public class InventarioAlmacen implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInventario")
    private Integer idInventario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAlmacen", nullable = false)
    @NotNull(message = "El almacén es obligatorio")
    private Almacen almacen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProducto", nullable = true)
    private Producto producto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSuministro", nullable = true)
    private Suministro suministro;
    
    @Column(name = "stock", nullable = false)
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;
    
    @Column(name = "stockReservado")
    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Integer stockReservado = 0;
    
    @Size(max = 50, message = "El lote no puede exceder los 50 caracteres")
    @Column(name = "lote", length = 50)
    private String lote;
    
    // Constructores
    public InventarioAlmacen() {}
    
    public InventarioAlmacen(Almacen almacen, Producto producto, Integer stock, String lote) {
        this.almacen = almacen;
        this.producto = producto;
        this.stock = stock;
        this.lote = lote;
        this.stockReservado = 0;
    }
    
    public InventarioAlmacen(Almacen almacen, Suministro suministro, Integer stock, String lote) {
        this.almacen = almacen;
        this.suministro = suministro;
        this.stock = stock;
        this.lote = lote;
        this.stockReservado = 0;
    }
    
    // Métodos de validación
    @PrePersist
    @PreUpdate
    private void validarConstraints() {
        // Validar que solo uno de producto o suministro esté presente
        if ((producto != null && suministro != null) || (producto == null && suministro == null)) {
            throw new IllegalStateException("Debe especificar exactamente uno: producto o suministro");
        }
        
        // Validar que el stock reservado no sea mayor al stock total
        if (stockReservado != null && stock != null && stockReservado > stock) {
            throw new IllegalStateException("El stock reservado no puede ser mayor al stock total");
        }
    }
    
    // Getters y Setters
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Almacen getAlmacen() {
        return almacen;
    }
    
    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
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
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Integer getStockReservado() {
        return stockReservado;
    }
    
    public void setStockReservado(Integer stockReservado) {
        this.stockReservado = stockReservado;
    }
    
    public String getLote() {
        return lote;
    }
    
    public void setLote(String lote) {
        this.lote = lote;
    }
    
    // Métodos de utilidad
    public Integer getStockDisponible() {
        return stock - stockReservado;
    }
    
    public String getNombreItem() {
        if (producto != null) {
            return producto.getNombreProducto();
        } else if (suministro != null) {
            return suministro.getNombreSuministro();
        }
        return "N/A";
    }
    
    public String getTipoItem() {
        if (producto != null) {
            return "Producto";
        } else if (suministro != null) {
            return "Suministro";
        }
        return "N/A";
    }
    
    public String getCodigoItem() {
        if (producto != null) {
            return producto.getSerialProducto();
        } else if (suministro != null) {
            return suministro.getCodigoSuministro();
        }
        return "N/A";
    }
    
    @Override
    public String toString() {
        return "InventarioAlmacen{" +
                "idInventario=" + idInventario +
                ", almacen=" + (almacen != null ? almacen.getNombreAlmacen() : "null") +
                ", producto=" + (producto != null ? producto.getNombreProducto() : "null") +
                ", suministro=" + (suministro != null ? suministro.getNombreSuministro() : "null") +
                ", stock=" + stock +
                ", stockReservado=" + stockReservado +
                ", lote='" + lote + '\'' +
                '}';
    }
    
}
