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
@Table(name = "marcas")
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idMarca")
    private Integer idMarca;
    
    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name="nombre",unique = true, nullable = false)
    private String nombre;
    
    @Size(max = 50, message = "El origen no puede exceder 50 caracteres")
    @Column(name="origen")
    private String origen;
    
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(name="descripcionMarca")
    private String descripcionMarca;
    
    // Constructors
    public Marca() {}
    
    public Marca(String nombre, String origen, String descripcionMarca) {
        this.nombre = nombre;
        this.origen = origen;
        this.descripcionMarca = descripcionMarca;
    }
    
    // Getters and Setters
    public Integer getIdMarca() { return idMarca; }
    public void setIdMarca(Integer idMarca) { this.idMarca = idMarca; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    
    public String getDescripcionMarca() { return descripcionMarca; }
    public void setDescripcionMarca(String descripcionMarca) { this.descripcionMarca = descripcionMarca; }
}
