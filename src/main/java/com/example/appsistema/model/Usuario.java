package com.example.appsistema.model;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "estado")
    private Boolean estado;
    
    @Column(name = "FechaRegistro")
    @Temporal(TemporalType.DATE)  
    private Date fechaRegistro;

    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
    name = "usuario_roles",
    joinColumns = @JoinColumn(name = "idUsuario"),
    inverseJoinColumns = @JoinColumn(name = "idRol"))

    private Set<Rol> roles;

    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
    name = "usuario_empresas",
    joinColumns = @JoinColumn(name = "idUsuario"),
    inverseJoinColumns = @JoinColumn(name = "idEmpresa"))

    private Set<Empresa> empresas;

    public Integer getIdUsuario() {
        return idUsuario;
    }   

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    } 

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEstado() {
        return estado;
    }
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    public Boolean isEstado() {
        return estado;
    }
    public Date getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Set<Rol> getRoles() {
        return roles;
    }
    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public Set<Empresa> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(Set<Empresa> empresas) {
        this.empresas = empresas;
    }

    
}
