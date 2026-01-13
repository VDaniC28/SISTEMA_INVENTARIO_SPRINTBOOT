package com.example.appsistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.GuiaRemision;

@Repository
public interface GuiaRemisionRepository extends JpaRepository<GuiaRemision, Integer> {
    
    Optional<GuiaRemision> findByNumeroGuia(String numeroGuia);
    Page<GuiaRemision> findByEmpresaIdEmpresaOrderByFechaEmisionDesc(Integer idEmpresa, Pageable pageable);
    List<GuiaRemision> findByEmpresaIdEmpresaOrderByFechaEmisionDesc(Integer idEmpresa);
    
    @Query("SELECT MAX(g.correlativoGuia) FROM GuiaRemision g WHERE g.empresa.idEmpresa = :idEmpresa")
    String findMaxCorrelativo(@Param("idEmpresa") Integer idEmpresa);
    
    @Query("SELECT COUNT(g) > 0 FROM GuiaRemision g WHERE g.ordenCompra.idOrdenes = :idOrden")
    boolean existsByOrdenCompra(@Param("idOrden") Integer idOrden);
    
    @Query("SELECT COUNT(g) > 0 FROM GuiaRemision g WHERE g.salidaInventario.idSalida = :idSalida")
    boolean existsBySalidaInventario(@Param("idSalida") Integer idSalida);
    
    Page<GuiaRemision> findByEstadoGuiaAndEmpresaIdEmpresa(GuiaRemision.EstadoGuia estadoGuia, Integer idEmpresa, Pageable pageable);
    
    @Query("SELECT g FROM GuiaRemision g WHERE g.empresa.idEmpresa = :idEmpresa " +
           "AND (g.numeroGuia LIKE %:search% OR g.transportista LIKE %:search% " +
           "OR g.placaVehiculo LIKE %:search%) ORDER BY g.fechaEmision DESC")
    Page<GuiaRemision> searchGuias(@Param("idEmpresa") Integer idEmpresa, 
                                    @Param("search") String search, 
                                    Pageable pageable);


     @Query("SELECT g FROM GuiaRemision g " +
           // --- Carga Salida de Inventario (Origen del problema) y Cliente ---
           "LEFT JOIN FETCH g.salidaInventario s " +
           "LEFT JOIN FETCH s.cliente c " +     // <--- ¡CLAVE! Carga el Cliente en la misma consulta
           
           // --- Carga Orden de Compra (si existe) y Proveedor ---
           "LEFT JOIN FETCH g.ordenCompra oc " + 
           "LEFT JOIN FETCH oc.proveedor prov " + 
           
           // --- Carga Detalles y sus Productos/Suministros ---
           "LEFT JOIN FETCH g.detalles d " +    
           "LEFT JOIN FETCH d.producto p " +    
           "LEFT JOIN FETCH d.suministro sum " + // Asegura que los items del detalle se carguen
           
           // --- Carga Almacenes y Empresa (si son LAZY) ---
           "LEFT JOIN FETCH g.almacenOrigen ao " +
           "LEFT JOIN FETCH g.almacenDestino ad " +
           "LEFT JOIN FETCH g.empresa e " + 

           "WHERE g.idGuia = :idGuia")
    GuiaRemision findByIdForPdf(Integer idGuia);            
}