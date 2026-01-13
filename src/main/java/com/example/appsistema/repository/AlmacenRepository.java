package com.example.appsistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Almacen;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Integer> {
    // Buscar almacén por nombre
    Optional<Almacen> findByNombreAlmacen(String nombreAlmacen);
    
    // Verificar si existe un almacén con el nombre (útil para validaciones)
    boolean existsByNombreAlmacen(String nombreAlmacen);
    
    // Buscar almacenes por tipo
    List<Almacen> findByTipoAlmacen(Almacen.TipoAlmacen tipoAlmacen);
    
    // Buscar almacenes por ubicación (contiene texto)
    @Query("SELECT a FROM Almacen a WHERE UPPER(a.ubicacion) LIKE UPPER(CONCAT('%', :ubicacion, '%'))")
    List<Almacen> findByUbicacionContainingIgnoreCase(@Param("ubicacion") String ubicacion);
    
    // Buscar almacenes con paginación y filtro por nombre
    @Query("SELECT a FROM Almacen a WHERE UPPER(a.nombreAlmacen) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    Page<Almacen> findByNombreAlmacenContainingIgnoreCase(@Param("nombre") String nombre, Pageable pageable);
    
    // Obtener todos los almacenes ordenados por nombre
    @Query("SELECT a FROM Almacen a ORDER BY a.nombreAlmacen ASC")
    List<Almacen> findAllOrderByNombreAlmacen();
    
    // Contar almacenes por tipo
    long countByTipoAlmacen(Almacen.TipoAlmacen tipoAlmacen);
}
