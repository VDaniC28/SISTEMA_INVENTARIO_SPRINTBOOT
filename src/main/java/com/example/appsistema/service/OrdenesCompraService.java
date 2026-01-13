package com.example.appsistema.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.OrdenesCompra;

import com.example.appsistema.model.Proveedor;
import com.example.appsistema.repository.OrdenesCompraRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OrdenesCompraService {
    @Autowired
    private OrdenesCompraRepository ordenesCompraRepository;
    
    @Autowired
    private OrdenesCompraDetalleService ordenesCompraDetalleService;
    
    // =====================================================
    // OPERACIONES BÁSICAS CRUD
    // =====================================================
    
    public List<OrdenesCompra> listarTodas() {
        return ordenesCompraRepository.findAllByOrderByFechaOrdenDesc();
    }
    
    public Page<OrdenesCompra> listarTodasPaginadas(Pageable pageable) {
        return ordenesCompraRepository.findAll(pageable);
    }
    
    public Optional<OrdenesCompra> obtenerPorId(Integer id) {
        return ordenesCompraRepository.findById(id);
    }
    
    public OrdenesCompra guardar(OrdenesCompra ordenCompra) {
        // Validaciones antes de guardar
        validarOrdenCompra(ordenCompra);
        
        // Si es una nueva orden, establecer fecha de registro
        if (ordenCompra.getIdOrdenes() == null) {
            if (ordenCompra.getFechaOrden() == null) {
                ordenCompra.setFechaOrden(LocalDate.now());
            }
            // Establecer estado inicial si no está definido
            if (ordenCompra.getEstadoOrden() == null) {
                ordenCompra.setEstadoOrden(OrdenesCompra.EstadoOrden.Pendiente);
            }
        }
        
        return ordenesCompraRepository.save(ordenCompra);
    }
    
    public OrdenesCompra actualizar(OrdenesCompra ordenCompra) {
        if (ordenCompra.getIdOrdenes() == null) {
            throw new IllegalArgumentException("No se puede actualizar una orden sin ID");
        }
        
        // Verificar que la orden existe
        Optional<OrdenesCompra> ordenExistente = ordenesCompraRepository.findById(ordenCompra.getIdOrdenes());
        if (ordenExistente.isEmpty()) {
            throw new RuntimeException("La orden con ID " + ordenCompra.getIdOrdenes() + " no existe");
        }
        
        // Validar que la orden puede ser actualizada
        if (!ordenExistente.get().puedeSerEditada()) {
            throw new IllegalStateException("No se puede editar una orden en estado: " + ordenExistente.get().getEstadoOrden());
        }
        
        validarOrdenCompra(ordenCompra);
        return ordenesCompraRepository.save(ordenCompra);
    }
    
    public void eliminar(Integer id) {
        Optional<OrdenesCompra> orden = ordenesCompraRepository.findById(id);
        if (orden.isEmpty()) {
            throw new RuntimeException("La orden con ID " + id + " no existe");
        }
        
        // Solo se pueden eliminar órdenes pendientes
        if (!orden.get().puedeSerEditada()) {
            throw new IllegalStateException("Solo se pueden eliminar órdenes en estado Pendiente");
        }
        
        ordenesCompraRepository.deleteById(id);
    }
    
    // =====================================================
    // OPERACIONES DE CAMBIO DE ESTADO
    // =====================================================
    
    public OrdenesCompra confirmarOrden(Integer id) {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraRepository.findById(id);
        if (ordenOpt.isEmpty()) {
            throw new RuntimeException("La orden con ID " + id + " no existe");
        }
        
        OrdenesCompra orden = ordenOpt.get();
        if (!orden.puedeSerConfirmada()) {
            throw new IllegalStateException("La orden no puede ser confirmada en su estado actual: " + orden.getEstadoOrden());
        }
        
        orden.setEstadoOrden(OrdenesCompra.EstadoOrden.Confirmada);
        return ordenesCompraRepository.save(orden);
    }
    
    public OrdenesCompra cancelarOrden(Integer id, String motivo) {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraRepository.findById(id);
        if (ordenOpt.isEmpty()) {
            throw new RuntimeException("La orden con ID " + id + " no existe");
        }
        
        OrdenesCompra orden = ordenOpt.get();
        if (!orden.puedeSerCancelada()) {
            throw new IllegalStateException("La orden no puede ser cancelada en su estado actual: " + orden.getEstadoOrden());
        }
        
        orden.setEstadoOrden(OrdenesCompra.EstadoOrden.Cancelada);
        return ordenesCompraRepository.save(orden);
    }
    
    public OrdenesCompra finalizarRecepcion(Integer id) { // 👈 Renombrado para más claridad
    Optional<OrdenesCompra> ordenOpt = ordenesCompraRepository.findById(id);
        if (ordenOpt.isEmpty()) {
            throw new RuntimeException("La orden con ID " + id + " no existe");
        }
        
        OrdenesCompra orden = ordenOpt.get();
        
        // Solo puede ser completada si está Confirmada o en Recepción Parcial
        if (orden.getEstadoOrden() != OrdenesCompra.EstadoOrden.Confirmada && 
            orden.getEstadoOrden() != OrdenesCompra.EstadoOrden.RecepcionParcial) {
            throw new IllegalStateException("La orden debe estar Confirmada para finalizar su recepción.");
        }
        
        // Verificar que todos los detalles están completamente recibidos
        // ASUMO que ordenesCompraDetalleService.todosLosDetallesCompletamenteRecibidos(orden) verifica esto.
        boolean todosRecibidos = ordenesCompraDetalleService.todosLosDetallesCompletamenteRecibidos(orden);
        
        if (todosRecibidos) {
            // ✅ CORRECCIÓN CLAVE: Actualizar la fecha y estado
            if (orden.getFechaEntregaReal() == null) {
                orden.setFechaEntregaReal(LocalDate.now()); // Solo si no ha sido establecida
            }
            orden.setEstadoOrden(OrdenesCompra.EstadoOrden.Recibida);
            return ordenesCompraRepository.save(orden);
        } 
        
        // Si no está totalmente recibida, pero se han recibido ítems, 
        // y quieres cambiar el estado a RECEPCION_PARCIAL, lo haces aquí.
        if (orden.getEstadoOrden() == OrdenesCompra.EstadoOrden.Confirmada && 
            ordenesCompraDetalleService.algunDetalleRecibido(orden)) {
            // Asumo que tienes un método auxiliar para verificar si ALGO se recibió.
            orden.setEstadoOrden(OrdenesCompra.EstadoOrden.RecepcionParcial);
            return ordenesCompraRepository.save(orden);
        }
        
        // Si no se completó, ni requiere cambio a Recepción Parcial (puede que sea recepción parcial y siga pendiente)
        // Simplemente devuelve la orden sin guardar si no hay cambios de estado/fecha.
        return orden; 
    }
    
    // =====================================================
    // CONSULTAS Y FILTROS
    // =====================================================
    
    public List<OrdenesCompra> listarPorEstado(OrdenesCompra.EstadoOrden estado) {
        return ordenesCompraRepository.findByEstadoOrden(estado);
    }
    
    public Page<OrdenesCompra> listarPorEstado(OrdenesCompra.EstadoOrden estado, Pageable pageable) {
        return ordenesCompraRepository.findByEstadoOrden(estado, pageable);
    }
    
    public List<OrdenesCompra> listarPorProveedor(Proveedor proveedor) {
        return ordenesCompraRepository.findByProveedor(proveedor);
    }
    
    public List<OrdenesCompra> listarPorAlmacen(Almacen almacen) {
        return ordenesCompraRepository.findByAlmacen(almacen);
    }
    
    public List<OrdenesCompra> listarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenesCompraRepository.findByFechaOrdenBetween(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Page<OrdenesCompra> listarConFiltros(Proveedor proveedor, Almacen almacen, 
                                            OrdenesCompra.EstadoOrden estado,
                                            LocalDate fechaInicio, LocalDate fechaFin,
                                            Pageable pageable) {
        
        // Primera consulta: obtener la página con IDs
        Page<OrdenesCompra> ordenesPage = ordenesCompraRepository.findWithFilters(
            proveedor, almacen, estado, fechaInicio, fechaFin, pageable);
        
        // Segunda consulta: cargar las relaciones para los registros de esta página
        if (!ordenesPage.isEmpty()) {
            List<OrdenesCompra> ordenesConDetalles = ordenesCompraRepository.findWithDetails(
                ordenesPage.getContent());
            
            // Reemplazar el contenido de la página con las órdenes que tienen las relaciones cargadas
            return new PageImpl<>(ordenesConDetalles, pageable, ordenesPage.getTotalElements());
        }
        
        return ordenesPage;
    }
    // =====================================================
    // CONSULTAS ESPECIALES Y REPORTES
    // =====================================================
    
    public List<OrdenesCompra> listarOrdenesVencidas() {
        return ordenesCompraRepository.findOrdenesVencidas(LocalDate.now(), OrdenesCompra.EstadoOrden.Confirmada);
    }
    
    public List<OrdenesCompra> listarOrdenesDelDia() {
        return ordenesCompraRepository.findOrdenesDelDia();
    }
    
    public List<OrdenesCompra> listarOrdenesDelMes() {
        return ordenesCompraRepository.findOrdenesDelMes();
    }
    
    public List<OrdenesCompra> listarOrdenesParaRecepcion() {
        return ordenesCompraRepository.findOrdenesParaRecepcion();
    }
    
    public List<OrdenesCompra> listarOrdenesConEntregaProxima(int diasAdelante) {
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAdelante);
        return ordenesCompraRepository.findOrdenesConEntregaProxima(fechaLimite);
    }
    
    // =====================================================
    // ESTADÍSTICAS Y MÉTRICAS
    // =====================================================
    
    public long contarPorEstado(OrdenesCompra.EstadoOrden estado) {
        return ordenesCompraRepository.countByEstadoOrden(estado);
    }
    
    public List<Object[]> obtenerEstadisticasPorEstado() {
        return ordenesCompraRepository.getEstadisticasPorEstado();
    }
    
    public Double obtenerMontoTotalPorEstado(OrdenesCompra.EstadoOrden estado) {
        Double monto = ordenesCompraRepository.getMontoTotalByEstado(estado);
        return monto != null ? monto : 0.0;
    }
    
    // =====================================================
    // VALIDACIONES Y UTILIDADES
    // =====================================================
    
    private void validarOrdenCompra(OrdenesCompra ordenCompra) {
        if (ordenCompra == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }
        
        if (ordenCompra.getProveedor() == null) {
            throw new IllegalArgumentException("La orden debe tener un proveedor asignado");
        }
        
        if (ordenCompra.getAlmacen() == null) {
            throw new IllegalArgumentException("La orden debe tener un almacén asignado");
        }
        
        if (ordenCompra.getFechaOrden() == null) {
            throw new IllegalArgumentException("La orden debe tener una fecha de orden");
        }
        
        if (ordenCompra.getFechaEntregaEsperada() == null) {
            throw new IllegalArgumentException("La orden debe tener una fecha de entrega esperada");
        }
        
        if (ordenCompra.getFechaEntregaEsperada().isBefore(ordenCompra.getFechaOrden())) {
            throw new IllegalArgumentException("La fecha de entrega esperada no puede ser anterior a la fecha de orden");
        }
        
        if (ordenCompra.getMontoSubtotal() == null || ordenCompra.getMontoSubtotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto subtotal debe ser mayor a cero");
        }
    }
    
    public boolean existeOrdenEnRangoFechas(Proveedor proveedor, LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenesCompraRepository.existsOrdenByProveedorInDateRange(proveedor, fechaInicio, fechaFin);
    }
    
    public String generarNumeroOrden(OrdenesCompra orden) {
        if (orden != null && orden.getIdOrdenes() != null) {
            return orden.getNumeroOrdenFormateado();
        }
        return "";
    }
    
    // =====================================================
    // OPERACIONES EN LOTE
    // =====================================================
    
    public List<OrdenesCompra> guardarVarias(List<OrdenesCompra> ordenes) {
        for (OrdenesCompra orden : ordenes) {
            validarOrdenCompra(orden);
        }
        return ordenesCompraRepository.saveAll(ordenes);
    }

    @Transactional(readOnly = true)
    public Optional<OrdenesCompra> obtenerPorIdConDetalles(Integer id) {
        return ordenesCompraRepository.findByIdWithDetails(id);
    }
        
    public void confirmarVarias(List<Integer> idsOrdenes) {
        for (Integer id : idsOrdenes) {
            confirmarOrden(id);
        }
    }
    
    public void cancelarVarias(List<Integer> idsOrdenes, String motivo) {
        for (Integer id : idsOrdenes) {
            cancelarOrden(id, motivo);
        }
    }
    
    // =====================================================
    // MÉTODOS DE INTEGRACIÓN
    // =====================================================
    
    public void recalcularMontosOrden(Integer idOrden) {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            OrdenesCompra orden = ordenOpt.get();
            
            // Recalcular montos basado en los detalles
            var subtotal = ordenesCompraDetalleService.calcularSubtotalOrden(orden);
            var descuentos = ordenesCompraDetalleService.calcularTotalDescuentosOrden(orden);
            var impuestos = ordenesCompraDetalleService.calcularTotalImpuestosOrden(orden);
            
            orden.setMontoSubtotal(subtotal);
            orden.setDescuentos(descuentos);
            orden.setImpuestos(impuestos);
            
            ordenesCompraRepository.save(orden);
        }
    }
    
    public boolean tieneDetalles(Integer idOrden) {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraRepository.findById(idOrden);
        if (ordenOpt.isPresent()) {
            return ordenesCompraDetalleService.contarDetallesPorOrden(ordenOpt.get()) > 0;
        }
        return false;
    }


    @Transactional
    public OrdenesCompra guardarOrden(OrdenesCompra ordenCompra) {
        
        // --- 1. Lógica de Estado y Fechas (Creación vs. Edición) ---
        
        if (ordenCompra.getIdOrdenes() != null) {
            // Es una edición:
            Optional<OrdenesCompra> ordenExistenteOpt = ordenesCompraRepository.findById(ordenCompra.getIdOrdenes());
            
            if (ordenExistenteOpt.isEmpty()) {
                 throw new RuntimeException("La orden con ID " + ordenCompra.getIdOrdenes() + " no existe.");
            }
            
            OrdenesCompra ordenExistente = ordenExistenteOpt.get();
            
            // Revalidar que el estado permite la edición (solo Pendiente)
            if (!ordenExistente.puedeSerEditada()) {
                throw new IllegalStateException("No se puede editar una orden en estado: " + ordenExistente.getEstadoOrden());
            }
            
            // Mantener campos inmutables: fecha de orden y estado.
            ordenCompra.setFechaOrden(ordenExistente.getFechaOrden()); 
            ordenCompra.setEstadoOrden(ordenExistente.getEstadoOrden());
            
        } else {
            // Es una nueva orden:
            if (ordenCompra.getFechaOrden() == null) {
                ordenCompra.setFechaOrden(LocalDate.now());
            }
            if (ordenCompra.getEstadoOrden() == null) {
                ordenCompra.setEstadoOrden(OrdenesCompra.EstadoOrden.Pendiente);
            }
        }
        
        // --- 2. Validación de DETALLES ---
        if (ordenCompra.getDetalles() == null || ordenCompra.getDetalles().isEmpty()) {
             throw new IllegalArgumentException("La orden de compra debe tener al menos un item de detalle.");
        }
        
        // --- 3. Recálculo de MONTOS (Lógica de Negocio) ---
        // Este paso es crucial para mantener la coherencia entre la cabecera y los detalles.
        recalcularMontosOrden(ordenCompra); 
        
        // --- 4. Aplicación de Validaciones Finales ---
        // Se llama al método auxiliar que valida proveedor, almacén, fechas, etc.
        validarOrdenCompra(ordenCompra); 
        
        
        return ordenesCompraRepository.save(ordenCompra);
    }
    
    // =====================================================
    // MÉTODOS AUXILIARES NECESARIOS
    // =====================================================
  
    
    private void recalcularMontosOrden(OrdenesCompra orden) {
        if (orden.getDetalles() != null) {
            var subtotal = ordenesCompraDetalleService.calcularSubtotalLista(orden.getDetalles());
            var descuentos = ordenesCompraDetalleService.calcularTotalDescuentosLista(orden.getDetalles());
            var impuestos = ordenesCompraDetalleService.calcularTotalImpuestosLista(orden.getDetalles());
            
            // Asigna los valores recalculados de vuelta a la cabecera.
            orden.setMontoSubtotal(subtotal);
            orden.setDescuentos(descuentos);
            orden.setImpuestos(impuestos);
        }
    }
}
