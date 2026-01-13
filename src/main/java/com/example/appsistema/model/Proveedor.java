package com.example.appsistema.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.example.appsistema.component.AuditEntityListener;

@Entity
@Table(name="proveedores")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
public class Proveedor {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProveedor")
    private Integer idProveedor;
    
    @Column(name="nombreProveedor",nullable = false, length = 150)
    private String nombreProveedor;
    
    @Enumerated(EnumType.STRING)
    @Column(name="tipoDocumento",nullable = false)
    private TipoDocumento tipoDocumento;
    
    @Column(name="numeroDocumento",nullable = false, unique = true, length = 11)
    private String numeroDocumento;
    
    @Enumerated(EnumType.STRING)
    @Column(name="tipoPersona",nullable = false)
    private TipoPersona tipoPersona;
    
    @Column(name="email",nullable = false, length = 300)
    private String email;
    
    @Column(name="telefono",length = 13)
    private String telefono;
    
    @Column(name="direccion",length = 200)
    private String direccion;
    
    @Column(name="estado",nullable = false)
    private Boolean estado = true;
    
    @Column(name="fechaRegistro",nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    // Constructores
    public Proveedor() {}

     public Proveedor(String nombreProveedor, TipoDocumento tipoDocumento, 
                    String numeroDocumento, TipoPersona tipoPersona, 
                    String email, String telefono, String direccion) {
        this.nombreProveedor = nombreProveedor;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.tipoPersona = tipoPersona;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }
    
    public String getNombreProveedor() {
        return nombreProveedor;
    }
    
    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }
    
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    
    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }
    
    public void setTipoPersona(TipoPersona tipoPersona) {
        this.tipoPersona = tipoPersona;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    // Enums
    public enum TipoDocumento {
        DNI, RUC
    }
    
    public enum TipoPersona {
        JURIDICA, NATURAL
    }

}
