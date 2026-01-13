package com.example.appsistema.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "usuario"}) // ← AGREGAR ESTA LÍNEA
public class Auditoria {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAuditoria;
    
    @Column(nullable = false)
    private Integer idUsuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAccion tipoAccion;
    
    @Column(nullable = false, length = 100)
    private String nombreTabla;
    
    private Integer idRegistroAfectado;
    
    @Column(length = 255)
    private String descripcionAccion;
    
    @Column(columnDefinition = "JSON")
    private String valorAnterior;
    
    @Column(columnDefinition = "JSON")
    private String valorNuevo;
    
    @Column(length = 45)
    private String ipAcceso;
    
    @Column(length = 100)
    private String navegador;
    
    @Column(nullable = false)
    private LocalDateTime fechaAccion;
    
    // Relación con Usuario (opcional, para obtener el nombre)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", insertable = false, updatable = false)
    private Usuario usuario;
    
    @PrePersist
    protected void onCreate() {
        if (fechaAccion == null) {
            fechaAccion = LocalDateTime.now();
        }
    }
    
    public enum TipoAccion {
        INSERT, UPDATE, DELETE, LOGIN, LOGOUT, EXPORT, IMPORT
    }
}
