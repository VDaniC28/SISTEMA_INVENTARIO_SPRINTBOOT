package com.example.appsistema.dto;

import java.time.LocalDateTime;

import com.example.appsistema.model.Backup;

public class BackupDTO {
    private Integer idBackup;
    private String nombreArchivo;
    private String rutaArchivo;
    private Long tamanoArchivo;
    private String tamanoFormateado;
    private String tipoBackup;
    private String estadoBackup;
    private LocalDateTime fechaBackup;
    private LocalDateTime fechaRegistro;
    private String usuarioUsername;
    private String observaciones;
    private Integer numeroTablas;
    private Integer duracionSegundos;
    
    public BackupDTO(Backup backup) {
        this.idBackup = backup.getIdBackup();
        this.nombreArchivo = backup.getNombreArchivo();
        this.rutaArchivo = backup.getRutaArchivo();
        this.tamanoArchivo = backup.getTamanoArchivo();
        this.tamanoFormateado = backup.getTamanoFormateado();
        this.tipoBackup = backup.getTipoBackup().name();
        this.estadoBackup = backup.getEstadoBackup().name();
        this.fechaBackup = backup.getFechaBackup();
        this.fechaRegistro = backup.getFechaRegistro();
        this.usuarioUsername = backup.getUsuario() != null ? backup.getUsuario().getUsername() : "Sistema";
        this.observaciones = backup.getObservaciones();
        this.numeroTablas = backup.getNumeroTablas();
        this.duracionSegundos = backup.getDuracionSegundos();
    }
    
    // Getters y Setters
    public Integer getIdBackup() { return idBackup; }
    public void setIdBackup(Integer idBackup) { this.idBackup = idBackup; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    
    public Long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(Long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }
    
    public String getTamanoFormateado() { return tamanoFormateado; }
    public void setTamanoFormateado(String tamanoFormateado) { this.tamanoFormateado = tamanoFormateado; }
    
    public String getTipoBackup() { return tipoBackup; }
    public void setTipoBackup(String tipoBackup) { this.tipoBackup = tipoBackup; }
    
    public String getEstadoBackup() { return estadoBackup; }
    public void setEstadoBackup(String estadoBackup) { this.estadoBackup = estadoBackup; }
    
    public LocalDateTime getFechaBackup() { return fechaBackup; }
    public void setFechaBackup(LocalDateTime fechaBackup) { this.fechaBackup = fechaBackup; }
    
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public String getUsuarioUsername() { return usuarioUsername; }
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public Integer getNumeroTablas() { return numeroTablas; }
    public void setNumeroTablas(Integer numeroTablas) { this.numeroTablas = numeroTablas; }
    
    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }
}
