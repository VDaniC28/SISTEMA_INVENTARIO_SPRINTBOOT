package com.example.appsistema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Suministro;
import com.example.appsistema.repository.InventarioAlmacenRepository;

import jakarta.persistence.EntityManager;



@Service
@Transactional
public class InventarioAlmacenService {
     @Autowired
    private InventarioAlmacenRepository inventarioRepository;

    @Autowired
    private EntityManager entityManager;
    
    // Obtener todos los inventarios
    public List<InventarioAlmacen> obtenerTodos() {
        return inventarioRepository.findAll();
    }
    
    // Obtener inventarios con paginación
    public Page<InventarioAlmacen> obtenerTodosConPaginacion(Pageable pageable) {
        return inventarioRepository.findAll(pageable);
    }
    
    // Obtener inventario por ID
    public Optional<InventarioAlmacen> obtenerPorId(Integer id) {
        return inventarioRepository.findById(id);
    }
    
    // Guardar inventario
    public InventarioAlmacen guardar(InventarioAlmacen inventario) {
        validarInventario(inventario);
        return inventarioRepository.save(inventario);
    }
    
    // Actualizar inventario
    public InventarioAlmacen actualizar(InventarioAlmacen inventario) {
        if (inventario.getIdInventario() == null) {
            throw new IllegalArgumentException("El ID del inventario es requerido para actualizar");
        }
        validarInventario(inventario);
        return inventarioRepository.save(inventario);
    }
    
    // Eliminar inventario
    public void eliminar(Integer id) {
        if (!inventarioRepository.existsById(id)) {
            throw new IllegalArgumentException("El inventario con ID " + id + " no existe");
        }
        inventarioRepository.deleteById(id);
    }
    

    // Este es el método que tu endpoint API debe usar
    public Optional<InventarioAlmacen> obtenerPorIdWithDetails(Integer id) {
        return inventarioRepository.findByIdWithDetails(id);
    }

    
    // Obtener inventario por almacén
    public Page<InventarioAlmacen> obtenerPorAlmacen(Almacen almacen, Pageable pageable) {
        return inventarioRepository.findByAlmacen(almacen, pageable);
    }
    
    // Obtener inventario para Excel con filtros
    public List<InventarioAlmacen> obtenerParaExcel(Integer filtroAlmacen, 
                                                    Integer filtroProducto, 
                                                    Integer filtroSuministro, 
                                                    String filtroLote) {
        // CRÍTICO: Llamar al método JOIN FETCH
        return inventarioRepository.findForExcelReportWithDetails(
            filtroAlmacen, 
            filtroProducto, 
            filtroSuministro, 
            filtroLote
        );
    }
    
    // Obtener stock bajo por almacén
    public List<InventarioAlmacen> obtenerStockBajo(Almacen almacen) {
        return inventarioRepository.findStockBajoByAlmacen(almacen);
    }
    
    // Obtener inventario con stock disponible
    public List<InventarioAlmacen> obtenerConStockDisponible() {
        return inventarioRepository.findWithStockDisponible();
    }
    
    // Contar items por almacén
    public long contarPorAlmacen(Almacen almacen) {
        return inventarioRepository.countByAlmacen(almacen);
    }
    
    // Sumar stock total por almacén
    public Long sumarStockPorAlmacen(Almacen almacen) {
        return inventarioRepository.sumStockByAlmacen(almacen);
    }
    
    // Actualizar stock
    public InventarioAlmacen actualizarStock(Integer inventarioId, Integer nuevoStock) {
        Optional<InventarioAlmacen> inventarioOpt = inventarioRepository.findById(inventarioId);
        if (inventarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Inventario no encontrado");
        }
        
        InventarioAlmacen inventario = inventarioOpt.get();
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        if (nuevoStock < inventario.getStockReservado()) {
            throw new IllegalArgumentException("El stock no puede ser menor al stock reservado (" + 
                                             inventario.getStockReservado() + ")");
        }
        
        inventario.setStock(nuevoStock);
        return inventarioRepository.save(inventario);
    }
    
    // Reservar stock
    public InventarioAlmacen reservarStock(Integer inventarioId, Integer cantidadReservar) {
        Optional<InventarioAlmacen> inventarioOpt = inventarioRepository.findById(inventarioId);
        if (inventarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Inventario no encontrado");
        }
        
        InventarioAlmacen inventario = inventarioOpt.get();
        int nuevoStockReservado = inventario.getStockReservado() + cantidadReservar;
        
        if (nuevoStockReservado > inventario.getStock()) {
            throw new IllegalArgumentException("No hay suficiente stock disponible para reservar");
        }
        
        inventario.setStockReservado(nuevoStockReservado);
        return inventarioRepository.save(inventario);
    }
    
    // Liberar stock reservado
    public InventarioAlmacen liberarStockReservado(Integer inventarioId, Integer cantidadLiberar) {
        Optional<InventarioAlmacen> inventarioOpt = inventarioRepository.findById(inventarioId);
        if (inventarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Inventario no encontrado");
        }
        
        InventarioAlmacen inventario = inventarioOpt.get();
        int nuevoStockReservado = inventario.getStockReservado() - cantidadLiberar;
        
        if (nuevoStockReservado < 0) {
            throw new IllegalArgumentException("No se puede liberar más stock del que está reservado");
        }
        
        inventario.setStockReservado(nuevoStockReservado);
        return inventarioRepository.save(inventario);
    }
    
    // Obtener inventario por producto
    public List<InventarioAlmacen> obtenerPorProducto(Producto producto) {
        return inventarioRepository.findByProducto(producto);
    }
    
    // Obtener inventario por suministro
    public List<InventarioAlmacen> obtenerPorSuministro(Suministro suministro) {
        return inventarioRepository.findBySuministro(suministro);
    }
    
    // Buscar por lote
    public List<InventarioAlmacen> buscarPorLote(String lote) {
        return inventarioRepository.findByLoteContainingIgnoreCase(lote);
    }
    
    // Verificar si existe inventario específico
    public boolean existeInventario(Almacen almacen, Producto producto, String lote) {
        return inventarioRepository.findByAlmacenAndProductoAndLote(almacen, producto, lote).isPresent();
    }
    
    public boolean existeInventario(Almacen almacen, Suministro suministro, String lote) {
        return inventarioRepository.findByAlmacenAndSuministroAndLote(almacen, suministro, lote).isPresent();
    }
    
    // Validaciones
    private void validarInventario(InventarioAlmacen inventario) {
        if (inventario.getAlmacen() == null) {
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        
        if (inventario.getProducto() == null && inventario.getSuministro() == null) {
            throw new IllegalArgumentException("Debe especificar un producto o suministro");
        }
        
        if (inventario.getProducto() != null && inventario.getSuministro() != null) {
            throw new IllegalArgumentException("No se puede especificar producto y suministro al mismo tiempo");
        }
        
        if (inventario.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        if (inventario.getStockReservado() < 0) {
            throw new IllegalArgumentException("El stock reservado no puede ser negativo");
        }
        
        if (inventario.getStockReservado() > inventario.getStock()) {
            throw new IllegalArgumentException("El stock reservado no puede ser mayor al stock total");
        }
        
        // Verificar duplicados solo para nuevos registros
        if (inventario.getIdInventario() == null) {
            verificarDuplicados(inventario);
        }
    }
    
    private void verificarDuplicados(InventarioAlmacen inventario) {
        if (inventario.getProducto() != null) {
            Optional<InventarioAlmacen> existente = inventarioRepository
                .findByAlmacenAndProductoAndLote(inventario.getAlmacen(), 
                                               inventario.getProducto(), 
                                               inventario.getLote());
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Ya existe inventario para este producto en el almacén con el mismo lote");
            }
        } else if (inventario.getSuministro() != null) {
            Optional<InventarioAlmacen> existente = inventarioRepository
                .findByAlmacenAndSuministroAndLote(inventario.getAlmacen(), 
                                                 inventario.getSuministro(), 
                                                 inventario.getLote());
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Ya existe inventario para este suministro en el almacén con el mismo lote");
            }
        }
    }

   
    @Autowired
    private AlmacenService almacenService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private SuministroService suministroService;
    
    public void actualizarStockRecepcion(Integer idAlmacen, Integer idProducto, 
                                         Integer idSuministro, Integer cantidad, String lote) {
        
        // Se valida que la cantidad sea positiva antes de proceder
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a recibir debe ser mayor a cero.");
        }
                                             
        // Buscar si ya existe un registro en inventario
        InventarioAlmacen inventario;
        
        if (idProducto != null) {
            // Buscar por producto
            inventario = inventarioRepository
                .findByAlmacenIdAlmacenAndProductoIdProductoAndLote(idAlmacen, idProducto, lote)
                .orElse(null);
            
            if (inventario == null) {
                // Crear nuevo registro
                inventario = new InventarioAlmacen();
                inventario.setAlmacen(almacenService.obtenerPorId(idAlmacen)
                    .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + idAlmacen)));
                inventario.setProducto(productoService.obtenerProductoPorId(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto)));
                inventario.setLote(lote);
                inventario.setStock(cantidad);
                inventario.setStockReservado(0);
            } else {
                // Aumentar stock existente
                inventario.setStock(inventario.getStock() + cantidad);
            }
        } else if (idSuministro != null) {
            // Buscar por suministro
            inventario = inventarioRepository
                .findByAlmacenIdAlmacenAndSuministroIdSuministroAndLote(idAlmacen, idSuministro, lote)
                .orElse(null);
            
            if (inventario == null) {
                // Crear nuevo registro
                inventario = new InventarioAlmacen();
                inventario.setAlmacen(almacenService.obtenerPorId(idAlmacen)
                    .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + idAlmacen)));
                inventario.setSuministro(suministroService.findById(idSuministro)
                    .orElseThrow(() -> new RuntimeException("Suministro no encontrado con ID: " + idSuministro)));
                inventario.setLote(lote);
                inventario.setStock(cantidad);
                inventario.setStockReservado(0);
            } else {
                // Aumentar stock existente
                inventario.setStock(inventario.getStock() + cantidad);
            }
        } else {
            throw new IllegalArgumentException("Debe especificar un producto o suministro");
        }
        
        // ✅ CORRECCIÓN CLAVE: Usamos saveAndFlush() para forzar la escritura inmediata a la BD.
        // Esto garantiza que el dato esté disponible para la siguiente consulta de lectura 
        // que carga la vista de inventario.
        inventarioRepository.saveAndFlush(inventario);
    }

    /**
     * Busca inventario aplicando filtros y paginación.
     * Esta es la consulta que alimenta la tabla principal de inventario.
     * (El repositorio ya tiene la anotación @QueryHints(REFRESH) para asegurar visibilidad).
     */
    @Transactional(readOnly = true)
    public Page<InventarioAlmacen> buscarConFiltros(Integer almacenId, Integer productoId,
                                                Integer suministroId, String lote, Pageable pageable) {
        
        // Limpiar caché de primer nivel para forzar lectura fresca de BD
        entityManager.clear();
        
        // PASO 1: Obtener IDs paginados
        Page<Integer> idsPage = inventarioRepository.findIdsWithFilters(
            almacenId, productoId, suministroId, lote, pageable);
        
        // Si no hay resultados, retornar página vacía
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // PASO 2: Obtener entidades completas con JOIN FETCH
        List<InventarioAlmacen> inventarios = inventarioRepository.findByIdsWithDetails(
            idsPage.getContent());
        
        // PASO 3: Crear página con los resultados completos
        return new PageImpl<>(inventarios, pageable, idsPage.getTotalElements());
    }
}
