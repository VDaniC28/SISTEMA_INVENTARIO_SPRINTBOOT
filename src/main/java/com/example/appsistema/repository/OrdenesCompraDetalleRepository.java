package com.example.appsistema.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.OrdenesCompraDetalle;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Suministro;

@Repository
public interface OrdenesCompraDetalleRepository extends JpaRepository<OrdenesCompraDetalle, Integer> {
    
    // Buscar todos los detalles de una orden específica
    List<OrdenesCompraDetalle> findByOrdenCompra(OrdenesCompra ordenCompra);
    
    // Buscar detalles por ID de la orden
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.ordenCompra.idOrdenes = :idOrden")
    List<OrdenesCompraDetalle> findByOrdenCompraId(@Param("idOrden") Integer idOrden);
    
    // Buscar detalles que contengan un producto específico
    List<OrdenesCompraDetalle> findByProducto(Producto producto);
    
    // Buscar detalles que contengan un suministro específico
    List<OrdenesCompraDetalle> findBySuministro(Suministro suministro);
    
    // Buscar detalles por ID de producto
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.producto.idProducto = :idProducto")
    List<OrdenesCompraDetalle> findByProductoId(@Param("idProducto") Integer idProducto);
    
    // Buscar detalles por ID de suministro
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.suministro.idSuministro = :idSuministro")
    List<OrdenesCompraDetalle> findBySuministroId(@Param("idSuministro") Integer idSuministro);
    
    // Contar la cantidad de detalles en una orden
    long countByOrdenCompra(OrdenesCompra ordenCompra);
    
    // Buscar detalles con recepción pendiente (cantidad recibida < cantidad solicitada)
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.cantidadRecibida < d.cantidadSolicitada")
    List<OrdenesCompraDetalle> findDetallesConRecepcionPendiente();
    
    // Buscar detalles con recepción completa
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.cantidadRecibida = d.cantidadSolicitada")
    List<OrdenesCompraDetalle> findDetallesConRecepcionCompleta();
    
    // Buscar detalles con recepción parcial
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.cantidadRecibida > 0 AND d.cantidadRecibida < d.cantidadSolicitada")
    List<OrdenesCompraDetalle> findDetallesConRecepcionParcial();
    
    // Buscar detalles sin recepción
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.cantidadRecibida = 0")
    List<OrdenesCompraDetalle> findDetallesSinRecepcion();
    
    // Calcular el valor total de una orden (suma de todos sus detalles)
    @Query("SELECT COALESCE(SUM(d.cantidadSolicitada * d.precioUnitario - COALESCE(d.cantidadSolicitada * d.descuentoUnitario, 0) + COALESCE(d.cantidadSolicitada * d.impuestoUnitario, 0)), 0) " +
           "FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra")
    BigDecimal calcularValorTotalOrden(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Obtener el subtotal de una orden (sin impuestos ni descuentos)
    @Query("SELECT COALESCE(SUM(d.cantidadSolicitada * d.precioUnitario), 0) FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra")
    BigDecimal calcularSubtotalOrden(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Obtener el total de descuentos de una orden
    @Query("SELECT COALESCE(SUM(d.cantidadSolicitada * COALESCE(d.descuentoUnitario, 0)), 0) FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra")
    BigDecimal calcularTotalDescuentosOrden(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Obtener el total de impuestos de una orden
    @Query("SELECT COALESCE(SUM(d.cantidadSolicitada * COALESCE(d.impuestoUnitario, 0)), 0) FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra")
    BigDecimal calcularTotalImpuestosOrden(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Buscar detalles por rango de precios
    List<OrdenesCompraDetalle> findByPrecioUnitarioBetween(BigDecimal precioMinimo, BigDecimal precioMaximo);
    
    // Buscar detalles con cantidad mayor a un valor específico
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.cantidadSolicitada >= :cantidadMinima")
    List<OrdenesCompraDetalle> findDetallesConCantidadMayorA(@Param("cantidadMinima") Integer cantidadMinima);
    
    // Obtener los productos más solicitados (por cantidad total)
    @Query("SELECT d.producto, SUM(d.cantidadSolicitada) as totalSolicitado " +
           "FROM OrdenesCompraDetalle d WHERE d.producto IS NOT NULL " +
           "GROUP BY d.producto ORDER BY totalSolicitado DESC")
    List<Object[]> getProductosMasSolicitados();
    
    // Obtener los suministros más solicitados (por cantidad total)
    @Query("SELECT d.suministro, SUM(d.cantidadSolicitada) as totalSolicitado " +
           "FROM OrdenesCompraDetalle d WHERE d.suministro IS NOT NULL " +
           "GROUP BY d.suministro ORDER BY totalSolicitado DESC")
    List<Object[]> getSuministrosMasSolicitados();
    
    // Buscar detalles de una orden con estado específico
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.ordenCompra.estadoOrden = :estado")
    List<OrdenesCompraDetalle> findByEstadoOrden(@Param("estado") OrdenesCompra.EstadoOrden estado);
    
    // Obtener el porcentaje promedio de recepción de una orden
    @Query("SELECT AVG(CAST(d.cantidadRecibida AS double) / CAST(d.cantidadSolicitada AS double) * 100) " +
           "FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra AND d.cantidadSolicitada > 0")
    Double getPorcentajePromedioRecepcion(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Verificar si todos los detalles de una orden están completamente recibidos
    @Query("SELECT COUNT(d) = 0 FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra AND d.cantidadRecibida < d.cantidadSolicitada")
    boolean todosLosDetallesCompletamenteRecibidos(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Buscar detalles que requieren recepción en una orden específica
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra AND d.cantidadRecibida < d.cantidadSolicitada")
    List<OrdenesCompraDetalle> findDetallesParaRecepcion(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Obtener el valor pendiente de recibir de una orden
    @Query("SELECT COALESCE(SUM((d.cantidadSolicitada - d.cantidadRecibida) * d.precioUnitario), 0) " +
           "FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra")
    BigDecimal getValorPendienteRecepcion(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Eliminar todos los detalles de una orden específica
    void deleteByOrdenCompra(OrdenesCompra ordenCompra);
    
    // Buscar el detalle más caro de una orden
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra ORDER BY d.precioUnitario DESC")
    List<OrdenesCompraDetalle> findDetallesMasCaros(@Param("ordenCompra") OrdenesCompra ordenCompra);
    
    // Obtener estadísticas de recepción por orden
    @Query("SELECT d.ordenCompra, " +
           "SUM(d.cantidadSolicitada) as totalSolicitado, " +
           "SUM(d.cantidadRecibida) as totalRecibido, " +
           "COUNT(d) as totalItems " +
           "FROM OrdenesCompraDetalle d " +
           "GROUP BY d.ordenCompra")
    List<Object[]> getEstadisticasRecepcionPorOrden();
    
    // Buscar detalles ordenados por valor total descendente
    @Query("SELECT d FROM OrdenesCompraDetalle d WHERE d.ordenCompra = :ordenCompra " +
           "ORDER BY (d.cantidadSolicitada * d.precioUnitario) DESC")
    List<OrdenesCompraDetalle> findByOrdenCompraOrderByValorDesc(@Param("ordenCompra") OrdenesCompra ordenCompra);

    //VISTA GUIA

    List<OrdenesCompraDetalle> findByOrdenCompraIdOrdenes(Integer idOrdenes);
}
