package com.example.appsistema.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.OrdenesCompraDetalle;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Suministro;

import com.example.appsistema.repository.OrdenesCompraDetalleRepository;
import com.example.appsistema.repository.OrdenesCompraRepository;



@Service
@Transactional
public class OrdenesCompraDetalleService {
    @Autowired
    private OrdenesCompraDetalleRepository ordenesCompraDetalleRepository;
    
    // =====================================================
    // OPERACIONES BÁSICAS CRUD
    // =====================================================
    
    public List<OrdenesCompraDetalle> listarTodos() {
        return ordenesCompraDetalleRepository.findAll();
    }
    
    public Optional<OrdenesCompraDetalle> obtenerPorId(Integer id) {
        return ordenesCompraDetalleRepository.findById(id);
    }
    
    public OrdenesCompraDetalle guardar(OrdenesCompraDetalle detalle) {
        validarDetalle(detalle);
        return ordenesCompraDetalleRepository.save(detalle);
    }
    
    public OrdenesCompraDetalle actualizar(OrdenesCompraDetalle detalle) {
        if (detalle.getIdDetalle() == null) {
            throw new IllegalArgumentException("No se puede actualizar un detalle sin ID");
        }
        
        // Verificar que el detalle existe
        Optional<OrdenesCompraDetalle> detalleExistente = ordenesCompraDetalleRepository.findById(detalle.getIdDetalle());
        if (detalleExistente.isEmpty()) {
            throw new RuntimeException("El detalle con ID " + detalle.getIdDetalle() + " no existe");
        }
        
        validarDetalle(detalle);
        return ordenesCompraDetalleRepository.save(detalle);
    }
    
    public void eliminar(Integer id) {
        if (!ordenesCompraDetalleRepository.existsById(id)) {
            throw new RuntimeException("El detalle con ID " + id + " no existe");
        }
        ordenesCompraDetalleRepository.deleteById(id);
    }
    
    // =====================================================
    // OPERACIONES POR ORDEN DE COMPRA
    // =====================================================
    
    public List<OrdenesCompraDetalle> listarPorOrden(OrdenesCompra ordenCompra) {
        return ordenesCompraDetalleRepository.findByOrdenCompra(ordenCompra);
    }
    
    public List<OrdenesCompraDetalle> listarPorOrdenId(Integer idOrden) {
        return ordenesCompraDetalleRepository.findByOrdenCompraId(idOrden);
    }
    
    public long contarDetallesPorOrden(OrdenesCompra ordenCompra) {
        return ordenesCompraDetalleRepository.countByOrdenCompra(ordenCompra);
    }
    
    public void eliminarTodosLosDetallesDeOrden(OrdenesCompra ordenCompra) {
        ordenesCompraDetalleRepository.deleteByOrdenCompra(ordenCompra);
    }
    
    public List<OrdenesCompraDetalle> guardarDetallesDeOrden(List<OrdenesCompraDetalle> detalles) {
        for (OrdenesCompraDetalle detalle : detalles) {
            validarDetalle(detalle);
        }
        return ordenesCompraDetalleRepository.saveAll(detalles);
    }
    
    // =====================================================
    // OPERACIONES DE RECEPCIÓN
    // =====================================================
    
    public List<OrdenesCompraDetalle> listarDetallesParaRecepcion(OrdenesCompra ordenCompra) {
        return ordenesCompraDetalleRepository.findDetallesParaRecepcion(ordenCompra);
    }
    
    public List<OrdenesCompraDetalle> listarDetallesConRecepcionPendiente() {
        return ordenesCompraDetalleRepository.findDetallesConRecepcionPendiente();
    }
    
    public List<OrdenesCompraDetalle> listarDetallesConRecepcionCompleta() {
        return ordenesCompraDetalleRepository.findDetallesConRecepcionCompleta();
    }
    
    public List<OrdenesCompraDetalle> listarDetallesConRecepcionParcial() {
        return ordenesCompraDetalleRepository.findDetallesConRecepcionParcial();
    }
    
    public List<OrdenesCompraDetalle> listarDetallesSinRecepcion() {
        return ordenesCompraDetalleRepository.findDetallesSinRecepcion();
    }
    
    public OrdenesCompraDetalle actualizarCantidadRecibida(Integer idDetalle, Integer cantidadRecibida) {
        Optional<OrdenesCompraDetalle> detalleOpt = ordenesCompraDetalleRepository.findById(idDetalle);
        if (detalleOpt.isEmpty()) {
            throw new RuntimeException("El detalle con ID " + idDetalle + " no existe");
        }
        
        OrdenesCompraDetalle detalle = detalleOpt.get();
        
        // Validar que la cantidad recibida no sea mayor a la solicitada
        if (cantidadRecibida > detalle.getCantidadSolicitada()) {
            throw new IllegalArgumentException("La cantidad recibida no puede ser mayor a la cantidad solicitada");
        }
        
        if (cantidadRecibida < 0) {
            throw new IllegalArgumentException("La cantidad recibida no puede ser negativa");
        }
        
        detalle.setCantidadRecibida(cantidadRecibida);
        return ordenesCompraDetalleRepository.save(detalle);
    }
    
    
    public Double obtenerPorcentajePromedioRecepcion(OrdenesCompra ordenCompra) {
        Double porcentaje = ordenesCompraDetalleRepository.getPorcentajePromedioRecepcion(ordenCompra);
        return porcentaje != null ? porcentaje : 0.0;
    }
    
    // =====================================================
    // CÁLCULOS FINANCIEROS
    // =====================================================
    
    public BigDecimal calcularValorTotalOrden(OrdenesCompra ordenCompra) {
        BigDecimal valor = ordenesCompraDetalleRepository.calcularValorTotalOrden(ordenCompra);
        return valor != null ? valor : BigDecimal.ZERO;
    }
    
    public BigDecimal calcularSubtotalOrden(OrdenesCompra ordenCompra) {
        BigDecimal subtotal = ordenesCompraDetalleRepository.calcularSubtotalOrden(ordenCompra);
        return subtotal != null ? subtotal : BigDecimal.ZERO;
    }
    
    public BigDecimal calcularTotalDescuentosOrden(OrdenesCompra ordenCompra) {
        BigDecimal descuentos = ordenesCompraDetalleRepository.calcularTotalDescuentosOrden(ordenCompra);
        return descuentos != null ? descuentos : BigDecimal.ZERO;
    }
    
    public BigDecimal calcularTotalImpuestosOrden(OrdenesCompra ordenCompra) {
        BigDecimal impuestos = ordenesCompraDetalleRepository.calcularTotalImpuestosOrden(ordenCompra);
        return impuestos != null ? impuestos : BigDecimal.ZERO;
    }
    
    public BigDecimal obtenerValorPendienteRecepcion(OrdenesCompra ordenCompra) {
        BigDecimal valorPendiente = ordenesCompraDetalleRepository.getValorPendienteRecepcion(ordenCompra);
        return valorPendiente != null ? valorPendiente : BigDecimal.ZERO;
    }
    
    // =====================================================
    // CONSULTAS POR PRODUCTO Y SUMINISTRO
    // =====================================================
    
    public List<OrdenesCompraDetalle> listarPorProducto(Producto producto) {
        return ordenesCompraDetalleRepository.findByProducto(producto);
    }
    
    public List<OrdenesCompraDetalle> listarPorSuministro(Suministro suministro) {
        return ordenesCompraDetalleRepository.findBySuministro(suministro);
    }
    
    public List<OrdenesCompraDetalle> listarPorProductoId(Integer idProducto) {
        return ordenesCompraDetalleRepository.findByProductoId(idProducto);
    }
    
    public List<OrdenesCompraDetalle> listarPorSuministroId(Integer idSuministro) {
        return ordenesCompraDetalleRepository.findBySuministroId(idSuministro);
    }
    
    // =====================================================
    // ESTADÍSTICAS Y REPORTES
    // =====================================================
    
    public List<Object[]> obtenerProductosMasSolicitados() {
        return ordenesCompraDetalleRepository.getProductosMasSolicitados();
    }
    
    public List<Object[]> obtenerSuministrosMasSolicitados() {
        return ordenesCompraDetalleRepository.getSuministrosMasSolicitados();
    }
    
    public List<Object[]> obtenerEstadisticasRecepcionPorOrden() {
        return ordenesCompraDetalleRepository.getEstadisticasRecepcionPorOrden();
    }
    
    public List<OrdenesCompraDetalle> listarPorEstadoOrden(OrdenesCompra.EstadoOrden estado) {
        return ordenesCompraDetalleRepository.findByEstadoOrden(estado);
    }
    
    // =====================================================
    // OPERACIONES AVANZADAS
    // =====================================================
    
    public List<OrdenesCompraDetalle> listarPorRangoPrecios(BigDecimal precioMinimo, BigDecimal precioMaximo) {
        return ordenesCompraDetalleRepository.findByPrecioUnitarioBetween(precioMinimo, precioMaximo);
    }
    
    public List<OrdenesCompraDetalle> listarConCantidadMayorA(Integer cantidadMinima) {
        return ordenesCompraDetalleRepository.findDetallesConCantidadMayorA(cantidadMinima);
    }
    
    public List<OrdenesCompraDetalle> listarDetallesMasCaros(OrdenesCompra ordenCompra) {
        return ordenesCompraDetalleRepository.findDetallesMasCaros(ordenCompra);
    }
    
    public List<OrdenesCompraDetalle> listarPorOrdenOrdenadoPorValor(OrdenesCompra ordenCompra) {
        return ordenesCompraDetalleRepository.findByOrdenCompraOrderByValorDesc(ordenCompra);
    }
    
    // =====================================================
    // OPERACIONES EN LOTE
    // =====================================================
    
    public void actualizarCantidadesRecibidas(List<Integer> idsDetalles, List<Integer> cantidadesRecibidas) {
        if (idsDetalles.size() != cantidadesRecibidas.size()) {
            throw new IllegalArgumentException("El número de IDs debe coincidir con el número de cantidades");
        }
        
        for (int i = 0; i < idsDetalles.size(); i++) {
            actualizarCantidadRecibida(idsDetalles.get(i), cantidadesRecibidas.get(i));
        }
    }
    
    public List<OrdenesCompraDetalle> crearDetallesDesdeProductos(OrdenesCompra ordenCompra, 
                                                                 List<Producto> productos, 
                                                                 List<Integer> cantidades, 
                                                                 List<BigDecimal> precios) {
        if (productos.size() != cantidades.size() || productos.size() != precios.size()) {
            throw new IllegalArgumentException("Las listas de productos, cantidades y precios deben tener el mismo tamaño");
        }
        
        List<OrdenesCompraDetalle> detalles = new java.util.ArrayList<>();
        
        for (int i = 0; i < productos.size(); i++) {
            OrdenesCompraDetalle detalle = new OrdenesCompraDetalle();
            detalle.setOrdenCompra(ordenCompra);
            detalle.setProducto(productos.get(i));
            detalle.setCantidadSolicitada(cantidades.get(i));
            detalle.setPrecioUnitario(precios.get(i));
            detalle.setCantidadRecibida(0);
            detalle.setDescuentoUnitario(BigDecimal.ZERO);
            detalle.setImpuestoUnitario(BigDecimal.ZERO);
            
            validarDetalle(detalle);
            detalles.add(detalle);
        }
        
        return ordenesCompraDetalleRepository.saveAll(detalles);
    }
    
    public List<OrdenesCompraDetalle> crearDetallesDesdeSupministros(OrdenesCompra ordenCompra, 
                                                                    List<Suministro> suministros, 
                                                                    List<Integer> cantidades, 
                                                                    List<BigDecimal> precios) {
        if (suministros.size() != cantidades.size() || suministros.size() != precios.size()) {
            throw new IllegalArgumentException("Las listas de suministros, cantidades y precios deben tener el mismo tamaño");
        }
        
        List<OrdenesCompraDetalle> detalles = new java.util.ArrayList<>();
        
        for (int i = 0; i < suministros.size(); i++) {
            OrdenesCompraDetalle detalle = new OrdenesCompraDetalle();
            detalle.setOrdenCompra(ordenCompra);
            detalle.setSuministro(suministros.get(i));
            detalle.setCantidadSolicitada(cantidades.get(i));
            detalle.setPrecioUnitario(precios.get(i));
            detalle.setCantidadRecibida(0);
            detalle.setDescuentoUnitario(BigDecimal.ZERO);
            detalle.setImpuestoUnitario(BigDecimal.ZERO);
            
            validarDetalle(detalle);
            detalles.add(detalle);
        }
        
        return ordenesCompraDetalleRepository.saveAll(detalles);
    }
    
    // =====================================================
    // VALIDACIONES Y UTILIDADES
    // =====================================================
    
    private void validarDetalle(OrdenesCompraDetalle detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        
        if (detalle.getOrdenCompra() == null) {
            throw new IllegalArgumentException("El detalle debe estar asociado a una orden de compra");
        }
        
        // Validar que tiene producto O suministro, pero no ambos
        boolean tieneProducto = detalle.getProducto() != null;
        boolean tieneSuministro = detalle.getSuministro() != null;
        
        if (!tieneProducto && !tieneSuministro) {
            throw new IllegalArgumentException("El detalle debe tener un producto o un suministro asociado");
        }
        
        if (tieneProducto && tieneSuministro) {
            throw new IllegalArgumentException("El detalle no puede tener un producto y un suministro al mismo tiempo");
        }
        
        if (detalle.getCantidadSolicitada() == null || detalle.getCantidadSolicitada() <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
        
        if (detalle.getCantidadRecibida() == null) {
            detalle.setCantidadRecibida(0);
        }
        
        if (detalle.getCantidadRecibida() < 0) {
            throw new IllegalArgumentException("La cantidad recibida no puede ser negativa");
        }
        
        if (detalle.getCantidadRecibida() > detalle.getCantidadSolicitada()) {
            throw new IllegalArgumentException("La cantidad recibida no puede ser mayor a la cantidad solicitada");
        }
        
        if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
        }
        
        // Establecer valores por defecto para campos opcionales
        if (detalle.getDescuentoUnitario() == null) {
            detalle.setDescuentoUnitario(BigDecimal.ZERO);
        }
        
        if (detalle.getImpuestoUnitario() == null) {
            detalle.setImpuestoUnitario(BigDecimal.ZERO);
        }
        
        // Validar que los descuentos e impuestos no sean negativos
        if (detalle.getDescuentoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El descuento unitario no puede ser negativo");
        }
        
        if (detalle.getImpuestoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El impuesto unitario no puede ser negativo");
        }
        
        // Validar que el descuento no sea mayor al precio unitario
        if (detalle.getDescuentoUnitario().compareTo(detalle.getPrecioUnitario()) > 0) {
            throw new IllegalArgumentException("El descuento unitario no puede ser mayor al precio unitario");
        }
    }
    
    // =====================================================
    // MÉTODOS DE UTILIDAD Y CONVERSIÓN
    // =====================================================
    
    public String obtenerNombreItemDetalle(OrdenesCompraDetalle detalle) {
        if (detalle.getProducto() != null) {
            return detalle.getProducto().getNombreProducto();
        } else if (detalle.getSuministro() != null) {
            return detalle.getSuministro().getNombreSuministro();
        }
        return "Item no definido";
    }
    
    public String obtenerCodigoItemDetalle(OrdenesCompraDetalle detalle) {
        if (detalle.getProducto() != null) {
            return detalle.getProducto().getSerialProducto();
        } else if (detalle.getSuministro() != null) {
            return detalle.getSuministro().getCodigoSuministro();
        }
        return "";
    }
    
    public String obtenerTipoItemDetalle(OrdenesCompraDetalle detalle) {
        if (detalle.getProducto() != null) {
            return "Producto";
        } else if (detalle.getSuministro() != null) {
            return "Suministro";
        }
        return "No definido";
    }
    
    // =====================================================
    // MÉTODOS DE ANÁLISIS Y SEGUIMIENTO
    // =====================================================
    
    public boolean necesitaRecepcion(OrdenesCompraDetalle detalle) {
        return detalle.getCantidadRecibida() < detalle.getCantidadSolicitada();
    }
    
    public boolean estaCompletamenteRecibido(OrdenesCompraDetalle detalle) {
        return detalle.getCantidadRecibida().equals(detalle.getCantidadSolicitada());
    }
    
    public boolean tieneRecepcionParcial(OrdenesCompraDetalle detalle) {
        return detalle.getCantidadRecibida() > 0 && detalle.getCantidadRecibida() < detalle.getCantidadSolicitada();
    }
    
    public Integer obtenerCantidadPendiente(OrdenesCompraDetalle detalle) {
        return detalle.getCantidadSolicitada() - detalle.getCantidadRecibida();
    }
    
    public double obtenerPorcentajeRecepcion(OrdenesCompraDetalle detalle) {
        if (detalle.getCantidadSolicitada() == 0) {
            return 0.0;
        }
        return (detalle.getCantidadRecibida().doubleValue() / detalle.getCantidadSolicitada().doubleValue()) * 100;
    }
    
    public String obtenerEstadoRecepcion(OrdenesCompraDetalle detalle) {
        if (detalle.getCantidadRecibida() == 0) {
            return "Sin recepción";
        } else if (estaCompletamenteRecibido(detalle)) {
            return "Completo";
        } else if (tieneRecepcionParcial(detalle)) {
            return "Parcial";
        }
        return "Pendiente";
    }
    
    // =====================================================
    // MÉTODOS DE INTEGRACIÓN CON INVENTARIO
    // =====================================================
    
    public void procesarRecepcionCompleta(Integer idDetalle) {
        Optional<OrdenesCompraDetalle> detalleOpt = ordenesCompraDetalleRepository.findById(idDetalle);
        if (detalleOpt.isPresent()) {
            OrdenesCompraDetalle detalle = detalleOpt.get();
            detalle.setCantidadRecibida(detalle.getCantidadSolicitada());
            ordenesCompraDetalleRepository.save(detalle);
        }
    }
    
    public void procesarRecepcionParcial(Integer idDetalle, Integer cantidadRecibidaAdicional) {
        Optional<OrdenesCompraDetalle> detalleOpt = ordenesCompraDetalleRepository.findById(idDetalle);
        if (detalleOpt.isPresent()) {
            OrdenesCompraDetalle detalle = detalleOpt.get();
            Integer nuevaCantidadRecibida = detalle.getCantidadRecibida() + cantidadRecibidaAdicional;
            
            if (nuevaCantidadRecibida > detalle.getCantidadSolicitada()) {
                throw new IllegalArgumentException("La cantidad total recibida excede la cantidad solicitada");
            }
            
            detalle.setCantidadRecibida(nuevaCantidadRecibida);
            ordenesCompraDetalleRepository.save(detalle);
        }
    }
    
    // =====================================================
    // MÉTODOS DE EXPORTACIÓN Y REPORTES
    // =====================================================
    
    public List<OrdenesCompraDetalle> obtenerDetallesParaExportacion(OrdenesCompra ordenCompra) {
        return listarPorOrdenOrdenadoPorValor(ordenCompra);
    }
    
    public Object[] obtenerResumenOrden(OrdenesCompra ordenCompra) {
        long totalItems = contarDetallesPorOrden(ordenCompra);
        BigDecimal subtotal = calcularSubtotalOrden(ordenCompra);
        BigDecimal descuentos = calcularTotalDescuentosOrden(ordenCompra);
        BigDecimal impuestos = calcularTotalImpuestosOrden(ordenCompra);
        BigDecimal total = calcularValorTotalOrden(ordenCompra);
        Double porcentajeRecepcion = obtenerPorcentajePromedioRecepcion(ordenCompra);
        
        return new Object[]{
            totalItems,
            subtotal,
            descuentos,
            impuestos,
            total,
            porcentajeRecepcion
        };
    }
    
    // =====================================================
    // MÉTODOS DE VERIFICACIÓN Y CONTROL DE CALIDAD
    // =====================================================
    
    public boolean validarConsistenciaOrden(OrdenesCompra ordenCompra) {
        try {
            List<OrdenesCompraDetalle> detalles = listarPorOrden(ordenCompra);
            
            // Verificar que todos los detalles tienen valores válidos
            for (OrdenesCompraDetalle detalle : detalles) {
                validarDetalle(detalle);
                
                // Verificar que la orden del detalle coincide
                if (!detalle.getOrdenCompra().getIdOrdenes().equals(ordenCompra.getIdOrdenes())) {
                    return false;
                }
            }
            
            // Verificar que los totales calculados coinciden con los de la orden
            BigDecimal subtotalCalculado = calcularSubtotalOrden(ordenCompra);
            BigDecimal descuentosCalculados = calcularTotalDescuentosOrden(ordenCompra);
            BigDecimal impuestosCalculados = calcularTotalImpuestosOrden(ordenCompra);
            
            // Tolerancia para comparación de decimales
            BigDecimal tolerancia = new BigDecimal("0.01");
            
            return Math.abs(subtotalCalculado.subtract(ordenCompra.getMontoSubtotal()).doubleValue()) <= tolerancia.doubleValue() &&
                   Math.abs(descuentosCalculados.subtract(ordenCompra.getDescuentos() != null ? ordenCompra.getDescuentos() : BigDecimal.ZERO).doubleValue()) <= tolerancia.doubleValue() &&
                   Math.abs(impuestosCalculados.subtract(ordenCompra.getImpuestos() != null ? ordenCompra.getImpuestos() : BigDecimal.ZERO).doubleValue()) <= tolerancia.doubleValue();
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<String> obtenerErroresValidacion(OrdenesCompraDetalle detalle) {
        List<String> errores = new java.util.ArrayList<>();
        
        try {
            validarDetalle(detalle);
        } catch (IllegalArgumentException e) {
            errores.add(e.getMessage());
        }
        
        return errores;
    }

    public BigDecimal calcularSubtotalLista(List<OrdenesCompraDetalle> detalles) {
        if (detalles == null) return BigDecimal.ZERO;
        return detalles.stream()
                .map(OrdenesCompraDetalle::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el descuento total sumando los descuentos totales de todas las líneas.
     */
    public BigDecimal calcularTotalDescuentosLista(List<OrdenesCompraDetalle> detalles) {
        if (detalles == null) return BigDecimal.ZERO;
        return detalles.stream()
                .map(OrdenesCompraDetalle::getDescuentoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total de impuestos sumando los impuestos totales de todas las líneas.
     */
    public BigDecimal calcularTotalImpuestosLista(List<OrdenesCompraDetalle> detalles) {
        if (detalles == null) return BigDecimal.ZERO;
        return detalles.stream()
                .map(OrdenesCompraDetalle::getImpuestoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    
    @Autowired
    private InventarioAlmacenService inventarioService;
    @Autowired
    private OrdenesCompraDetalleRepository detalleRepository;
    
    public void actualizarCantidadesRecibidasYStock(List<Integer> detalleIds, List<Integer> cantidadesRecibidas) {
        if (detalleIds.size() != cantidadesRecibidas.size()) {
            throw new IllegalArgumentException("Las listas no coinciden en tamaño");
        }
        
        OrdenesCompra orden = null;
        
        for (int i = 0; i < detalleIds.size(); i++) {
            Integer detalleId = detalleIds.get(i);
            Integer cantidadRecibida = cantidadesRecibidas.get(i);
            
            // 1. Obtener el detalle
            OrdenesCompraDetalle detalle = detalleRepository.findById(detalleId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado: " + detalleId));
            
            // Guardar referencia a la orden (la necesitaremos al final)
            if (orden == null) {
                orden = detalle.getOrdenCompra();
            }
            
            // 2. Calcular la nueva recepción
            int cantidadAnterior = detalle.getCantidadRecibida();
            int nuevaRecepcion = cantidadRecibida - cantidadAnterior;
            
            if (nuevaRecepcion < 0) {
                throw new IllegalArgumentException("No se puede reducir la cantidad recibida");
            }
            
            if (nuevaRecepcion == 0) {
                continue; // Sin cambios
            }
            
            // 3. Actualizar cantidad recibida en el detalle
            detalle.setCantidadRecibida(cantidadRecibida);
            detalleRepository.save(detalle);
            
            // 4. Actualizar stock en inventario
            Almacen almacen = detalle.getOrdenCompra().getAlmacen();
            
            if (detalle.getProducto() != null) {
                inventarioService.actualizarStockRecepcion(
                    almacen.getIdAlmacen(),
                    detalle.getProducto().getIdProducto(),
                    null,
                    nuevaRecepcion,
                    "LOTE-" + detalle.getOrdenCompra().getIdOrdenes() // Genera un lote basado en la orden
                );
            } else if (detalle.getSuministro() != null) {
                inventarioService.actualizarStockRecepcion(
                    almacen.getIdAlmacen(),
                    null,
                    detalle.getSuministro().getIdSuministro(),
                    nuevaRecepcion,
                    "LOTE-" + detalle.getOrdenCompra().getIdOrdenes()
                );
            }
        }
        
        // 5. ⭐ ACTUALIZAR ESTADO DE LA ORDEN Y FECHA DE ENTREGA REAL
        if (orden != null) {
            actualizarEstadoOrden(orden);
        }
    }

    @Autowired
    private OrdenesCompraRepository ordenesCompraRepository;

    /**
     * Actualiza el estado de la orden según las cantidades recibidas.
     * También establece la fecha de entrega real cuando corresponde.
     */
    private void actualizarEstadoOrden(OrdenesCompra orden) {
        List<OrdenesCompraDetalle> detalles = detalleRepository.findByOrdenCompra(orden);
        
        boolean todoRecibido = detalles.stream()
            .allMatch(d -> d.getCantidadRecibida() >= d.getCantidadSolicitada());
        
        boolean algunoRecibido = detalles.stream()
            .anyMatch(d -> d.getCantidadRecibida() > 0);
        
        // ⭐ LÓGICA DE ACTUALIZACIÓN DE ESTADO Y FECHA
        if (todoRecibido) {
            orden.setEstadoOrden(OrdenesCompra.EstadoOrden.Recibida);
            // ✅ Establecer fecha de entrega real SOLO cuando está completamente recibida
            orden.setFechaEntregaReal(LocalDate.now());
            
        } else if (algunoRecibido) {
            orden.setEstadoOrden(OrdenesCompra.EstadoOrden.RecepcionParcial);
            // NO establecer fecha de entrega real en recepción parcial
            
        } else {
            // Sin recepciones, mantener estado actual (Pendiente o Confirmada)
            // NO establecer fecha de entrega real
        }
        
        ordenesCompraRepository.save(orden);
    }

    /**
     * Verifica si al menos un ítem de la orden ha registrado una cantidad recibida (> 0).
     * Esto se usa para cambiar el estado de CONFIRMADA a RECEPCION_PARCIAL.
     * @param orden La orden de compra a verificar.
     * @return true si al menos un detalle ha sido recibido parcialmente, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean algunDetalleRecibido(OrdenesCompra orden) {
        
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            return false;
        }
        
        // Se asume que la lista 'detalles' está cargada (EAGER o cargada previamente por el servicio).
        // Itera sobre los detalles y verifica si la cantidad recibida es mayor a cero para CUALQUIER detalle.
        return orden.getDetalles().stream()
                .anyMatch(detalle -> detalle.getCantidadRecibida() > 0);
        
        /* // Alternativa: Si prefieres no depender de la carga EAGER o LAZY en la entidad:
        // Usa una consulta directa al repositorio:
        
        return detalleRepository.existsByOrdenCompraAndCantidadRecibidaGreaterThan(orden, 0); 
        */
    }
    
    // El método que ya tenías:
    @Transactional(readOnly = true)
    public boolean todosLosDetallesCompletamenteRecibidos(OrdenesCompra orden) {
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            return false;
        }
        
        return orden.getDetalles().stream()
                .allMatch(detalle -> detalle.getCantidadRecibida() >= detalle.getCantidadSolicitada());
    }
}
