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

import com.example.appsistema.model.Proveedor;
import com.example.appsistema.model.Suministro;

@Repository
public interface SuministroRepository extends JpaRepository<Suministro, Integer> {
    // ==========================================
    // CONSULTAS BÁSICAS DE BÚSQUEDA
    // ==========================================

    /**
     * Busca suministros por nombre (contiene texto, ignorando mayúsculas)
     */
    List<Suministro> findByNombreSuministroContainingIgnoreCase(String nombreSuministro);

    /**
     * Busca suministro por código exacto
     */
    Optional<Suministro> findByCodigoSuministro(String codigoSuministro);

    /**
     * Verifica si existe un suministro con el código dado
     */
    boolean existsByCodigoSuministro(String codigoSuministro);

    /**
     * Verifica si existe un suministro con el código dado, excluyendo un ID específico
     */
    boolean existsByCodigoSuministroAndIdSuministroNot(String codigoSuministro, Integer idSuministro);

    // ==========================================
    // FILTROS POR ENTIDADES RELACIONADAS
    // ==========================================

    /**
     * Busca suministros por tipo de item
     */
    List<Suministro> findByTipoItem_IdTipoItem(Integer idTipoItem);

   

    /**
     * Busca suministros por proveedor
     */
    List<Suministro> findByProveedor_IdProveedor(Integer idProveedor);

    // ==========================================
    // FILTROS POR FECHA
    // ==========================================

    /**
     * Busca suministros registrados en una fecha específica
     */
    List<Suministro> findByFechaRegistro(LocalDate fechaRegistro);

    /**
     * Busca suministros registrados entre fechas
     */
    List<Suministro> findByFechaRegistroBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca suministros registrados después de una fecha
     */
    List<Suministro> findByFechaRegistroAfter(LocalDate fecha);

    /**
     * Busca suministros registrados antes de una fecha
     */
    List<Suministro> findByFechaRegistroBefore(LocalDate fecha);

    // ==========================================
    // CONSULTAS COMPLEJAS CON @Query
    // ==========================================

    /**
     * Busca suministros con múltiples filtros opcionales
     */
    @Query("SELECT s FROM Suministro s WHERE " +
           "(:nombreSuministro IS NULL OR LOWER(s.nombreSuministro) LIKE LOWER(CONCAT('%', :nombreSuministro, '%'))) AND " +
           "(:idTipoItem IS NULL OR s.tipoItem.idTipoItem = :idTipoItem) AND " +
           "(:idProveedor IS NULL OR s.proveedor.idProveedor = :idProveedor) AND " +
           "(:fechaInicio IS NULL OR s.fechaRegistro >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fechaRegistro <= :fechaFin)")
    List<Suministro> findSuministrosWithFilters(
            @Param("nombreSuministro") String nombreSuministro,
            @Param("idTipoItem") Integer idTipoItem,
            @Param("idProveedor") Integer idProveedor,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Versión paginada de la búsqueda con filtros
     */
    @Query("SELECT s FROM Suministro s WHERE " +
           "(:nombreSuministro IS NULL OR LOWER(s.nombreSuministro) LIKE LOWER(CONCAT('%', :nombreSuministro, '%'))) AND " +
           "(:idTipoItem IS NULL OR s.tipoItem.idTipoItem = :idTipoItem) AND " +
           "(:idProveedor IS NULL OR s.proveedor.idProveedor = :idProveedor) AND " +
           "(:fechaInicio IS NULL OR s.fechaRegistro >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fechaRegistro <= :fechaFin)")
    Page<Suministro> findSuministrosWithFiltersPageable(
            @Param("nombreSuministro") String nombreSuministro,
            @Param("idTipoItem") Integer idTipoItem,
            @Param("idProveedor") Integer idProveedor,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable);

    /**
     * Obtiene suministros con stock bajo (menor al mínimo)
     */
    @Query("SELECT ia.suministro FROM InventarioAlmacen ia WHERE ia.stock <= ia.suministro.stockMinimo")
    List<Suministro> findSuministrosWithLowStock();

    /**
     * Busca suministros por código o nombre
     */
    @Query("SELECT s FROM Suministro s WHERE " +
           "LOWER(s.codigoSuministro) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(s.nombreSuministro) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Suministro> findByCodigoOrNombre(@Param("busqueda") String busqueda);

    /**
     * Obtiene información completa del suministro con sus relaciones
     */
    @Query("SELECT s FROM Suministro s " +
           "LEFT JOIN FETCH s.tipoItem " +
           "LEFT JOIN FETCH s.proveedor " +
           "WHERE s.idSuministro = :idSuministro")
    Optional<Suministro> findByIdWithRelations(@Param("idSuministro") Integer idSuministro);

    /**
     * Obtiene todos los suministros con sus relaciones para reportes
     */
    @Query("SELECT s FROM Suministro s " +
           "LEFT JOIN FETCH s.tipoItem " +
           "LEFT JOIN FETCH s.proveedor " +
           "ORDER BY s.fechaRegistro DESC")
    List<Suministro> findAllWithRelations();

    // ==========================================
    // CONSULTAS DE ESTADÍSTICAS
    // ==========================================


    /**
     * Cuenta suministros por proveedor
     */
    @Query("SELECT COUNT(s) FROM Suministro s WHERE s.proveedor.idProveedor = :idProveedor")
    Long countByProveedor(@Param("idProveedor") Integer idProveedor);

    /**
     * Obtiene los últimos N suministros registrados
     */
    @Query("SELECT s FROM Suministro s ORDER BY s.fechaRegistro DESC, s.idSuministro DESC")
    List<Suministro> findLatestSuministros(Pageable pageable);

    /**
     * Busca suministros por rango de precios
     */
    @Query("SELECT s FROM Suministro s WHERE s.precioCompra BETWEEN :precioMin AND :precioMax")
    List<Suministro> findByPrecioCompraBetween(
            @Param("precioMin") java.math.BigDecimal precioMin,
            @Param("precioMax") java.math.BigDecimal precioMax);

    // ==========================================
    // CONSULTAS PARA VALIDACIONES
    // ==========================================

    /**
     * Busca suministros duplicados por nombre y proveedor
     */
    @Query("SELECT s FROM Suministro s WHERE " +
           "LOWER(s.nombreSuministro) = LOWER(:nombreSuministro) AND " +
           "s.proveedor.idProveedor = :idProveedor AND " +
           "(:idSuministro IS NULL OR s.idSuministro != :idSuministro)")
    List<Suministro> findDuplicatesByNombreAndProveedor(
            @Param("nombreSuministro") String nombreSuministro,
            @Param("idProveedor") Integer idProveedor,
            @Param("idSuministro") Integer idSuministro);

    /**
     * Obtiene suministros ordenados por nombre
     */
    List<Suministro> findAllByOrderByNombreSuministroAsc();

    /**
     * Obtiene suministros ordenados por fecha de registro descendente
     */
    List<Suministro> findAllByOrderByFechaRegistroDesc();
    // Obtener todos los almacenes ordenados por nombre
    @Query("SELECT s FROM Suministro s ORDER BY s.nombreSuministro ASC")
    List<Suministro> findAllOrderByNombreSuministro();

    List<Suministro> findByProveedor(Proveedor proveedor);
}
