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
@Table(name="material_principal")
public class MaterialPrincipal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idMaterialPrincipal")
    private Integer idMaterialPrincipal;

    @NotBlank(message = "El nombre del material es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name="nombreMaterial",unique = true, nullable = false)
    private String nombreMaterial;
    
    // Constructors
    public MaterialPrincipal() {}
    
    public MaterialPrincipal(String nombreMaterial) {
        this.nombreMaterial = nombreMaterial;
    }
    
    // Getters and Setters
    public Integer getIdMaterialPrincipal() { return idMaterialPrincipal; }
    public void setIdMaterialPrincipal(Integer idMaterialPrincipal) { this.idMaterialPrincipal = idMaterialPrincipal; }
    
    public String getNombreMaterial() { return nombreMaterial; }
    public void setNombreMaterial(String nombreMaterial) { this.nombreMaterial = nombreMaterial; }
}
