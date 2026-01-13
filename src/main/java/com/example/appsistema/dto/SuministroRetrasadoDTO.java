package com.example.appsistema.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SuministroRetrasadoDTO {
    private String numeroOrden;
    private Integer idOrden;
    private String nombreSuministro;
    private String codigoSuministro;
    private String nombreProveedor;
    private LocalDate fechaEntregaEsperada;
    private Integer diasRetraso;
    private Integer cantidadPendiente;
    private String estadoOrden;
    private String almacenDestino;

    // Constructor vacío
    public SuministroRetrasadoDTO() {}

    // Constructor completo
    public SuministroRetrasadoDTO(String numeroOrden, Integer idOrden, String nombreSuministro,
                                 String codigoSuministro, String nombreProveedor,
                                 LocalDate fechaEntregaEsperada, Integer cantidadPendiente,
                                 String estadoOrden, String almacenDestino) {
        this.numeroOrden = numeroOrden;
        this.idOrden = idOrden;
        this.nombreSuministro = nombreSuministro;
        this.codigoSuministro = codigoSuministro;
        this.nombreProveedor = nombreProveedor;
        this.fechaEntregaEsperada = fechaEntregaEsperada;
        this.cantidadPendiente = cantidadPendiente;
        this.estadoOrden = estadoOrden;
        this.almacenDestino = almacenDestino;
        
        // Calcular días de retraso automáticamente
        if (fechaEntregaEsperada != null) {
            this.diasRetraso = (int) ChronoUnit.DAYS.between(fechaEntregaEsperada, LocalDate.now());
        } else {
            this.diasRetraso = 0;
        }
    }

    // Getters y Setters
    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public String getNombreSuministro() {
        return nombreSuministro;
    }

    public void setNombreSuministro(String nombreSuministro) {
        this.nombreSuministro = nombreSuministro;
    }

    public String getCodigoSuministro() {
        return codigoSuministro;
    }

    public void setCodigoSuministro(String codigoSuministro) {
        this.codigoSuministro = codigoSuministro;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public LocalDate getFechaEntregaEsperada() {
        return fechaEntregaEsperada;
    }

    public void setFechaEntregaEsperada(LocalDate fechaEntregaEsperada) {
        this.fechaEntregaEsperada = fechaEntregaEsperada;
        // Recalcular días de retraso
        if (fechaEntregaEsperada != null) {
            this.diasRetraso = (int) ChronoUnit.DAYS.between(fechaEntregaEsperada, LocalDate.now());
        }
    }

    public Integer getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(Integer diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    public Integer getCantidadPendiente() {
        return cantidadPendiente;
    }

    public void setCantidadPendiente(Integer cantidadPendiente) {
        this.cantidadPendiente = cantidadPendiente;
    }

    public String getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(String estadoOrden) {
        this.estadoOrden = estadoOrden;
    }

    public String getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(String almacenDestino) {
        this.almacenDestino = almacenDestino;
    }

    public String getFechaEntregaEsperadaFormateada() {
        if (fechaEntregaEsperada != null) {
            return fechaEntregaEsperada.toString();
        }
        return "N/A";
    }
}
