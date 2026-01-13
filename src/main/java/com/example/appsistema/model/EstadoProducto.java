package com.example.appsistema.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="estados_producto")
public class EstadoProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idEstadoProducto")
    private Integer idEstadoProducto;
    
    @NotBlank(message = "La descripción del estado es obligatoria")
    @Size(max = 150, message = "La descripción no puede exceder 150 caracteres")
    @Column(name="descripcionEstado",nullable = false)
    private String descripcionEstado;
    
    @Column(name="afectaStock",nullable = false)
    private Boolean afectaStock = true;
    
    // Constructors
    public EstadoProducto() {}
    
    public EstadoProducto(String descripcionEstado, Boolean afectaStock) {
        this.descripcionEstado = descripcionEstado;
        this.afectaStock = afectaStock;
    }
    
    // Getters and Setters
    public Integer getIdEstadoProducto() { return idEstadoProducto; }
    public void setIdEstadoProducto(Integer idEstadoProducto) { this.idEstadoProducto = idEstadoProducto; }
    
    public String getDescripcionEstado() { return descripcionEstado; }
    public void setDescripcionEstado(String descripcionEstado) { this.descripcionEstado = descripcionEstado; }
    
    public Boolean getAfectaStock() { return afectaStock; }
    public void setAfectaStock(Boolean afectaStock) { this.afectaStock = afectaStock; }
}
