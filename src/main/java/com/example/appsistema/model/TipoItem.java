package com.example.appsistema.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tipo_item")
public class TipoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idTipoItem")
    private Integer idTipoItem;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "La descripción es obligatoria")
    @Column(name="descripcion",nullable = false)
    private TipoDescripcion descripcion;
    
    public enum TipoDescripcion {
        Producto, Suministro
    }
    
    // Constructors
    public TipoItem() {}
    
    public TipoItem(TipoDescripcion descripcion) {
        this.descripcion = descripcion;
    }
    
    // Getters and Setters
    public Integer getIdTipoItem() { return idTipoItem; }
    public void setIdTipoItem(Integer idTipoItem) { this.idTipoItem = idTipoItem; }
    
    public TipoDescripcion getDescripcion() { return descripcion; }
    public void setDescripcion(TipoDescripcion descripcion) { this.descripcion = descripcion; }
}
