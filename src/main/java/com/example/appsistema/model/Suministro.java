package com.example.appsistema.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.appsistema.component.AuditEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="suministros")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
public class Suministro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSuministro")
    private Integer idSuministro;
    
    @Column(name = "nombreSuministro", nullable = false, length = 150)
    private String nombreSuministro;
    
    @Column(name = "codigoSuministro", nullable = false, length = 150)
    private String codigoSuministro;

    @ManyToOne
    @JoinColumn(name = "idTipoItem")
    private TipoItem tipoItem;
    
    
    
    @ManyToOne
    @JoinColumn(name = "idProveedor")
    private Proveedor proveedor;
    
    
    
    @Column(name = "precioCompra", nullable = false, precision = 10, scale = 4)
    private BigDecimal precioCompra;
    
    @Column(name = "stockMinimo", columnDefinition = "INT DEFAULT 0")
    private Integer stockMinimo = 0;
    
    @Column(name = "stockMaximo", columnDefinition = "INT DEFAULT 1000")
    private Integer stockMaximo = 1000;
    
    @Column(name = "loteActual", length = 50)
    private String loteActual;
    
    @Column(name = "imagenURL", length = 255)
    private String imagenURL;
    
    @Column(name = "fechaRegistro")
    private LocalDate fechaRegistro;

    // Constructor vacío
    public Suministro() {}

    // Constructor con parámetros
    public Suministro(Integer idSuministro, String nombreSuministro, String codigoSuministro, 
                     TipoItem tipoItem, Proveedor proveedor,
                     BigDecimal precioCompra, Integer stockMinimo, Integer stockMaximo, 
                     String loteActual, String imagenURL, LocalDate fechaRegistro) {
        this.idSuministro = idSuministro;
        this.nombreSuministro = nombreSuministro;
        this.codigoSuministro = codigoSuministro;
        this.tipoItem = tipoItem;
        
        this.proveedor = proveedor;
   
        this.precioCompra = precioCompra;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.loteActual = loteActual;
        this.imagenURL = imagenURL;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Integer getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(Integer idSuministro) {
        this.idSuministro = idSuministro;
    }

    public String getNombreSuministro() {
        return nombreSuministro;
    }

    public void setNombreSuministro(String nombreSuministro) {
        this.nombreSuministro = nombreSuministro;
    }

    public String getCodigoSuministro() {
        return codigoSuministro;
    }

    public void setCodigoSuministro(String codigoSuministro) {
        this.codigoSuministro = codigoSuministro;
    }

    // ¡Nuevos getters y setters para el objeto TipoItem!
    public TipoItem getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItem tipoItem) { this.tipoItem = tipoItem; }

    
    // Getter and setter for the Proveedor object
    
    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    
    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Integer getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(Integer stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getLoteActual() {
        return loteActual;
    }

    public void setLoteActual(String loteActual) {
        this.loteActual = loteActual;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Nuevo método para obtener la descripción del tipo de ítem
    public String getTipoItemDescripcion() {
    if (this.tipoItem != null && this.tipoItem.getDescripcion() != null) {
        return this.tipoItem.getDescripcion().toString();
    }
    return null;
    }

    
    // New method to get the provider's name
    public String getNombreProveedor() {
        return (this.proveedor != null) ? this.proveedor.getNombreProveedor() : null;
    }
    
}
