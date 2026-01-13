package com.example.appsistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.SalidaInventarioDetalle;

@Repository
public interface SalidaInventarioDetalleRepository extends JpaRepository<SalidaInventarioDetalle, Integer> {

    // VISTA CLIENTES
    
    
    // Obtener detalles por ID de salida, ordenados por ID de detalle
    List<SalidaInventarioDetalle> findBySalidaInventario_IdSalidaOrderByIdDetalleSalidaAsc(Integer idSalida);

    // Contar cuántos ítems hay en una salida específica
    @Query("SELECT COUNT(d) FROM SalidaInventarioDetalle d WHERE d.salidaInventario.idSalida = :idSalida")
    Long contarItemsPorSalida(@Param("idSalida") Integer idSalida);
    
    // Sumar la cantidad total de productos de una salida
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM SalidaInventarioDetalle d WHERE d.salidaInventario.idSalida = :idSalida")
    Integer obtenerCantidadTotalPorSalida(@Param("idSalida") Integer idSalida);

    // Eliminar todos los detalles de una salida específica
    void deleteBySalidaInventario_IdSalida(Integer idSalida);


    // VISTA SALIDA DE INVENTARIO

    // Obtener detalles por ID de salida
    List<SalidaInventarioDetalle> findBySalidaInventario_IdSalida(Integer idSalida);

    @Query("SELECT d FROM SalidaInventarioDetalle d WHERE d.producto.idProducto = :productoId")
    List<SalidaInventarioDetalle> findByProductoId(@Param("productoId") Integer productoId);
    
    @Query("SELECT d FROM SalidaInventarioDetalle d WHERE d.suministro.idSuministro = :suministroId")
    List<SalidaInventarioDetalle> findBySuministroId(@Param("suministroId") Integer suministroId);

}
