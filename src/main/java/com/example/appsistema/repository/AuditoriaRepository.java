package com.example.appsistema.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Auditoria;
import com.example.appsistema.model.Auditoria.TipoAccion;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    
     // Búsqueda por usuario
    Page<Auditoria> findByIdUsuario(Integer idUsuario, Pageable pageable);
    
    // Búsqueda por tipo de acción
    Page<Auditoria> findByTipoAccion(TipoAccion tipoAccion, Pageable pageable);
    
    // Búsqueda por tabla
    Page<Auditoria> findByNombreTabla(String nombreTabla, Pageable pageable);
    
    // Búsqueda por rango de fechas
    Page<Auditoria> findByFechaAccionBetween(
        LocalDateTime inicio, 
        LocalDateTime fin, 
        Pageable pageable
    );
    
    // Búsqueda avanzada combinada
    @Query("""
        SELECT a FROM Auditoria a 
        WHERE (:idUsuario IS NULL OR a.idUsuario = :idUsuario)
        AND (:tipoAccion IS NULL OR a.tipoAccion = :tipoAccion)
        AND (:nombreTabla IS NULL OR a.nombreTabla = :nombreTabla)
        AND (:fechaInicio IS NULL OR a.fechaAccion >= :fechaInicio)
        AND (:fechaFin IS NULL OR a.fechaAccion <= :fechaFin)
        ORDER BY a.fechaAccion DESC
    """)
    Page<Auditoria> busquedaAvanzada(
        @Param("idUsuario") Integer idUsuario,
        @Param("tipoAccion") TipoAccion tipoAccion,
        @Param("nombreTabla") String nombreTabla,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable
    );
    
    // Estadísticas por tipo de acción
    @Query("""
        SELECT a.tipoAccion, COUNT(a) 
        FROM Auditoria a 
        GROUP BY a.tipoAccion
    """)
    List<Object[]> obtenerEstadisticasPorTipoAccion();
    
    // Estadísticas por tabla
    @Query("""
        SELECT a.nombreTabla, COUNT(a) 
        FROM Auditoria a 
        GROUP BY a.nombreTabla 
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> obtenerEstadisticasPorTabla();
    
    // Últimas acciones por usuario
    @Query("""
        SELECT a FROM Auditoria a 
        WHERE a.idUsuario = :idUsuario 
        ORDER BY a.fechaAccion DESC
    """)
    Page<Auditoria> obtenerUltimasAccionesUsuario(
        @Param("idUsuario") Integer idUsuario, 
        Pageable pageable
    );
    
    // Acciones sobre un registro específico
    @Query("""
        SELECT a FROM Auditoria a 
        WHERE a.nombreTabla = :tabla 
        AND a.idRegistroAfectado = :idRegistro 
        ORDER BY a.fechaAccion DESC
    """)
    List<Auditoria> obtenerHistorialRegistro(
        @Param("tabla") String tabla,
        @Param("idRegistro") Integer idRegistro
    );
}
