package com.example.appsistema.model;

import java.util.List;

import com.example.appsistema.component.AuditEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="almacenes")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
public class Almacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlmacen")
    private Integer idAlmacen;
    
    @Column(name = "nombreAlmacen", nullable = false, length = 150)
    private String nombreAlmacen;
    
    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;
    
    @Column(name = "capacidadMaxima")
    private Integer capacidadMaxima;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoAlmacen", columnDefinition = "ENUM('Principal', 'Sucursal', 'Transito') DEFAULT 'Principal'")
    private TipoAlmacen tipoAlmacen;

    @JsonIgnore
    @OneToMany(mappedBy = "almacen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventarioAlmacen> inventarios;

    public enum TipoAlmacen {
        Principal, Sucursal, Transito
    }

    // Constructor vacío
    public Almacen() {}

    // Constructor con parámetros
    public Almacen(Integer idAlmacen, String nombreAlmacen, String ubicacion, 
                   Integer capacidadMaxima, TipoAlmacen tipoAlmacen) {
        this.idAlmacen = idAlmacen;
        this.nombreAlmacen = nombreAlmacen;
        this.ubicacion = ubicacion;
        this.capacidadMaxima = capacidadMaxima;
        this.tipoAlmacen = tipoAlmacen;
    }

    // Getters y Setters
    public Integer getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(Integer idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public TipoAlmacen getTipoAlmacen() {
        return tipoAlmacen;
    }

    public void setTipoAlmacen(TipoAlmacen tipoAlmacen) {
        this.tipoAlmacen = tipoAlmacen;
    }

    public List<InventarioAlmacen> getInventarios() {
        return inventarios;
    }
    
    public void setInventarios(List<InventarioAlmacen> inventarios) {
        this.inventarios = inventarios;
    }
}
