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
@Table(name="color")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idColor")
    private Integer idColor;
    
    @NotBlank(message = "El nombre del color es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombreColor",unique = true, nullable = false)
    private String nombreColor;
    
    // Constructors
    public Color() {}
    
    public Color(String nombreColor) {
        this.nombreColor = nombreColor;
    }
    
    // Getters and Setters
    public Integer getIdColor() { return idColor; }
    public void setIdColor(Integer idColor) { this.idColor = idColor; }
    
    public String getNombreColor() { return nombreColor; }
    public void setNombreColor(String nombreColor) { this.nombreColor = nombreColor; }
}
