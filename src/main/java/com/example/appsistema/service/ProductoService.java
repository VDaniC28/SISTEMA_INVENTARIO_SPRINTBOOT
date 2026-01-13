package com.example.appsistema.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Proveedor;
import com.example.appsistema.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAllByOrderByFechaRegistroDesc();
    }

    public List<Producto> obtenerTodos3() {
        return productoRepository.findAllOrderByNombreProducto();
    }

    // Obtener producto por ID
    public Optional<Producto> obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Guardar o actualizar producto
    public Producto guardarProducto(Producto producto) {
        if (producto.getFechaRegistro() == null) {
            producto.setFechaRegistro(LocalDate.now());
        }
        
        // Validar que el stock máximo sea mayor al mínimo
        if (producto.getStockMaximo() <= producto.getStockMinimo()) {
            throw new IllegalArgumentException("El stock máximo debe ser mayor al stock mínimo");
        }
        
        return productoRepository.save(producto);
    }

    // Eliminar producto por ID
    public boolean eliminarProducto(Integer id) {
        try {
            if (productoRepository.existsById(id)) {
                productoRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el producto: " + e.getMessage());
        }
    }

    // Buscar productos por nombre
    public List<Producto> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreProductoContainingIgnoreCase(nombre);
    }

    // Buscar producto por serial
    public Optional<Producto> buscarProductoPorSerial(String serial) {
        return productoRepository.findBySerialProducto(serial);
    }

    // Verificar si existe un producto con el serial dado (para validación)
    public boolean existeProductoConSerial(String serial) {
        return productoRepository.existsBySerialProducto(serial);
    }

    // Verificar si existe un producto con el serial dado, excluyendo un ID específico
    public boolean existeProductoConSerial(String serial, Integer idExcluir) {
        Optional<Producto> producto = productoRepository.findBySerialProducto(serial);
        return producto.isPresent() && !producto.get().getIdProducto().equals(idExcluir);
    }

    // Filtrar productos con múltiples criterios
    public List<Producto> filtrarProductos(LocalDate fechaRegistro,
                                         Integer idTipoItem,
                                         Integer idMarca,
                                         Integer idCategoria,
                                         Integer idProveedor,
                                         Integer idColor,
                                         Integer idGenero,
                                         Integer idMaterialSuela,
                                         Integer idTalla,
                                         Integer idMaterialPrincipal,
                                         Integer idTipoPersona,
                                         Integer idEstadoProducto) {
        
        return productoRepository.findProductosConFiltros(
                fechaRegistro, idTipoItem, idMarca, idCategoria, idProveedor,
                idColor, idGenero, idMaterialSuela, idTalla, idMaterialPrincipal,
                idTipoPersona, idEstadoProducto);
    }

    // Obtener productos por fecha de registro
    public List<Producto> obtenerProductosPorFecha(LocalDate fecha) {
        return productoRepository.findByFechaRegistro(fecha);
    }

    // Obtener productos por rango de fechas
    public List<Producto> obtenerProductosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return productoRepository.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    // Filtros específicos por entidades relacionadas
    public List<Producto> obtenerProductosPorTipoItem(Integer idTipoItem) {
        return productoRepository.findByTipoItem_IdTipoItem(idTipoItem);
    }

    public List<Producto> obtenerProductosPorMarca(Integer idMarca) {
        return productoRepository.findByMarca_IdMarca(idMarca);
    }

    public List<Producto> obtenerProductosPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoria_IdCategoria(idCategoria);
    }

    public List<Producto> obtenerProductosPorProveedor2(Integer idProveedor) {
        return productoRepository.findByProveedor_IdProveedor(idProveedor);
    }

    public List<Producto> obtenerProductosPorColor(Integer idColor) {
        return productoRepository.findByColor_IdColor(idColor);
    }

    public List<Producto> obtenerProductosPorGenero(Integer idGenero) {
        return productoRepository.findByGenero_IdGenero(idGenero);
    }

    public List<Producto> obtenerProductosPorMaterialSuela(Integer idMaterialSuela) {
        return productoRepository.findByMaterialSuela_IdMaterialSuela(idMaterialSuela);
    }

    public List<Producto> obtenerProductosPorTalla(Integer idTalla) {
        return productoRepository.findByTalla_IdTalla(idTalla);
    }

    public List<Producto> obtenerProductosPorMaterialPrincipal(Integer idMaterialPrincipal) {
        return productoRepository.findByMaterialPrincipal_IdMaterialPrincipal(idMaterialPrincipal);
    }

    public List<Producto> obtenerProductosPorTipoPersona(Integer idTipoPersona) {
        return productoRepository.findByTipoPersona_IdTipoPersona(idTipoPersona);
    }

    public List<Producto> obtenerProductosPorEstadoProducto(Integer idEstadoProducto) {
        return productoRepository.findByEstadoProducto_IdEstadoProducto(idEstadoProducto);
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    // Validar datos del producto antes de guardar
    public void validarProducto(Producto producto) {
        if (producto.getSerialProducto() == null || producto.getSerialProducto().trim().isEmpty()) {
            throw new IllegalArgumentException("El serial del producto es obligatorio");
        }
        
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a 0");
        }
        
        if (producto.getStockMinimo() != null && producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        
        if (producto.getStockMaximo() != null && producto.getStockMaximo() <= 0) {
            throw new IllegalArgumentException("El stock máximo debe ser mayor a 0");
        }
        
        if (producto.getStockMinimo() != null && producto.getStockMaximo() != null && 
            producto.getStockMaximo() <= producto.getStockMinimo()) {
            throw new IllegalArgumentException("El stock máximo debe ser mayor al stock mínimo");
        }
    }

    // Actualizar producto (validando que existe)
    public Producto actualizarProducto(Integer id, Producto productoActualizado) {
        Optional<Producto> productoExistente = productoRepository.findById(id);
        
        if (!productoExistente.isPresent()) {
            throw new RuntimeException("El producto con ID " + id + " no existe");
        }

        // Validar que no exista otro producto con el mismo serial
        if (existeProductoConSerial(productoActualizado.getSerialProducto(), id)) {
            throw new IllegalArgumentException("Ya existe otro producto con el serial: " + productoActualizado.getSerialProducto());
        }

        productoActualizado.setIdProducto(id);
        validarProducto(productoActualizado);
        
        // Mantener la fecha de registro original si no se especifica una nueva
        if (productoActualizado.getFechaRegistro() == null) {
            productoActualizado.setFechaRegistro(productoExistente.get().getFechaRegistro());
        }
        
        return productoRepository.save(productoActualizado);
    }

    // NECESITAS ESTE MÉTODO EN EL REPOSITORIO: List<Producto> findByProveedor(Proveedor proveedor);
    public List<Producto> obtenerProductosPorProveedor(Proveedor proveedor) {
        // Llama al método del repositorio.
        return productoRepository.findByProveedor(proveedor);
    }

    public Producto guardar(Producto producto) {
        return guardarProducto(producto);
    }
  
    public Optional<Producto> buscarPorId(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }
}
