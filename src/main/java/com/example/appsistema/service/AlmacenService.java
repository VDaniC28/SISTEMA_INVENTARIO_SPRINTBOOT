package com.example.appsistema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.repository.AlmacenRepository;
import com.example.appsistema.repository.InventarioAlmacenRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AlmacenService {
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private InventarioAlmacenRepository inventarioRepository;
    
    // Obtener todos los almacenes
    public List<Almacen> obtenerTodos() {
        return almacenRepository.findAllOrderByNombreAlmacen();
    }
    
    // Obtener almacenes con paginación
    public Page<Almacen> obtenerTodosConPaginacion(Pageable pageable) {
        return almacenRepository.findAll(pageable);
    }
    
    // Obtener almacén por ID
    public Optional<Almacen> obtenerPorId(Integer id) {
        return almacenRepository.findById(id);
    }
    
    // Guardar almacén
    public Almacen guardar(Almacen almacen) {
        validarAlmacen(almacen);
        return almacenRepository.save(almacen);
    }
    
    // Actualizar almacén
    public Almacen actualizar(Almacen almacen) {
        if (almacen.getIdAlmacen() == null) {
            throw new IllegalArgumentException("El ID del almacén es requerido para actualizar");
        }
        validarAlmacenParaActualizacion(almacen);
        return almacenRepository.save(almacen);
    }
    
    // Eliminar almacén
    public void eliminar(Integer id) {
        if (!almacenRepository.existsById(id)) {
            throw new IllegalArgumentException("El almacén con ID " + id + " no existe");
        }
        
        // Verificar si tiene inventario asociado antes de eliminar
        long inventarioCount = inventarioRepository.countByAlmacen(almacenRepository.findById(id).get());
        if (inventarioCount > 0) {
            throw new IllegalArgumentException("No se puede eliminar el almacén porque tiene " + inventarioCount + " items en inventario");
        }
        
        almacenRepository.deleteById(id);
    }
    
    // Buscar almacenes con filtro
    public Page<Almacen> buscarConFiltro(String nombre, Pageable pageable) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return almacenRepository.findAll(pageable);
        }
        return almacenRepository.findByNombreAlmacenContainingIgnoreCase(nombre.trim(), pageable);
    }
    
    // Verificar si existe almacén por nombre
    public boolean existePorNombre(String nombre) {
        return almacenRepository.existsByNombreAlmacen(nombre);
    }
    
    // Obtener almacenes por tipo
    public List<Almacen> obtenerPorTipo(Almacen.TipoAlmacen tipo) {
        return almacenRepository.findByTipoAlmacen(tipo);
    }
    
    // Obtener estadísticas del almacén
    public AlmacenEstadisticas obtenerEstadisticas(Integer idAlmacen) {
        Optional<Almacen> almacenOpt = almacenRepository.findById(idAlmacen);
        if (almacenOpt.isEmpty()) {
            throw new IllegalArgumentException("Almacén no encontrado");
        }
        
        Almacen almacen = almacenOpt.get();
        long totalItems = inventarioRepository.countByAlmacen(almacen);
        Long totalStock = inventarioRepository.sumStockByAlmacen(almacen);
        
        return new AlmacenEstadisticas(
            almacen.getNombreAlmacen(),
            totalItems,
            totalStock != null ? totalStock : 0L,
            almacen.getCapacidadMaxima()
        );
    }
    
    // Validaciones
    private void validarAlmacen(Almacen almacen) {
        if (almacen.getNombreAlmacen() == null || almacen.getNombreAlmacen().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del almacén es obligatorio");
        }
        
        if (existePorNombre(almacen.getNombreAlmacen())) {
            throw new IllegalArgumentException("Ya existe un almacén con el nombre: " + almacen.getNombreAlmacen());
        }
        
        if (almacen.getUbicacion() == null || almacen.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        
        if (almacen.getCapacidadMaxima() != null && almacen.getCapacidadMaxima() < 0) {
            throw new IllegalArgumentException("La capacidad máxima no puede ser negativa");
        }
    }
    
    private void validarAlmacenParaActualizacion(Almacen almacen) {
        if (almacen.getNombreAlmacen() == null || almacen.getNombreAlmacen().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del almacén es obligatorio");
        }
        
        // Verificar si el nombre ya existe en otro almacén
        Optional<Almacen> existente = almacenRepository.findByNombreAlmacen(almacen.getNombreAlmacen());
        if (existente.isPresent() && !existente.get().getIdAlmacen().equals(almacen.getIdAlmacen())) {
            throw new IllegalArgumentException("Ya existe otro almacén con el nombre: " + almacen.getNombreAlmacen());
        }
        
        if (almacen.getUbicacion() == null || almacen.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        
        if (almacen.getCapacidadMaxima() != null && almacen.getCapacidadMaxima() < 0) {
            throw new IllegalArgumentException("La capacidad máxima no puede ser negativa");
        }
    }
    
    // Clase interna para estadísticas
    public static class AlmacenEstadisticas {
        private String nombreAlmacen;
        private long totalItems;
        private long totalStock;
        private Integer capacidadMaxima;
        
        public AlmacenEstadisticas(String nombreAlmacen, long totalItems, long totalStock, Integer capacidadMaxima) {
            this.nombreAlmacen = nombreAlmacen;
            this.totalItems = totalItems;
            this.totalStock = totalStock;
            this.capacidadMaxima = capacidadMaxima;
        }
        
        // Getters
        public String getNombreAlmacen() { return nombreAlmacen; }
        public long getTotalItems() { return totalItems; }
        public long getTotalStock() { return totalStock; }
        public Integer getCapacidadMaxima() { return capacidadMaxima; }
        
        public double getPorcentajeOcupacion() {
            if (capacidadMaxima == null || capacidadMaxima == 0) return 0.0;
            return (totalStock * 100.0) / capacidadMaxima;
        }
    }
}
