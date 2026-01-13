package com.example.appsistema.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Cliente;
import com.example.appsistema.model.SalidaInventario;
import com.example.appsistema.model.SalidaInventario.EstadoSalida;
import com.example.appsistema.model.SalidaInventario.TipoComprobante;
import com.example.appsistema.model.SalidaInventario.TipoSalida;

@Repository
public interface SalidaInventarioRepository extends JpaRepository<SalidaInventario, Integer> {


                        // VISTA CLIENTE
    
       // 🔹 VISTA CLIENTE — Paginación por cliente (Sin filtros)
       @Query("SELECT s FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente")
       Page<SalidaInventario> findByIdCliente(@Param("idCliente") Integer idCliente, Pageable pageable);

       // 🔹 Filtros dinámicos - CORREGIDO PARA USAR EAGER FETCHING
       // Incluye LEFT JOIN FETCH para detalles, producto y suministro para evitar LazyInitializationException.
       @Query("SELECT DISTINCT s FROM SalidaInventario s " +
              "LEFT JOIN FETCH s.detalles d " +
              "LEFT JOIN FETCH d.producto p " +
              "LEFT JOIN FETCH d.suministro su " +
              "WHERE s.cliente.idCliente = :idCliente " +
              "AND (:fechaInicio IS NULL OR s.fechaSalida >= :fechaInicio) " +
              "AND (:fechaFin IS NULL OR s.fechaSalida <= :fechaFin) " +
              "AND (:tipoComprobante IS NULL OR s.tipoComprobante = :tipoComprobante) " +
              "AND (:estadoSalida IS NULL OR s.estadoSalida = :estadoSalida) " +
              "AND (:tipoSalida IS NULL OR s.tipoSalida = :tipoSalida) " +
              "ORDER BY s.fechaSalida DESC, s.idSalida DESC")
       Page<SalidaInventario> buscarConFiltros(
              @Param("idCliente") Integer idCliente,
              @Param("fechaInicio") LocalDate fechaInicio,
              @Param("fechaFin") LocalDate fechaFin,
              @Param("tipoComprobante") TipoComprobante tipoComprobante,
              @Param("estadoSalida") EstadoSalida estadoSalida,
              @Param("tipoSalida") TipoSalida tipoSalida,
              Pageable pageable
       );

       // 🔹 Total de compras completadas
       @Query("SELECT COUNT(s) FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente " +
              "AND s.estadoSalida = 'Completada'")
       Long contarComprasPorCliente(@Param("idCliente") Integer idCliente);

       // 🔹 Monto total gastado por cliente
       @Query("SELECT COALESCE(SUM(s.montoTotal), 0) FROM SalidaInventario s " +
              "WHERE s.cliente.idCliente = :idCliente AND s.estadoSalida = 'Completada'")
       BigDecimal calcularMontoTotalPorCliente(@Param("idCliente") Integer idCliente);

       // 🔹 Últimas compras (ordenadas por fecha y ID)
       @Query("SELECT s FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente " +
              "ORDER BY s.fechaSalida DESC, s.idSalida DESC")
       List<SalidaInventario> obtenerUltimasCompras(@Param("idCliente") Integer idCliente, Pageable pageable);

       // 🔹 Compras pendientes por cliente
       @Query("SELECT COUNT(s) FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente " +
              "AND s.estadoSalida = 'Pendiente'")
       Long contarComprasPendientesPorCliente(@Param("idCliente") Integer idCliente);
    

    //        VISTA SALIDA DE INVENTARIO

    // 🔹 Búsqueda por número de salida
    Optional<SalidaInventario> findByNumeroSalida(String numeroSalida);

    // 🔹 Verificar existencia por número
    boolean existsByNumeroSalida(String numeroSalida);


     @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.numeroSalida, 4) AS int)), 0) FROM SalidaInventario s WHERE s.numeroSalida LIKE 'SAL%'")
    Integer findMaxNumeroSalida();
    
    @Query("SELECT s FROM SalidaInventario s " +
           "LEFT JOIN FETCH s.cliente c " +
           "LEFT JOIN FETCH s.almacen a " +
           "LEFT JOIN FETCH s.usuario u " +
           "WHERE (:clienteId IS NULL OR s.cliente.idCliente = :clienteId) " +
           "AND (:fechaInicio IS NULL OR s.fechaSalida >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR s.fechaSalida <= :fechaFin) " +
           "AND (:estadoSalida IS NULL OR s.estadoSalida = :estadoSalida) " +
           "AND (:tipoSalida IS NULL OR s.tipoSalida = :tipoSalida) " +
           "AND (:almacenId IS NULL OR s.almacen.idAlmacen = :almacenId)")
    Page<SalidaInventario> findByFilters(
            @Param("clienteId") Integer clienteId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estadoSalida") EstadoSalida estadoSalida,
            @Param("tipoSalida") TipoSalida tipoSalida,
            @Param("almacenId") Integer almacenId,
            Pageable pageable
    );

       @Query(value = "SELECT DISTINCT s FROM SalidaInventario s " + 
                     "LEFT JOIN FETCH s.cliente c " +
                     "LEFT JOIN FETCH s.almacen a " +
                     "LEFT JOIN FETCH s.usuario u " +
                     "LEFT JOIN FETCH s.detalles d " +
                     
                     // 🔑 CORRECCIÓN: Agregar fetch de segundo nivel
                     "LEFT JOIN FETCH d.producto p " +      // Carga la entidad Producto
                     "LEFT JOIN FETCH d.suministro sum " +  // Carga la entidad Suministro
                     
                     "WHERE (:clienteId IS NULL OR s.cliente.idCliente = :clienteId) " +
                     "AND (:fechaInicio IS NULL OR s.fechaSalida >= :fechaInicio) " +
                     "AND (:fechaFin IS NULL OR s.fechaSalida <= :fechaFin) " +
                     "AND (:estadoSalida IS NULL OR s.estadoSalida = :estadoSalida) " +
                     "AND (:tipoSalida IS NULL OR s.tipoSalida = :tipoSalida) " +
                     "AND (:almacenId IS NULL OR s.almacen.idAlmacen = :almacenId) " +
                     "ORDER BY s.fechaSalida DESC",
              
              countQuery = "SELECT COUNT(s) FROM SalidaInventario s " +
                            "WHERE (:clienteId IS NULL OR s.cliente.idCliente = :clienteId) " +
                            "AND (:fechaInicio IS NULL OR s.fechaSalida >= :fechaInicio) " +
                            "AND (:fechaFin IS NULL OR s.fechaSalida <= :fechaFin) " +
                            "AND (:estadoSalida IS NULL OR s.estadoSalida = :estadoSalida) " +
                            "AND (:tipoSalida IS NULL OR s.tipoSalida = :tipoSalida) " +
                            "AND (:almacenId IS NULL OR s.almacen.idAlmacen = :almacenId)")
       Page<SalidaInventario> findByFiltersWithDetails(
              @Param("clienteId") Integer clienteId,
              @Param("fechaInicio") LocalDate fechaInicio,
              @Param("fechaFin") LocalDate fechaFin,
              @Param("estadoSalida") EstadoSalida estadoSalida, // Nota: Tu parámetro es EstadoSalida, no TipoSalida
              @Param("tipoSalida") TipoSalida tipoSalida,
              @Param("almacenId") Integer almacenId,
              Pageable pageable
       );
    
    List<SalidaInventario> findByFechaSalidaBetween(LocalDate inicio, LocalDate fin);
    
    List<SalidaInventario> findByEstadoSalida(EstadoSalida estado);
    
    
      @Query("SELECT s FROM SalidaInventario s " +
              // Fetches de primer nivel (Relaciones de SalidaInventario)
              "LEFT JOIN FETCH s.cliente c " +
              "LEFT JOIN FETCH s.almacen a " +
              "LEFT JOIN FETCH s.usuario u " +
              "LEFT JOIN FETCH s.detalles d " + 
              
              // ** Fetches de segundo nivel (Relaciones dentro de SalidaInventarioDetalle) **
              "LEFT JOIN FETCH d.producto p " +      // Necesario para detalle.getProducto()
              "LEFT JOIN FETCH d.suministro sum " + // Necesario para detalle.getSuministro()
              "LEFT JOIN FETCH d.almacen al " +     // Necesario para detalle.getAlmacen() en el detalle
              
              "WHERE s.idSalida = :id")
       Optional<SalidaInventario> findByIdWithDetalles(@Param("id") Integer id);
  
       // ✅ MÉTODO PARA LISTAR TODAS (Necesario para listarTodas2)
       @Query("SELECT DISTINCT s FROM SalidaInventario s " +
              // Fetches de primer nivel
              "LEFT JOIN FETCH s.cliente c " +
              "LEFT JOIN FETCH s.almacen a " +
              "LEFT JOIN FETCH s.usuario u " +
              "LEFT JOIN FETCH s.detalles d " + 
              // Fetches de segundo nivel
              "LEFT JOIN FETCH d.producto p " + 
              "LEFT JOIN FETCH d.suministro sum " + 
              "LEFT JOIN FETCH d.almacen al " + 
              // CLÁUSULA CRÍTICA: Quitamos el WHERE s.idSalida
              "ORDER BY s.idSalida DESC")
       List<SalidaInventario> findAllWithEagerFetch();

       
       //VISTA GUIA

       @Query("SELECT s FROM SalidaInventario s WHERE s.estadoSalida = 'Completada' " +
           "AND NOT EXISTS (SELECT g FROM GuiaRemision g WHERE g.salidaInventario.idSalida = s.idSalida) " +
           "ORDER BY s.fechaSalida DESC")
       List<SalidaInventario> findSalidasCompletadasSinGuia();
       
       @Query("SELECT s FROM SalidaInventario s WHERE s.estadoSalida = 'Completada' " +
              "AND NOT EXISTS (SELECT g FROM GuiaRemision g WHERE g.salidaInventario.idSalida = s.idSalida) " +
              "ORDER BY s.fechaSalida DESC")
       Page<SalidaInventario> findSalidasCompletadasSinGuia(Pageable pageable);
       
       // ⭐ NUEVA CONSULTA con JOIN FETCH
      @Query("SELECT s FROM SalidaInventario s " +
              "LEFT JOIN FETCH s.cliente " +
              // The property is 'salidaInventario', not 'salida'
              "WHERE s.estadoSalida = 'Completada' AND NOT EXISTS (" +
              "    SELECT g FROM GuiaRemision g WHERE g.salidaInventario = s" + 
              ")")
       Page<SalidaInventario> findAllWithCliente(Pageable pageable);


      // VISTA DE DEUDA A PAGAR
      
       @Query("SELECT s FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente " +
              "AND s.tipoSalida = 'Venta' AND s.estadoSalida != 'Anulada' " +
              "ORDER BY s.fechaSalida DESC")
       List<SalidaInventario> findVentasByCliente(@Param("idCliente") Integer idCliente);
       
       @Query("SELECT COALESCE(SUM(s.montoTotal), 0) FROM SalidaInventario s " +
              "WHERE s.cliente.idCliente = :idCliente AND s.tipoSalida = 'Venta' " +
              "AND s.estadoSalida != 'Anulada'")
       BigDecimal calcularTotalVentasPorCliente(@Param("idCliente") Integer idCliente);
       
       @Query("SELECT COUNT(s) FROM SalidaInventario s WHERE s.cliente.idCliente = :idCliente " +
              "AND s.tipoSalida = 'Venta' AND s.estadoSalida != 'Anulada'")
       Integer contarVentasPorCliente(@Param("idCliente") Integer idCliente);
       
       @Query("SELECT DISTINCT s.cliente FROM SalidaInventario s WHERE s.tipoSalida = 'Venta' " +
              "AND s.estadoSalida != 'Anulada'")
       List<Cliente> findClientesConVentas();

              // Busca las salidas (ventas) por cliente, tipo de salida y que NO sean un estado específico
       public abstract Page<SalidaInventario> findByClienteIdClienteAndTipoSalidaAndEstadoSalidaNot(
              Integer idCliente, 
              SalidaInventario.TipoSalida tipoSalida, 
              SalidaInventario.EstadoSalida estadoSalida, 
              Pageable pageable
       );

       
       

       @Query(value = """
              SELECT 
              COALESCE(SUM(v.montoTotal), 0) AS totalVentas,
              COALESCE(SUM(p.montoAbono), 0) AS totalAbonado,
              MAX(v.fechaSalida) AS fechaUltimaVenta,
              MAX(p.fechaAbono) AS fechaUltimoAbono,
              COUNT(DISTINCT v.idSalida) AS cantidadVentas,
              COUNT(p.idPago) AS cantidadAbonos
              FROM clientes c
              LEFT JOIN salida_inventario v ON c.idCliente = v.idCliente AND v.tipoSalida = 'Venta' AND v.estadoSalida != 'Anulada'
              LEFT JOIN pagos_clientes p ON c.idCliente = p.idCliente
              WHERE c.idCliente = :idCliente
              GROUP BY c.idCliente
              """, nativeQuery = true)
       Object[] findResumenContableByCliente(@Param("idCliente") Integer idCliente);

       // Nuevo método a agregar en SalidaInventarioRepository:

       Page<SalidaInventario> findByClienteIdCliente(Integer idCliente, Pageable pageable);

       List<SalidaInventario> findByClienteIdClienteAndTipoSalidaAndEstadoSalidaOrderByFechaSalidaDesc(
              Integer idCliente, 
              SalidaInventario.TipoSalida tipoSalida, 
              SalidaInventario.EstadoSalida estadoSalida
       );
}
