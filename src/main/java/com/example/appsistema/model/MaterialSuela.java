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
@Table(name="material_suela")
public class MaterialSuela {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idMaterialSuela")
    private Integer idMaterialSuela;
    
    @NotBlank(message = "La descripción de la suela es obligatoria")
    @Size(max = 150, message = "La descripción no puede exceder 150 caracteres")
    @Column(name="descripcionSuela",unique = true, nullable = false)
    private String descripcionSuela;
    
    // Constructors
    public MaterialSuela() {}
    
    public MaterialSuela(String descripcionSuela) {
        this.descripcionSuela = descripcionSuela;
    }
    
    // Getters and Setters
    public Integer getIdMaterialSuela() { return idMaterialSuela; }
    public void setIdMaterialSuela(Integer idMaterialSuela) { this.idMaterialSuela = idMaterialSuela; }
    
    public String getDescripcionSuela() { return descripcionSuela; }
    public void setDescripcionSuela(String descripcionSuela) { this.descripcionSuela = descripcionSuela; }
}
