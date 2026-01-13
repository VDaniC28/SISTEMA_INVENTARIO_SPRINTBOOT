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
@Table(name="tipo_Persona")
public class TipoPersona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idTipoPersona")
    private Integer idTipoPersona;
    
    @NotBlank(message = "El nombre del tipo de persona es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name="nombreTipoPersona",unique = true, nullable = false)
    private String nombreTipoPersona;
    
    // Constructors
    public TipoPersona() {}
    
    public TipoPersona(String nombreTipoPersona) {
        this.nombreTipoPersona = nombreTipoPersona;
    }
    
    // Getters and Setters
    public Integer getIdTipoPersona() { return idTipoPersona; }
    public void setIdTipoPersona(Integer idTipoPersona) { this.idTipoPersona = idTipoPersona; }
    
    public String getNombreTipoPersona() { return nombreTipoPersona; }
    public void setNombreTipoPersona(String nombreTipoPersona) { this.nombreTipoPersona = nombreTipoPersona; }
}
