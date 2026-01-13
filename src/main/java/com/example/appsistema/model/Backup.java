package com.example.appsistema.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Backup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBackup;
    
    @Column(nullable = false)
    private String nombreArchivo;
    
    @Column(nullable = false, length = 500)
    private String rutaArchivo;
    
    @Column(nullable = false)
    private Long tamanoArchivo= 0L; // En bytes
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBackup tipoBackup = TipoBackup.COMPLETO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoBackup estadoBackup = EstadoBackup.EN_PROCESO;
    
    @Column(nullable = false)
    private LocalDateTime fechaBackup;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false) // updatable=false es clave
    private LocalDateTime fechaRegistro;
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "password"}) // AGREGAR ESTA LÍNEA
    private Usuario usuario;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    private Integer numeroTablas = 0;
    
    private Integer duracionSegundos = 0;
    
    public enum TipoBackup {
        COMPLETO, INCREMENTAL, DIFERENCIAL
    }
    
    public enum EstadoBackup {
        EXITOSO, FALLIDO, EN_PROCESO
    }
    
    // Método auxiliar para obtener tamaño formateado
    @Transient
    public String getTamanoFormateado() {
        if (tamanoArchivo == null) return "0 B";
        
        double size = tamanoArchivo;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
