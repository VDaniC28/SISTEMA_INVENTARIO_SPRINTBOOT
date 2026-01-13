package com.example.appsistema.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IndicadorStockDTO {
    private Long totalProductosStock;
    private Long totalSuministrosStock;
    private Long productosStockCritico;
    private Long suministrosStockBajo;
    private Long productosSinStock;
    private Long suministrosSinStock;
    private BigDecimal valorTotalInventario;
    // Campo para el nuevo indicador
    private Long suministrosRetrasados;
    private List<SuministroRetrasadoDTO> suministrosRetrasadosDetalle;
    
    // Listas detalladas para los modales
    private List<DetalleStockDTO> productosStockCriticoDetalle;
    private List<DetalleStockDTO> suministrosStockBajoDetalle;
    private List<DetalleStockDTO> productosSinStockDetalle;
    private List<DetalleStockDTO> suministrosSinStockDetalle;
    
    public IndicadorStockDTO() {
        this.productosStockCriticoDetalle = new ArrayList<>();
        this.suministrosStockBajoDetalle = new ArrayList<>();
        this.productosSinStockDetalle = new ArrayList<>();
        this.suministrosSinStockDetalle = new ArrayList<>();
        this.suministrosRetrasadosDetalle = new ArrayList<>();
        this.valorTotalInventario = BigDecimal.ZERO;
    }

    // Getters y Setters
    public Long getTotalProductosStock() {
        return totalProductosStock;
    }

    public void setTotalProductosStock(Long totalProductosStock) {
        this.totalProductosStock = totalProductosStock;
    }

    public Long getTotalSuministrosStock() {
        return totalSuministrosStock;
    }

    public void setTotalSuministrosStock(Long totalSuministrosStock) {
        this.totalSuministrosStock = totalSuministrosStock;
    }

    public Long getProductosStockCritico() {
        return productosStockCritico;
    }

    public void setProductosStockCritico(Long productosStockCritico) {
        this.productosStockCritico = productosStockCritico;
    }

    public Long getSuministrosStockBajo() {
        return suministrosStockBajo;
    }

    public void setSuministrosStockBajo(Long suministrosStockBajo) {
        this.suministrosStockBajo = suministrosStockBajo;
    }

    public Long getProductosSinStock() {
        return productosSinStock;
    }

    public void setProductosSinStock(Long productosSinStock) {
        this.productosSinStock = productosSinStock;
    }

    public Long getSuministrosSinStock() {
        return suministrosSinStock;
    }

    public void setSuministrosSinStock(Long suministrosSinStock) {
        this.suministrosSinStock = suministrosSinStock;
    }

    public BigDecimal getValorTotalInventario() {
        return valorTotalInventario;
    }

    public void setValorTotalInventario(BigDecimal valorTotalInventario) {
        this.valorTotalInventario = valorTotalInventario;
    }

    public List<DetalleStockDTO> getProductosStockCriticoDetalle() {
        return productosStockCriticoDetalle;
    }

    public void setProductosStockCriticoDetalle(List<DetalleStockDTO> productosStockCriticoDetalle) {
        this.productosStockCriticoDetalle = productosStockCriticoDetalle;
    }

    public List<DetalleStockDTO> getSuministrosStockBajoDetalle() {
        return suministrosStockBajoDetalle;
    }

    public void setSuministrosStockBajoDetalle(List<DetalleStockDTO> suministrosStockBajoDetalle) {
        this.suministrosStockBajoDetalle = suministrosStockBajoDetalle;
    }

    public List<DetalleStockDTO> getProductosSinStockDetalle() {
        return productosSinStockDetalle;
    }

    public void setProductosSinStockDetalle(List<DetalleStockDTO> productosSinStockDetalle) {
        this.productosSinStockDetalle = productosSinStockDetalle;
    }

    public List<DetalleStockDTO> getSuministrosSinStockDetalle() {
        return suministrosSinStockDetalle;
    }

    public void setSuministrosSinStockDetalle(List<DetalleStockDTO> suministrosSinStockDetalle) {
        this.suministrosSinStockDetalle = suministrosSinStockDetalle;
    }
    // Agregar getters y setters al final:
    public Long getSuministrosRetrasados() {
        return suministrosRetrasados;
    }

    public void setSuministrosRetrasados(Long suministrosRetrasados) {
        this.suministrosRetrasados = suministrosRetrasados;
    }

    public List<SuministroRetrasadoDTO> getSuministrosRetrasadosDetalle() {
        return suministrosRetrasadosDetalle;
    }

    public void setSuministrosRetrasadosDetalle(List<SuministroRetrasadoDTO> suministrosRetrasadosDetalle) {
        this.suministrosRetrasadosDetalle = suministrosRetrasadosDetalle;
    }
}
