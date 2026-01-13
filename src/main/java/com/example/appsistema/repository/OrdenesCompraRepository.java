package com.example.appsistema.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.Proveedor;

@Repository
public interface OrdenesCompraRepository extends JpaRepository<OrdenesCompra, Integer> {
    
    // Buscar por estado de orden
    List<OrdenesCompra> findByEstadoOrden(OrdenesCompra.EstadoOrden estadoOrden);
    
    Page<OrdenesCompra> findByEstadoOrden(OrdenesCompra.EstadoOrden estadoOrden, Pageable pageable);
    
    // Buscar por proveedor
    List<OrdenesCompra> findByProveedor(Proveedor proveedor);
    
    Page<OrdenesCompra> findByProveedor(Proveedor proveedor, Pageable pageable);
    
    // Buscar por almacén
    List<OrdenesCompra> findByAlmacen(Almacen almacen);
    
    Page<OrdenesCompra> findByAlmacen(Almacen almacen, Pageable pageable);
    
    // Buscar por rango de fechas de orden
    List<OrdenesCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    Page<OrdenesCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    
    // Buscar por rango de fechas de entrega esperada
    List<OrdenesCompra> findByFechaEntregaEsperadaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    // Buscar órdenes vencidas (fecha entrega esperada pasada y estado Confirmada)
    @Query("SELECT o FROM OrdenesCompra o WHERE o.fechaEntregaEsperada < :fechaActual AND o.estadoOrden = :estado")
    List<OrdenesCompra> findOrdenesVencidas(@Param("fechaActual") LocalDate fechaActual, 
                                           @Param("estado") OrdenesCompra.EstadoOrden estado);
    
    // Filtros combinados más comunes
    
    @Query("SELECT o FROM OrdenesCompra o " +
        "WHERE (:proveedor IS NULL OR o.proveedor = :proveedor) AND " +
        "(:almacen IS NULL OR o.almacen = :almacen) AND " +
        "(:estado IS NULL OR o.estadoOrden = :estado) AND " +
        "(:fechaInicio IS NULL OR o.fechaOrden >= :fechaInicio) AND " +
        "(:fechaFin IS NULL OR o.fechaOrden <= :fechaFin)")
    Page<OrdenesCompra> findWithFilters(@Param("proveedor") Proveedor proveedor,
                                    @Param("almacen") Almacen almacen,
                                    @Param("estado") OrdenesCompra.EstadoOrden estado,
                                    @Param("fechaInicio") LocalDate fechaInicio,
                                    @Param("fechaFin") LocalDate fechaFin,
                                    Pageable pageable);

    // Agrega este nuevo método para cargar las relaciones
    @Query("SELECT DISTINCT o FROM OrdenesCompra o " +
        "LEFT JOIN FETCH o.proveedor " +
        "LEFT JOIN FETCH o.almacen " +
        "WHERE o IN :ordenes")
    List<OrdenesCompra> findWithDetails(@Param("ordenes") List<OrdenesCompra> ordenes);

    // Método específico para cargar una orden con sus relaciones
    @Query("SELECT o FROM OrdenesCompra o " +
        "LEFT JOIN FETCH o.proveedor " +
        "LEFT JOIN FETCH o.almacen " +
        "WHERE o.idOrdenes = :id")
    Optional<OrdenesCompra> findByIdWithDetails(@Param("id") Integer id);
    
    // Contar órdenes por estado
    long countByEstadoOrden(OrdenesCompra.EstadoOrden estadoOrden);
    
    // Buscar órdenes del día actual
    @Query("SELECT o FROM OrdenesCompra o WHERE DATE(o.fechaOrden) = CURRENT_DATE")
    List<OrdenesCompra> findOrdenesDelDia();
    
    // Buscar órdenes del mes actual
    @Query("SELECT o FROM OrdenesCompra o WHERE YEAR(o.fechaOrden) = YEAR(CURRENT_DATE) AND MONTH(o.fechaOrden) = MONTH(CURRENT_DATE)")
    List<OrdenesCompra> findOrdenesDelMes();
    
    // Buscar órdenes por ID del proveedor
    @Query("SELECT o FROM OrdenesCompra o WHERE o.proveedor.idProveedor = :idProveedor")
    List<OrdenesCompra> findByProveedorId(@Param("idProveedor") Integer idProveedor);
    
    // Buscar órdenes por ID del almacén
    @Query("SELECT o FROM OrdenesCompra o WHERE o.almacen.idAlmacen = :idAlmacen")
    List<OrdenesCompra> findByAlmacenId(@Param("idAlmacen") Integer idAlmacen);
    
    // Obtener estadísticas de órdenes
    @Query("SELECT o.estadoOrden, COUNT(o) FROM OrdenesCompra o GROUP BY o.estadoOrden")
    List<Object[]> getEstadisticasPorEstado();
    
    // Buscar órdenes que requieren recepción (Confirmadas sin fecha real)
    @Query("SELECT o FROM OrdenesCompra o WHERE o.estadoOrden = 'Confirmada' AND o.fechaEntregaReal IS NULL")
    List<OrdenesCompra> findOrdenesParaRecepcion();
    
    // Buscar las últimas órdenes creadas
    @Query("SELECT o FROM OrdenesCompra o ORDER BY o.fechaOrden DESC, o.idOrdenes DESC")
    List<OrdenesCompra> findTopByOrderByFechaOrdenDescIdOrdenesDesc(Pageable pageable);
    
    // Buscar órdenes con entrega próxima (en los próximos X días)
    @Query("SELECT o FROM OrdenesCompra o WHERE o.fechaEntregaEsperada BETWEEN CURRENT_DATE AND :fechaLimite AND o.estadoOrden = 'Confirmada'")
    List<OrdenesCompra> findOrdenesConEntregaProxima(@Param("fechaLimite") LocalDate fechaLimite);
    
    // Verificar si existe una orden con el mismo proveedor en un rango de fechas
    @Query("SELECT COUNT(o) > 0 FROM OrdenesCompra o WHERE o.proveedor = :proveedor AND o.fechaOrden BETWEEN :fechaInicio AND :fechaFin")
    boolean existsOrdenByProveedorInDateRange(@Param("proveedor") Proveedor proveedor,
                                            @Param("fechaInicio") LocalDate fechaInicio,
                                            @Param("fechaFin") LocalDate fechaFin);
    
    // Obtener el monto total de órdenes por estado
    @Query("SELECT COALESCE(SUM(o.montoSubtotal + COALESCE(o.impuestos, 0) - COALESCE(o.descuentos, 0)), 0) FROM OrdenesCompra o WHERE o.estadoOrden = :estado")
    Double getMontoTotalByEstado(@Param("estado") OrdenesCompra.EstadoOrden estado);
    
    // Buscar órdenes con monto mayor a un valor específico
    @Query("SELECT o FROM OrdenesCompra o WHERE (o.montoSubtotal + COALESCE(o.impuestos, 0) - COALESCE(o.descuentos, 0)) >= :montoMinimo")
    List<OrdenesCompra> findOrdenesConMontoMayorA(@Param("montoMinimo") Double montoMinimo);
    
    // Obtener órdenes ordenadas por fecha de orden descendente
    List<OrdenesCompra> findAllByOrderByFechaOrdenDesc();
    
    // Obtener órdenes ordenadas por monto total descendente
    @Query("SELECT o FROM OrdenesCompra o ORDER BY (o.montoSubtotal + COALESCE(o.impuestos, 0) - COALESCE(o.descuentos, 0)) DESC")
    List<OrdenesCompra> findAllOrderByMontoTotalDesc();

    //VISTA GUIA
    @Query("SELECT o FROM OrdenesCompra o WHERE o.estadoOrden = 'Confirmada' " +
           "AND NOT EXISTS (SELECT g FROM GuiaRemision g WHERE g.ordenCompra.idOrdenes = o.idOrdenes) " +
           "ORDER BY o.fechaOrden DESC")
    List<OrdenesCompra> findOrdenesConfirmadasSinGuia();
    
    @Query("SELECT o FROM OrdenesCompra o WHERE o.estadoOrden = 'Confirmada' " +
           "AND NOT EXISTS (SELECT g FROM GuiaRemision g WHERE g.ordenCompra.idOrdenes = o.idOrdenes) " +
           "ORDER BY o.fechaOrden DESC")
    Page<OrdenesCompra> findOrdenesConfirmadasSinGuia(Pageable pageable);

    // ⭐ NUEVA CONSULTA con JOIN FETCH
    @Query("SELECT o FROM OrdenesCompra o " +
       "LEFT JOIN FETCH o.proveedor " + 
       "LEFT JOIN FETCH o.almacen " +   
       // The property is 'ordenCompra'
       "WHERE o.estadoOrden = 'Confirmada' AND NOT EXISTS (" +
       "    SELECT g FROM GuiaRemision g WHERE g.ordenCompra = o" + 
       ")")
    Page<OrdenesCompra> findAllWithProveedorAndAlmacen(Pageable pageable);

    // ⭐ NUEVA CONSULTA: Órdenes con suministros retrasados
    @Query("SELECT o FROM OrdenesCompra o " +
        "LEFT JOIN FETCH o.proveedor " +
        "LEFT JOIN FETCH o.almacen " +
        "WHERE o.fechaEntregaEsperada < :fechaActual " +
        "AND o.fechaEntregaReal IS NULL " +
        "AND o.estadoOrden IN ('Pendiente', 'Confirmada', 'RecepcionParcial')")
    List<OrdenesCompra> findOrdenesConSuministrosRetrasados(@Param("fechaActual") LocalDate fechaActual);

}
