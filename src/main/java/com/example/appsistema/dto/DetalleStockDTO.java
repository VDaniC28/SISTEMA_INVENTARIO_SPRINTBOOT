package com.example.appsistema.dto;

import java.math.BigDecimal;

public class DetalleStockDTO {
    private String nombre;
    private String codigo;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer diferencia;
    private String almacen;
    private String proveedor;
    private String lote;
    private BigDecimal precioUnitario;
    private String tipoItem; // "Producto" o "Suministro"

    // Constructor vacío
    public DetalleStockDTO() {}

    // Constructor completo
    public DetalleStockDTO(String nombre, String codigo, Integer stockActual, 
                          Integer stockMinimo, String almacen, String proveedor,
                          String lote, BigDecimal precioUnitario, String tipoItem) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.diferencia = stockMinimo - stockActual;
        this.almacen = almacen;
        this.proveedor = proveedor;
        this.lote = lote;
        this.precioUnitario = precioUnitario;
        this.tipoItem = tipoItem;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
        if (this.stockMinimo != null) {
            this.diferencia = this.stockMinimo - stockActual;
        }
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
        if (this.stockActual != null) {
            this.diferencia = stockMinimo - this.stockActual;
        }
    }

    public Integer getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(Integer diferencia) {
        this.diferencia = diferencia;
    }

    public String getAlmacen() {
        return almacen;
    }

    public void setAlmacen(String almacen) {
        this.almacen = almacen;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }
}
