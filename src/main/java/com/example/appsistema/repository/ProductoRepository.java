package com.example.appsistema.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Proveedor;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Buscar productos por nombre (búsqueda parcial)
    List<Producto> findByNombreProductoContainingIgnoreCase(String nombre);

    // Buscar por serial
    Optional<Producto> findBySerialProducto(String serialProducto);

    // Verificar si existe un producto con el mismo serial (para validación de duplicados)
    boolean existsBySerialProducto(String serialProducto);

    // Buscar productos por fecha de registro
    List<Producto> findByFechaRegistro(LocalDate fechaRegistro);

    // Buscar productos por rango de fechas
    List<Producto> findByFechaRegistroBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Filtros específicos por entidades relacionadas
    List<Producto> findByTipoItem_IdTipoItem(Integer idTipoItem);
    
    List<Producto> findByMarca_IdMarca(Integer idMarca);
    
    List<Producto> findByCategoria_IdCategoria(Integer idCategoria);
    
    List<Producto> findByProveedor_IdProveedor(Integer idProveedor);
    
    List<Producto> findByColor_IdColor(Integer idColor);
    
    List<Producto> findByGenero_IdGenero(Integer idGenero);
    
    List<Producto> findByMaterialSuela_IdMaterialSuela(Integer idMaterialSuela);
    
    List<Producto> findByTalla_IdTalla(Integer idTalla);
    
    List<Producto> findByMaterialPrincipal_IdMaterialPrincipal(Integer idMaterialPrincipal);
    
    List<Producto> findByTipoPersona_IdTipoPersona(Integer idTipoPersona);
    
    List<Producto> findByEstadoProducto_IdEstadoProducto(Integer idEstadoProducto);

    // Consulta con filtros múltiples usando JPQL
    @Query("SELECT p FROM Producto p WHERE " +
            "(:fechaRegistro IS NULL OR p.fechaRegistro = :fechaRegistro) AND " +
            "(:idTipoItem IS NULL OR p.tipoItem.idTipoItem = :idTipoItem) AND " +
            "(:idMarca IS NULL OR p.marca.idMarca = :idMarca) AND " +
            "(:idCategoria IS NULL OR p.categoria.idCategoria = :idCategoria) AND " +
            "(:idProveedor IS NULL OR p.proveedor.idProveedor = :idProveedor) AND " +
            "(:idColor IS NULL OR p.color.idColor = :idColor) AND " +
            "(:idGenero IS NULL OR p.genero.idGenero = :idGenero) AND " +
            "(:idMaterialSuela IS NULL OR p.materialSuela.idMaterialSuela = :idMaterialSuela) AND " +
            "(:idTalla IS NULL OR p.talla.idTalla = :idTalla) AND " +
            "(:idMaterialPrincipal IS NULL OR p.materialPrincipal.idMaterialPrincipal = :idMaterialPrincipal) AND " +
            "(:idTipoPersona IS NULL OR p.tipoPersona.idTipoPersona = :idTipoPersona) AND " +
            "(:idEstadoProducto IS NULL OR p.estadoProducto.idEstadoProducto = :idEstadoProducto)")
    List<Producto> findProductosConFiltros(
            @Param("fechaRegistro") LocalDate fechaRegistro,
            @Param("idTipoItem") Integer idTipoItem,
            @Param("idMarca") Integer idMarca,
            @Param("idCategoria") Integer idCategoria,
            @Param("idProveedor") Integer idProveedor,
            @Param("idColor") Integer idColor,
            @Param("idGenero") Integer idGenero,
            @Param("idMaterialSuela") Integer idMaterialSuela,
            @Param("idTalla") Integer idTalla,
            @Param("idMaterialPrincipal") Integer idMaterialPrincipal,
            @Param("idTipoPersona") Integer idTipoPersona,
            @Param("idEstadoProducto") Integer idEstadoProducto
    );

    // Obtener productos ordenados por fecha de registro más reciente
    List<Producto> findAllByOrderByFechaRegistroDesc();

    // Consulta para obtener productos con stock bajo (se une a la tabla de inventario)
    // Se realiza una unión explícita en JPQL entre Producto e InventarioAlmacen
    // usando los IDs para conectar ambas entidades.
    @Query("SELECT p FROM Producto p JOIN InventarioAlmacen ia ON p.idProducto = ia.producto.idProducto WHERE ia.stock < p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    // Consulta para obtener productos con información de stock.
    // Similar a la anterior, se corrige la sintaxis de unión para que sea JPQL válida,
    // asumiendo que el campo `producto` en `InventarioAlmacen` es una referencia
    // a la entidad Producto.
    @Query("SELECT p FROM Producto p JOIN InventarioAlmacen ia ON p.idProducto = ia.producto.idProducto WHERE ia.stock > 0")
    List<Producto> findProductosConStock();

    // Obtener todos los almacenes ordenados por nombre
    @Query("SELECT p FROM Producto p ORDER BY p.nombreProducto ASC")
    List<Producto> findAllOrderByNombreProducto();

    List<Producto> findByProveedor(Proveedor proveedor);
}
