package com.example.appsistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Suministro;

import jakarta.persistence.QueryHint;

@Repository
public interface InventarioAlmacenRepository extends JpaRepository<InventarioAlmacen, Integer> {
    
    // Buscar inventario por almacén
    List<InventarioAlmacen> findByAlmacen(Almacen almacen);
    
    // Buscar inventario por almacén con paginación
    Page<InventarioAlmacen> findByAlmacen(Almacen almacen, Pageable pageable);
    
    // Buscar inventario por producto
    List<InventarioAlmacen> findByProducto(Producto producto);
    
    // Buscar inventario por suministro
    List<InventarioAlmacen> findBySuministro(Suministro suministro);
    
    // Buscar inventario por lote
    @Query("SELECT i FROM InventarioAlmacen i WHERE UPPER(i.lote) LIKE UPPER(CONCAT('%', :lote, '%'))")
    List<InventarioAlmacen> findByLoteContainingIgnoreCase(@Param("lote") String lote);
    
    // Buscar inventario específico por almacén y producto
    Optional<InventarioAlmacen> findByAlmacenAndProductoAndLote(Almacen almacen, Producto producto, String lote);
    
    // Buscar inventario específico por almacén y suministro
    Optional<InventarioAlmacen> findByAlmacenAndSuministroAndLote(Almacen almacen, Suministro suministro, String lote);
    
    // ====================================================================
    // MÉTODOS PARA BÚSQUEDA CON FILTROS Y PAGINACIÓN (SOLUCIÓN DOS PASOS)
    // ====================================================================
    
   
    // PASO 1: Solo obtener IDs sin ordenar (compatible con DISTINCT)
       @Query("""
              SELECT DISTINCT i.idInventario 
              FROM InventarioAlmacen i 
              LEFT JOIN i.almacen a 
              LEFT JOIN i.producto p 
              LEFT JOIN i.suministro s 
              WHERE (:almacenId IS NULL OR a.idAlmacen = :almacenId) 
              AND (:productoId IS NULL OR p.idProducto = :productoId) 
              AND (:suministroId IS NULL OR s.idSuministro = :suministroId) 
              AND (:lote IS NULL OR :lote = '' OR UPPER(i.lote) LIKE UPPER(CONCAT('%', :lote, '%')))
       """)
       Page<Integer> findIdsWithFilters(
              @Param("almacenId") Integer almacenId,
              @Param("productoId") Integer productoId,
              @Param("suministroId") Integer suministroId,
              @Param("lote") String lote,
              Pageable pageable
       );

    // PASO 2: Obtener entidades completas por IDs con JOIN FETCH
    @Query("""
        SELECT DISTINCT i FROM InventarioAlmacen i 
        JOIN FETCH i.almacen a 
        LEFT JOIN FETCH i.producto p 
        LEFT JOIN FETCH i.suministro s 
        WHERE i.idInventario IN :ids
        ORDER BY a.nombreAlmacen ASC
    """)
    @QueryHints({
        @QueryHint(name = org.hibernate.jpa.AvailableHints.HINT_CACHE_MODE, value = "REFRESH")
    })
    List<InventarioAlmacen> findByIdsWithDetails(@Param("ids") List<Integer> ids);
    
    // ====================================================================
    // MÉTODO PARA OBTENER UN SOLO INVENTARIO CON DETALLES
    // ====================================================================
    
    @Query("""
        SELECT i FROM InventarioAlmacen i 
        JOIN FETCH i.almacen a 
        LEFT JOIN FETCH i.producto p 
        LEFT JOIN FETCH i.suministro s 
        WHERE i.idInventario = :id
    """)
    Optional<InventarioAlmacen> findByIdWithDetails(@Param("id") Integer id);
    
    // ====================================================================
    // OTROS MÉTODOS DE CONSULTA
    // ====================================================================
    
    // Obtener inventario con stock bajo por almacén
    @Query("SELECT i FROM InventarioAlmacen i WHERE i.almacen = :almacen AND " +
           "((i.producto IS NOT NULL AND i.stock <= i.producto.stockMinimo) OR " +
           "(i.suministro IS NOT NULL AND i.stock <= i.suministro.stockMinimo))")
    List<InventarioAlmacen> findStockBajoByAlmacen(@Param("almacen") Almacen almacen);
    
    // Obtener inventario con stock disponible mayor a cero
    @Query("SELECT i FROM InventarioAlmacen i WHERE (i.stock - i.stockReservado) > 0")
    List<InventarioAlmacen> findWithStockDisponible();
    
    // Contar total de items en inventario por almacén
    @Query("SELECT COUNT(i) FROM InventarioAlmacen i WHERE i.almacen = :almacen")
    long countByAlmacen(@Param("almacen") Almacen almacen);
    
    // Sumar stock total por almacén
    @Query("SELECT COALESCE(SUM(i.stock), 0) FROM InventarioAlmacen i WHERE i.almacen = :almacen")
    Long sumStockByAlmacen(@Param("almacen") Almacen almacen);
    
    // Método para Excel con todos los detalles (sin paginación)
    @Query("""
        SELECT DISTINCT i FROM InventarioAlmacen i 
        JOIN FETCH i.almacen a 
        LEFT JOIN FETCH i.producto p 
        LEFT JOIN FETCH i.suministro s 
        WHERE (:idAlmacen IS NULL OR i.almacen.idAlmacen = :idAlmacen) 
        AND (:idProducto IS NULL OR i.producto.idProducto = :idProducto) 
        AND (:idSuministro IS NULL OR i.suministro.idSuministro = :idSuministro) 
        AND (:lote IS NULL OR :lote = '' OR i.lote LIKE %:lote%)
        ORDER BY a.nombreAlmacen ASC
    """)
    List<InventarioAlmacen> findForExcelReportWithDetails(
        @Param("idAlmacen") Integer idAlmacen,
        @Param("idProducto") Integer idProducto,
        @Param("idSuministro") Integer idSuministro,
        @Param("lote") String lote
    );
    
    // Verificar si existe inventario para un producto en un almacén específico
    boolean existsByAlmacenAndProducto(Almacen almacen, Producto producto);
    
    // Verificar si existe inventario para un suministro en un almacén específico
    boolean existsByAlmacenAndSuministro(Almacen almacen, Suministro suministro);

    // Métodos para búsqueda por IDs compuestos (usados en recepción de órdenes)
    Optional<InventarioAlmacen> findByAlmacenIdAlmacenAndProductoIdProductoAndLote(
        Integer idAlmacen, Integer idProducto, String lote);
    
    Optional<InventarioAlmacen> findByAlmacenIdAlmacenAndSuministroIdSuministroAndLote(
        Integer idAlmacen, Integer idSuministro, String lote);
    

        @Query("""
            SELECT i FROM InventarioAlmacen i 
            JOIN FETCH i.producto p 
            JOIN FETCH i.almacen a
            WHERE a.idAlmacen = :almacenId
            AND i.producto IS NOT NULL 
            AND i.stock > 0
        """)
        List<InventarioAlmacen> findAvailableProductsByAlmacenId(@Param("almacenId") Integer almacenId);


        @Query("""
            SELECT i FROM InventarioAlmacen i 
            JOIN FETCH i.suministro s 
            JOIN FETCH i.almacen a
            WHERE a.idAlmacen = :almacenId
            AND i.suministro IS NOT NULL 
            AND i.stock > 0
        """)
        List<InventarioAlmacen> findAvailableSuministrosByAlmacenId(@Param("almacenId") Integer almacenId);

        // Este método se genera automáticamente en Spring Data JPA para devolver una lista
    List<InventarioAlmacen> findByAlmacenIdAlmacenAndProductoIdProducto(Integer almacenId, Integer productoId);
    /**
     * Suma el stock de un suministro específico en un almacén, consolidando todos los lotes.
     */
    @Query("SELECT COALESCE(SUM(i.stock), 0) FROM InventarioAlmacen i WHERE i.almacen.idAlmacen = :almacenId AND i.suministro.idSuministro = :suministroId")
    Long sumStockByAlmacenAndSuministro(@Param("almacenId") Integer almacenId, @Param("suministroId") Integer suministroId);
        
}