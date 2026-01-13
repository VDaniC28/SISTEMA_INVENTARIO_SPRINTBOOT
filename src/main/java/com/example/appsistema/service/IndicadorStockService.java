package com.example.appsistema.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.dto.DetalleStockDTO;
import com.example.appsistema.dto.IndicadorStockDTO;
import com.example.appsistema.dto.SuministroRetrasadoDTO;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.OrdenesCompraDetalle;
import com.example.appsistema.repository.InventarioAlmacenRepository;
import com.example.appsistema.repository.OrdenesCompraRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@Slf4j
public class IndicadorStockService {

    @Autowired
    private InventarioAlmacenRepository inventarioRepository;
    
    @Autowired
    private OrdenesCompraRepository ordenesCompraRepository;

    /**
     * Obtiene todos los indicadores de stock con sus detalles
     */
    public IndicadorStockDTO obtenerIndicadoresStock() {
        log.info("Calculando indicadores de stock...");
        
        IndicadorStockDTO indicadores = new IndicadorStockDTO();
        
        // Obtener todos los inventarios
        List<InventarioAlmacen> inventarios = inventarioRepository.findAll();
        
        // Calcular indicadores de productos
        calcularIndicadoresProductos(inventarios, indicadores);
        
        // Calcular indicadores de suministros
        calcularIndicadoresSuministros(inventarios, indicadores);
        
        // Calcular suministros con entregas retrasadas
        calcularSuministrosRetrasados(indicadores);
        
        log.info("Indicadores calculados exitosamente");
        return indicadores;
    }

    /**
     * Calcula indicadores para productos
     */
    private void calcularIndicadoresProductos(List<InventarioAlmacen> inventarios, 
                                             IndicadorStockDTO indicadores) {
        
        List<InventarioAlmacen> inventariosProductos = inventarios.stream()
            .filter(inv -> inv.getProducto() != null)
            .collect(Collectors.toList());
        
        // Total de productos en stock
        long totalProductos = inventariosProductos.stream()
            .filter(inv -> inv.getStock() > 0)
            .count();
        indicadores.setTotalProductosStock(totalProductos);
        
        // Productos con stock crítico (stock < stockMinimo)
        List<DetalleStockDTO> productosStockCritico = inventariosProductos.stream()
            .filter(inv -> inv.getStock() > 0 && 
                          inv.getProducto().getStockMinimo() != null &&
                          inv.getStock() < inv.getProducto().getStockMinimo())
            .map(this::convertirADetalleStockProducto)
            .collect(Collectors.toList());
        
        indicadores.setProductosStockCritico((long) productosStockCritico.size());
        indicadores.setProductosStockCriticoDetalle(productosStockCritico);
        
        // Productos sin stock
        List<DetalleStockDTO> productosSinStock = inventariosProductos.stream()
            .filter(inv -> inv.getStock() == 0)
            .map(this::convertirADetalleStockProducto)
            .collect(Collectors.toList());
        
        indicadores.setProductosSinStock((long) productosSinStock.size());
        indicadores.setProductosSinStockDetalle(productosSinStock);
        
        // Valor total del inventario de productos
        BigDecimal valorTotal = inventariosProductos.stream()
            .filter(inv -> inv.getStock() > 0)
            .map(inv -> {
                BigDecimal precio = inv.getProducto().getPrecioVenta() != null ? 
                                   inv.getProducto().getPrecioVenta() : BigDecimal.ZERO;
                return precio.multiply(new BigDecimal(inv.getStock()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        indicadores.setValorTotalInventario(valorTotal);
    }

    /**
     * Calcula indicadores para suministros
     */
    private void calcularIndicadoresSuministros(List<InventarioAlmacen> inventarios, 
                                               IndicadorStockDTO indicadores) {
        
        List<InventarioAlmacen> inventariosSuministros = inventarios.stream()
            .filter(inv -> inv.getSuministro() != null)
            .collect(Collectors.toList());
        
        // Total de suministros en stock
        long totalSuministros = inventariosSuministros.stream()
            .filter(inv -> inv.getStock() > 0)
            .count();
        indicadores.setTotalSuministrosStock(totalSuministros);
        
        // Suministros con stock bajo (stock < stockMinimo)
        List<DetalleStockDTO> suministrosStockBajo = inventariosSuministros.stream()
            .filter(inv -> inv.getStock() > 0 && 
                          inv.getSuministro().getStockMinimo() != null &&
                          inv.getStock() < inv.getSuministro().getStockMinimo())
            .map(this::convertirADetalleStockSuministro)
            .collect(Collectors.toList());
        
        indicadores.setSuministrosStockBajo((long) suministrosStockBajo.size());
        indicadores.setSuministrosStockBajoDetalle(suministrosStockBajo);
        
        // Suministros sin stock
        List<DetalleStockDTO> suministrosSinStock = inventariosSuministros.stream()
            .filter(inv -> inv.getStock() == 0)
            .map(this::convertirADetalleStockSuministro)
            .collect(Collectors.toList());
        
        indicadores.setSuministrosSinStock((long) suministrosSinStock.size());
        indicadores.setSuministrosSinStockDetalle(suministrosSinStock);
    }

    /**
     * Calcula suministros con entregas retrasadas
     */
    private void calcularSuministrosRetrasados(IndicadorStockDTO indicadores) {
        LocalDate fechaActual = LocalDate.now();
        
        log.debug("Buscando órdenes con suministros retrasados...");
        
        // Obtener órdenes con fechas de entrega vencidas
        List<OrdenesCompra> ordenesRetrasadas = ordenesCompraRepository
            .findOrdenesConSuministrosRetrasados(fechaActual);
        
        List<SuministroRetrasadoDTO> suministrosRetrasados = new ArrayList<>();
        
        for (OrdenesCompra orden : ordenesRetrasadas) {
            List<OrdenesCompraDetalle> detalles = orden.getDetalles();
            
            if (detalles != null && !detalles.isEmpty()) {
                for (OrdenesCompraDetalle detalle : detalles) {
                    // Solo suministros con cantidad pendiente
                    if (detalle.getSuministro() != null && detalle.getCantidadPendiente() > 0) {
                        SuministroRetrasadoDTO dto = new SuministroRetrasadoDTO(
                            orden.getNumeroOrdenFormateado(),
                            orden.getIdOrdenes(),
                            detalle.getSuministro().getNombreSuministro(),
                            detalle.getSuministro().getCodigoSuministro(),
                            orden.getProveedor() != null ? orden.getProveedor().getNombreProveedor() : "N/A",
                            orden.getFechaEntregaEsperada(),
                            detalle.getCantidadPendiente(),
                            orden.getEstadoOrden().name(),
                            orden.getAlmacen() != null ? orden.getAlmacen().getNombreAlmacen() : "N/A"
                        );
                        suministrosRetrasados.add(dto);
                        
                        log.debug("Suministro retrasado encontrado: {} - {} días", 
                                 detalle.getSuministro().getNombreSuministro(), 
                                 dto.getDiasRetraso());
                    }
                }
            }
        }
        
        indicadores.setSuministrosRetrasados((long) suministrosRetrasados.size());
        indicadores.setSuministrosRetrasadosDetalle(suministrosRetrasados);
        
        log.info("Total de suministros retrasados encontrados: {}", suministrosRetrasados.size());
    }

    /**
     * Convierte InventarioAlmacen de Producto a DetalleStockDTO
     */
    private DetalleStockDTO convertirADetalleStockProducto(InventarioAlmacen inv) {
        DetalleStockDTO detalle = new DetalleStockDTO();
        detalle.setNombre(inv.getProducto().getNombreProducto());
        detalle.setCodigo(inv.getProducto().getSerialProducto());
        detalle.setStockActual(inv.getStock());
        detalle.setStockMinimo(inv.getProducto().getStockMinimo());
        detalle.setAlmacen(inv.getAlmacen().getNombreAlmacen());
        detalle.setProveedor(inv.getProducto().getProveedor() != null ? 
                            inv.getProducto().getProveedor().getNombreProveedor() : "N/A");
        detalle.setLote(inv.getLote() != null ? inv.getLote() : "N/A");
        detalle.setPrecioUnitario(inv.getProducto().getPrecioVenta());
        detalle.setTipoItem("Producto");
        return detalle;
    }

    /**
     * Convierte InventarioAlmacen de Suministro a DetalleStockDTO
     */
    private DetalleStockDTO convertirADetalleStockSuministro(InventarioAlmacen inv) {
        DetalleStockDTO detalle = new DetalleStockDTO();
        detalle.setNombre(inv.getSuministro().getNombreSuministro());
        detalle.setCodigo(inv.getSuministro().getCodigoSuministro());
        detalle.setStockActual(inv.getStock());
        detalle.setStockMinimo(inv.getSuministro().getStockMinimo());
        detalle.setAlmacen(inv.getAlmacen().getNombreAlmacen());
        detalle.setProveedor(inv.getSuministro().getProveedor() != null ? 
                            inv.getSuministro().getProveedor().getNombreProveedor() : "N/A");
        detalle.setLote(inv.getLote() != null ? inv.getLote() : "N/A");
        detalle.setPrecioUnitario(inv.getSuministro().getPrecioCompra());
        detalle.setTipoItem("Suministro");
        return detalle;
    }
}
