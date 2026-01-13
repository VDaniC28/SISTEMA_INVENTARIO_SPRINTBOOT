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
@Table(name="tallas")
public class Talla {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idTalla")
    private Integer idTalla;
    
    @NotBlank(message = "El valor de la talla es obligatorio")
    @Size(max = 50, message = "El valor no puede exceder 50 caracteres")
    @Column(name="valor",unique = true, nullable = false)
    private String valor;
    
    // Constructors
    public Talla() {}
    
    public Talla(String valor) {
        this.valor = valor;
    }
    
    // Getters and Setters
    public Integer getIdTalla() { return idTalla; }
    public void setIdTalla(Integer idTalla) { this.idTalla = idTalla; }
    
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
