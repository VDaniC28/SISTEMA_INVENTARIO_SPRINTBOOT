package com.example.appsistema.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.OrdenesCompraDetalle;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Proveedor;
import com.example.appsistema.model.Suministro;
import com.example.appsistema.service.AlmacenService;
import com.example.appsistema.service.OrdenesCompraDetalleService;
import com.example.appsistema.service.OrdenesCompraService;
import com.example.appsistema.service.ProductoService;
import com.example.appsistema.service.ProveedorService;
import com.example.appsistema.service.SuministroService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
@RequestMapping("/admin/ordenes-compra")
public class OrdenesCompraController {
    
    @Autowired
    private OrdenesCompraService ordenesCompraService;
    
    @Autowired
    private OrdenesCompraDetalleService ordenesCompraDetalleService;
    
    @Autowired
    private ProveedorService proveedorService;
    
    @Autowired
    private AlmacenService almacenService;
    
    // =====================================================
    // VISTA ÚNICA DE ÓRDENES DE COMPRA 
    // =====================================================
    
    @GetMapping
    public String vistaOrdenesCompra(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaOrden") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer proveedorId,
            @RequestParam(required = false) Integer almacenId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {
        
        // Configurar paginación y ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Preparar filtros
        Proveedor proveedor = null;
        if (proveedorId != null) {
            Optional<Proveedor> proveedorOpt = proveedorService.obtenerProveedorPorId(proveedorId);
            if (proveedorOpt.isPresent()) {
                proveedor = proveedorOpt.get();
            }
        }
        
        Almacen almacen = null;
        if (almacenId != null) {
            Optional<Almacen> almacenOpt = almacenService.obtenerPorId(almacenId);
            if (almacenOpt.isPresent()) {
                almacen = almacenOpt.get();
            }
        }
        
        OrdenesCompra.EstadoOrden estadoOrden = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoOrden = OrdenesCompra.EstadoOrden.valueOf(estado);
            } catch (IllegalArgumentException e) {
                // Estado inválido, se ignora
            }
        }
        
        // Obtener órdenes con filtros
        Page<OrdenesCompra> ordenesPage = ordenesCompraService.listarConFiltros(
            proveedor, almacen, estadoOrden, fechaInicio, fechaFin, pageable);
        
        // Preparar datos para la vista
        model.addAttribute("ordenesPage", ordenesPage);
        model.addAttribute("ordenes", ordenesPage.getContent());
        
        // Datos para filtros
        model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("almacenes", almacenService.obtenerTodos());
        model.addAttribute("estados", OrdenesCompra.EstadoOrden.values());
        
        // Filtros activos
        model.addAttribute("filtroProveedor", proveedor);
        model.addAttribute("filtroAlmacen", almacen);
        model.addAttribute("filtroEstado", estadoOrden);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        
        // Parámetros de paginación
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Estadísticas para el header
        agregarEstadisticasAlModelo(model);
        
        return "admin/vistaOrdenesCompra";
    }
   @PostMapping("/guardar")
    @ResponseBody 
    public Map<String, Object> guardarOrden(
        @ModelAttribute("orden") OrdenesCompra orden,
        // Capturamos los IDs y el JSON de detalles que vienen del formulario
        @RequestParam("idProveedor") Integer idProveedor,
        @RequestParam("idAlmacen") Integer idAlmacen,
        @RequestParam("detallesJSON") String detallesJson, 
        BindingResult result) {
        
        Map<String, Object> response = new HashMap<>();

        // 1. Validaciones de Spring
        if (result.hasErrors()) {
            response.put("status", "error");
            response.put("message", "Error de validación en los campos de la orden.");
            return response;
        }

        try {
            // 2. LÓGICA DE ASIGNACIÓN DE PROVEEDOR Y ALMACÉN (Resuelve el error "Proveedor no asignado")
            
            // Busca las entidades completas (Proveedor y Almacen) usando sus IDs
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(idProveedor)
                                                .orElseThrow(() -> new IllegalArgumentException("El proveedor seleccionado no es válido o no existe."));
            
            Almacen almacen = almacenService.obtenerPorId(idAlmacen)
                                            .orElseThrow(() -> new IllegalArgumentException("El almacén seleccionado no es válido o no existe."));

            // Asigna las entidades completas a la orden
            orden.setProveedor(proveedor);
            orden.setAlmacen(almacen);
            
            // ⭐ AGREGAR ESTA LÍNEA: Establecer la fecha de orden automáticamente
            orden.setFechaOrden(LocalDate.now());
            
            // También puedes establecer el estado inicial si no viene del formulario
            if (orden.getEstadoOrden() == null) {
                orden.setEstadoOrden(OrdenesCompra.EstadoOrden.Pendiente);
            }
            // 3. DESERIALIZAR Y ASIGNAR LOS DETALLES DE LA ORDEN
            List<OrdenesCompraDetalle> detalles = convertirJsonADetalles(detallesJson); 
            
            // 4. Asignar la orden principal y la entidad Producto/Suministro a cada detalle
            for (OrdenesCompraDetalle detalle : detalles) {
                detalle.setOrdenCompra(orden); // Asigna la referencia de la Orden principal
                
                // Usamos los campos auxiliares mapeados por Jackson para buscar la entidad correcta
                if ("producto".equals(detalle.getTipoItem())) {
                    Producto p = productoService.obtenerProductoPorId(detalle.getIdItem())
                                                .orElseThrow(() -> new IllegalArgumentException("El producto ID " + detalle.getIdItem() + " no fue encontrado."));
                    detalle.setProducto(p);
                } else if ("suministro".equals(detalle.getTipoItem())) {
                    Suministro s = suministroService.findById(detalle.getIdItem())
                                                    .orElseThrow(() -> new IllegalArgumentException("El suministro ID " + detalle.getIdItem() + " no fue encontrado."));
                    detalle.setSuministro(s);
                } else {
                    throw new IllegalArgumentException("Tipo de ítem inválido para el detalle: " + detalle.getTipoItem());
                }
            }
            
            // Asigna la lista de detalles completa a la orden principal
            orden.setDetalles(detalles);


            // ⭐ CALCULAR LOS TOTALES AUTOMÁTICAMENTE DESDE LOS DETALLES
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal totalImpuestos = BigDecimal.ZERO;
            BigDecimal totalDescuentos = BigDecimal.ZERO;

            for (OrdenesCompraDetalle detalle : detalles) {
                // Calcular subtotal de cada línea
                BigDecimal subtotalLinea = detalle.getPrecioUnitario()
                                                .multiply(new BigDecimal(detalle.getCantidadSolicitada()));
                
                // Aplicar descuento si existe
                if (detalle.getDescuentoUnitario() != null && detalle.getDescuentoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal descuentoLinea = detalle.getDescuentoUnitario()
                                                    .multiply(new BigDecimal(detalle.getCantidadSolicitada()));
                    totalDescuentos = totalDescuentos.add(descuentoLinea);
                }
                
                // Aplicar impuesto si existe
                if (detalle.getImpuestoUnitario() != null && detalle.getImpuestoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal impuestoLinea = detalle.getImpuestoUnitario()
                                                    .multiply(new BigDecimal(detalle.getCantidadSolicitada()));
                    totalImpuestos = totalImpuestos.add(impuestoLinea);
                }
                
                subtotal = subtotal.add(subtotalLinea);
            }

            // Asignar los totales calculados a la orden
            orden.setMontoSubtotal(subtotal);
            orden.setImpuestos(totalImpuestos);
            orden.setDescuentos(totalDescuentos);

            // Validar que haya al menos un ítem
            if (detalles.isEmpty()) {
                throw new IllegalArgumentException("La orden debe tener al menos un ítem en el detalle");
            }

            // Validar que el subtotal sea mayor a cero
            if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto subtotal debe ser mayor a cero");
            }

            // 5. Guardar la orden (esto guardará los detalles por cascada)
            OrdenesCompra ordenGuardada = ordenesCompraService.guardarOrden(orden); 
            
            // 6. Respuesta exitosa
            response.put("status", "success");
            response.put("id", ordenGuardada.getIdOrdenes());
            response.put("message", "Orden de Compra guardada con éxito bajo el ID: " + ordenGuardada.getIdOrdenes());
            
        } catch (IllegalArgumentException e) {
            // Captura errores de "no encontrado" o validaciones explícitas
            response.put("status", "error");
            response.put("message", "Error de validación en la orden: " + e.getMessage());
        } catch (Exception e) {
            // 7. Manejo de errores generales (incluyendo fallos de DB o JSON)
            response.put("status", "error");
            response.put("message", "Error interno al guardar la orden: " + e.getMessage());
            // Puedes loguear 'e' para depuración
            // log.error("Fallo al guardar la orden", e); 
        }

        return response;
    } 
    // =====================================================
    // API PARA OBTENER DETALLES DE UNA ORDEN (MODAL/EXPANDIBLE)
    // =====================================================
    
    @GetMapping("/api/detalles/{id}")
    @ResponseBody
    @Transactional(readOnly = true) // Mantén esto por seguridad
    public ResponseEntity<Map<String, Object>> obtenerDetallesOrden(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            
            Optional<OrdenesCompra> ordenOpt = ordenesCompraService.obtenerPorIdConDetalles(id);

            if (ordenOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Orden no encontrada");
                return ResponseEntity.ok(response);
            }
            
            OrdenesCompra orden = ordenOpt.get();
            List<OrdenesCompraDetalle> detalles = ordenesCompraDetalleService.listarPorOrden(orden);
            
            // Información de la orden
            Map<String, Object> ordenData = new HashMap<>();
            ordenData.put("id", orden.getIdOrdenes());
            ordenData.put("numeroOrden", orden.getNumeroOrdenFormateado());
            ordenData.put("fechaOrden", orden.getFechaOrden().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ordenData.put("proveedor", orden.getProveedor().getNombreProveedor());
            ordenData.put("almacen", orden.getAlmacen().getNombreAlmacen());
            ordenData.put("fechaEntregaEsperada", orden.getFechaEntregaEsperada() != null ? 
                orden.getFechaEntregaEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            ordenData.put("fechaEntregaReal", orden.getFechaEntregaReal() != null ? 
                orden.getFechaEntregaReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            ordenData.put("estado", orden.getEstadoOrden().name());
            ordenData.put("estadoTexto", getEstadoTexto(orden.getEstadoOrden()));
            
            // Detalles de la orden
            List<Map<String, Object>> detallesData = new ArrayList<>();
            for (OrdenesCompraDetalle detalle : detalles) {
                Map<String, Object> detalleData = new HashMap<>();
                detalleData.put("id", detalle.getIdDetalle());
                detalleData.put("nombreItem", detalle.getNombreItem());
                detalleData.put("codigoItem", detalle.getCodigoItem());
                detalleData.put("tipoItem", detalle.esProducto() ? "Producto" : "Suministro");
                detalleData.put("cantidadSolicitada", detalle.getCantidadSolicitada());
                detalleData.put("cantidadRecibida", detalle.getCantidadRecibida());
                detalleData.put("cantidadPendiente", detalle.getCantidadPendiente());
                detalleData.put("precioUnitario", detalle.getPrecioUnitario());
                detalleData.put("descuentoUnitario", detalle.getDescuentoUnitario());
                detalleData.put("impuestoUnitario", detalle.getImpuestoUnitario());
                detalleData.put("subtotalLinea", detalle.getSubtotalLinea());
                detalleData.put("totalLinea", detalle.getTotalLinea());
                detalleData.put("porcentajeRecepcion", detalle.getPorcentajeRecepcion());
                detalleData.put("estadoRecepcion", getEstadoRecepcionTexto(detalle));
                detallesData.add(detalleData);
            }
            
            // Resumen de la orden
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalItems", detalles.size());
            resumen.put("subtotal", orden.getMontoSubtotal());
            resumen.put("descuentos", orden.getDescuentos());
            resumen.put("impuestos", orden.getImpuestos());
            resumen.put("total", orden.getMontoTotal());
            
            // Estados de los botones
            Map<String, Object> permisos = new HashMap<>();
            permisos.put("puedeEditar", orden.puedeSerEditada());
            permisos.put("puedeConfirmar", orden.puedeSerConfirmada());
            permisos.put("puedeCancelar", orden.puedeSerCancelada());
            permisos.put("puedeRecepcion", orden.puedeHacerRecepcion());
            
            response.put("orden", ordenData);
            response.put("detalles", detallesData);
            response.put("resumen", resumen);
            response.put("permisos", permisos);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener detalles: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // =====================================================
    // API PARA BÚSQUEDA CON FILTROS (AJAX)
    // =====================================================
    
    @PostMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarOrdenesAjax(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaOrden") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer proveedorId,
            @RequestParam(required = false) Integer almacenId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Configurar paginación
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Preparar filtros
            Proveedor proveedor = null;
            if (proveedorId != null) {
                proveedor = proveedorService.obtenerProveedorPorId(proveedorId).orElse(null);
            }
            
            Almacen almacen = null;
            if (almacenId != null) {
                almacen = almacenService.obtenerPorId(almacenId).orElse(null);
            }
            
            OrdenesCompra.EstadoOrden estadoOrden = null;
            if (estado != null && !estado.isEmpty()) {
                try {
                    estadoOrden = OrdenesCompra.EstadoOrden.valueOf(estado);
                } catch (IllegalArgumentException e) {
                    // Estado inválido
                }
            }
            
            LocalDate fechaInicioDate = null;
            LocalDate fechaFinDate = null;
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                fechaInicioDate = LocalDate.parse(fechaInicio);
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fechaFinDate = LocalDate.parse(fechaFin);
            }
            
            // Buscar órdenes
            Page<OrdenesCompra> ordenesPage = ordenesCompraService.listarConFiltros(
                proveedor, almacen, estadoOrden, fechaInicioDate, fechaFinDate, pageable);
            
            // Preparar datos para respuesta
            List<Map<String, Object>> ordenesData = new ArrayList<>();
            for (OrdenesCompra orden : ordenesPage.getContent()) {
                Map<String, Object> ordenData = new HashMap<>();
                ordenData.put("id", orden.getIdOrdenes());
                ordenData.put("numeroOrden", orden.getNumeroOrdenFormateado());
                ordenData.put("fechaOrden", orden.getFechaOrden().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ordenData.put("proveedor", orden.getProveedor().getNombreProveedor());
                ordenData.put("almacen", orden.getAlmacen().getNombreAlmacen());
                ordenData.put("fechaEntregaEsperada", orden.getFechaEntregaEsperada() != null ? 
                    orden.getFechaEntregaEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                ordenData.put("fechaEntregaReal", orden.getFechaEntregaReal() != null ? 
                    orden.getFechaEntregaReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                ordenData.put("estado", orden.getEstadoOrden().name());
                ordenData.put("estadoTexto", getEstadoTexto(orden.getEstadoOrden()));
                ordenData.put("montoTotal", orden.getMontoTotal());
                
                // Información de detalles (resumen)
                List<OrdenesCompraDetalle> detalles = ordenesCompraDetalleService.listarPorOrden(orden);
                ordenData.put("cantidadItems", detalles.size());
                
                // Estados de los botones
                ordenData.put("puedeEditar", orden.puedeSerEditada());
                ordenData.put("puedeConfirmar", orden.puedeSerConfirmada());
                ordenData.put("puedeCancelar", orden.puedeSerCancelada());
                ordenData.put("puedeRecepcion", orden.puedeHacerRecepcion());
                ordenesData.add(ordenData);
            }
            
            response.put("ordenes", ordenesData);
            response.put("totalPages", ordenesPage.getTotalPages());
            response.put("totalElements", ordenesPage.getTotalElements());
            response.put("currentPage", page);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al buscar las órdenes: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // =====================================================
    // ACCIONES AJAX SOBRE ÓRDENES
    // =====================================================
    
    @PostMapping("/api/confirmar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmarOrden(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            OrdenesCompra orden = ordenesCompraService.confirmarOrden(id);
            response.put("success", true);
            response.put("message", "Orden confirmada exitosamente");
            response.put("nuevoEstado", orden.getEstadoOrden().name());
            response.put("nuevoEstadoTexto", getEstadoTexto(orden.getEstadoOrden()));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al confirmar la orden: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/cancelar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelarOrden(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "") String motivo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            OrdenesCompra orden = ordenesCompraService.cancelarOrden(id, motivo);
            response.put("success", true);
            response.put("message", "Orden cancelada exitosamente");
            response.put("nuevoEstado", orden.getEstadoOrden().name());
            response.put("nuevoEstadoTexto", getEstadoTexto(orden.getEstadoOrden()));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cancelar la orden: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/actualizar-recepcion")
    @ResponseBody
    @Transactional // ⭐ IMPORTANTE
    public ResponseEntity<Map<String, Object>> actualizarRecepcion(
            @RequestParam List<Integer> detalleIds,
            @RequestParam List<Integer> cantidadesRecibidas) {
        
        Map<String, Object> response = new HashMap<>();
    
        try {
            if (detalleIds.size() != cantidadesRecibidas.size()) {
                response.put("success", false);
                response.put("message", "Error en los datos enviados");
                return ResponseEntity.ok(response);
            }
            
            // Actualizar recepciones y stock
            ordenesCompraDetalleService.actualizarCantidadesRecibidasYStock(detalleIds, cantidadesRecibidas);
            
            // 🔍 DEBUG: Verificar que se actualizó
            if (!detalleIds.isEmpty()) {
                OrdenesCompraDetalle primerDetalle = ordenesCompraDetalleService.obtenerPorId(detalleIds.get(0))
                    .orElse(null);
                if (primerDetalle != null) {
                    OrdenesCompra orden = primerDetalle.getOrdenCompra();
                    System.out.println("Estado de orden después de recepción: " + orden.getEstadoOrden());
                    System.out.println("Fecha entrega real: " + orden.getFechaEntregaReal());
                }
            }
            
            response.put("success", true);
            response.put("message", "Recepción registrada y stock actualizado exitosamente");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar recepción: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
    
    // =====================================================
    // API PARA DATOS DE FILTROS
    // =====================================================
    
    @GetMapping("/api/filtros")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDatosFiltros() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> proveedoresData = new ArrayList<>();
            for (Proveedor p : proveedorService.obtenerTodosLosProveedores()) {
                Map<String, Object> provData = new HashMap<>();
                provData.put("id", p.getIdProveedor());
                provData.put("nombre", p.getNombreProveedor());
                proveedoresData.add(provData);
            }
            
            List<Map<String, Object>> almacenesData = new ArrayList<>();
            for (Almacen a : almacenService.obtenerTodos()) {
                Map<String, Object> almData = new HashMap<>();
                almData.put("id", a.getIdAlmacen());
                almData.put("nombre", a.getNombreAlmacen());
                almacenesData.add(almData);
            }
            
            List<Map<String, Object>> estadosData = new ArrayList<>();
            for (OrdenesCompra.EstadoOrden estado : OrdenesCompra.EstadoOrden.values()) {
                Map<String, Object> estData = new HashMap<>();
                estData.put("value", estado.name());
                estData.put("texto", getEstadoTexto(estado));
                estadosData.add(estData);
            }
            
            response.put("proveedores", proveedoresData);
            response.put("almacenes", almacenesData);
            response.put("estados", estadosData);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar los datos de filtros");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // =====================================================
    // EXPORTACIÓN A EXCEL
    // =====================================================
    
    @GetMapping("/exportar/excel/{id}")
    @Transactional(readOnly = true) // ⭐ AGREGAR ESTA ANOTACIÓN
    public ResponseEntity<byte[]> exportarOrdenIndividual(@PathVariable Integer id) {
        try {
            Optional<OrdenesCompra> ordenOpt = ordenesCompraService.obtenerPorIdConDetalles(id);
            
            if (ordenOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            OrdenesCompra orden = ordenOpt.get();
            List<OrdenesCompraDetalle> detalles = ordenesCompraDetalleService.listarPorOrden(orden);
            
            // ⭐ FORZAR LA CARGA DE PRODUCTOS Y SUMINISTROS
            for (OrdenesCompraDetalle detalle : detalles) {
                if (detalle.getProducto() != null) {
                    detalle.getProducto().getNombreProducto(); // Forzar carga
                }
                if (detalle.getSuministro() != null) {
                    detalle.getSuministro().getNombreSuministro(); // Forzar carga
                }
            }
            
            byte[] excelBytes = generarExcelOrdenIndividual(orden, detalles);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", 
                "orden-" + orden.getNumeroOrdenFormateado() + "-" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            System.err.println("Error al generar Excel: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
        
    // =====================================================
    // API PARA ESTADÍSTICAS
    // =====================================================
    
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Conteos por estado
            long totalPendientes = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Pendiente);
            long totalConfirmadas = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Confirmada);
            long totalCanceladas = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Cancelada);
            
            // Montos por estado
            Double montoPendiente = ordenesCompraService.obtenerMontoTotalPorEstado(OrdenesCompra.EstadoOrden.Pendiente);
            Double montoConfirmado = ordenesCompraService.obtenerMontoTotalPorEstado(OrdenesCompra.EstadoOrden.Confirmada);
            
            // Órdenes especiales
            List<OrdenesCompra> ordenesVencidas = ordenesCompraService.listarOrdenesVencidas();
            List<OrdenesCompra> ordenesDelDia = ordenesCompraService.listarOrdenesDelDia();
            List<OrdenesCompra> ordenesProximas = ordenesCompraService.listarOrdenesConEntregaProxima(7);
            
            response.put("totalPendientes", totalPendientes);
            response.put("totalConfirmadas", totalConfirmadas);
            response.put("totalCanceladas", totalCanceladas);
            response.put("montoPendiente", montoPendiente != null ? montoPendiente : 0.0);
            response.put("montoConfirmado", montoConfirmado != null ? montoConfirmado : 0.0);
            response.put("ordenesVencidas", ordenesVencidas.size());
            response.put("ordenesDelDia", ordenesDelDia.size());
            response.put("ordenesProximas", ordenesProximas.size());
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // =====================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // =====================================================
    
    private void agregarEstadisticasAlModelo(Model model) {
        try {
            // Conteos básicos
            long totalPendientes = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Pendiente);
            long totalConfirmadas = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Confirmada);
            long totalCanceladas = ordenesCompraService.contarPorEstado(OrdenesCompra.EstadoOrden.Cancelada);
            
            model.addAttribute("totalPendientes", totalPendientes);
            model.addAttribute("totalConfirmadas", totalConfirmadas);
            model.addAttribute("totalCanceladas", totalCanceladas);
            model.addAttribute("totalOrdenes", totalPendientes + totalConfirmadas + totalCanceladas);
            
            // Órdenes especiales
            model.addAttribute("ordenesVencidas", ordenesCompraService.listarOrdenesVencidas().size());
            model.addAttribute("ordenesDelDia", ordenesCompraService.listarOrdenesDelDia().size());
            model.addAttribute("ordenesParaRecepcion", ordenesCompraService.listarOrdenesParaRecepcion().size());
            
        } catch (Exception e) {
            // En caso de error, establecer valores por defecto
            model.addAttribute("totalPendientes", 0);
            model.addAttribute("totalConfirmadas", 0);
            model.addAttribute("totalCanceladas", 0);
            model.addAttribute("totalOrdenes", 0);
        }
    }
    
    private String getEstadoTexto(OrdenesCompra.EstadoOrden estado) {
        switch (estado) {
            case Pendiente: return "Pendiente";
            case Confirmada: return "Confirmada";
            case Cancelada: return "Cancelada";
            default: return estado.name();
        }
    }
    
    private String getEstadoRecepcionTexto(OrdenesCompraDetalle detalle) {
        if (detalle.getCantidadRecibida() == 0) {
            return "Sin recepción";
        } else if (detalle.estaCompletamenteRecibido()) {
            return "Completo";
        } else if (detalle.tieneRecepcionParcial()) {
            return "Parcial";
        }
        return "Pendiente";
    }
    
    private byte[] generarExcelOrdenIndividual(OrdenesCompra orden, List<OrdenesCompraDetalle> detalles) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        
        // HOJA 1: Información de la Orden
        Sheet sheetOrden = workbook.createSheet("Información Orden");
        crearHojaInformacionOrdenConEncabezado(workbook, sheetOrden, orden);
        
        // HOJA 2: Detalles
        Sheet sheetDetalles = workbook.createSheet("Detalles");
        crearHojaDetallesOrdenConEncabezado(workbook, sheetDetalles, detalles);
        
        // Convertir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}

private void crearHojaInformacionOrdenConEncabezado(Workbook workbook, Sheet sheet, OrdenesCompra orden) {
    int rowNum = 0;
    
    // ========== ENCABEZADO EMPRESA ==========
    rowNum = crearEncabezadoEmpresa(workbook, sheet, rowNum);
    
    rowNum++; // Línea en blanco
    
    // ========== ESTILOS ==========
    CellStyle labelStyle = workbook.createCellStyle();
    Font labelFont = workbook.createFont();
    labelFont.setBold(true);
    labelStyle.setFont(labelFont);
    
    CellStyle valueStyle = workbook.createCellStyle();
    valueStyle.setWrapText(true);
    
    CellStyle currencyStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("S/ #,##0.00"));
    
    // ========== TÍTULO SECCIÓN ==========
    Row titleRow = sheet.createRow(rowNum++);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue("INFORMACIÓN DE LA ORDEN");
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
    titleStyle.setFont(titleFont);
    titleCell.setCellStyle(titleStyle);
    
    rowNum++; // Línea en blanco
    
    // ========== INFORMACIÓN BÁSICA ==========
    agregarCampo(sheet, rowNum++, "N° de Orden:", orden.getNumeroOrdenFormateado(), labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Fecha de Orden:", 
        orden.getFechaOrden().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Proveedor:", orden.getProveedor().getNombreProveedor(), labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Almacén:", orden.getAlmacen().getNombreAlmacen(), labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Fecha Entrega Esperada:", 
        orden.getFechaEntregaEsperada() != null ? 
        orden.getFechaEntregaEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "No especificada",
        labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Fecha Entrega Real:", 
        orden.getFechaEntregaReal() != null ? 
        orden.getFechaEntregaReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Pendiente",
        labelStyle, valueStyle);
    agregarCampo(sheet, rowNum++, "Estado:", getEstadoTexto(orden.getEstadoOrden()), labelStyle, valueStyle);
    
    rowNum++; // Línea en blanco
    
    // ========== TOTALES ==========
    Row subtotalRow = sheet.createRow(rowNum++);
    Cell subtotalLabel = subtotalRow.createCell(0);
    subtotalLabel.setCellValue("Subtotal:");
    subtotalLabel.setCellStyle(labelStyle);
    Cell subtotalValue = subtotalRow.createCell(1);
    subtotalValue.setCellValue(orden.getMontoSubtotal().doubleValue());
    subtotalValue.setCellStyle(currencyStyle);
    
    Row descuentosRow = sheet.createRow(rowNum++);
    Cell descuentosLabel = descuentosRow.createCell(0);
    descuentosLabel.setCellValue("Descuentos:");
    descuentosLabel.setCellStyle(labelStyle);
    Cell descuentosValue = descuentosRow.createCell(1);
    descuentosValue.setCellValue(orden.getDescuentos() != null ? orden.getDescuentos().doubleValue() : 0.0);
    descuentosValue.setCellStyle(currencyStyle);
    
    Row impuestosRow = sheet.createRow(rowNum++);
    Cell impuestosLabel = impuestosRow.createCell(0);
    impuestosLabel.setCellValue("Impuestos:");
    impuestosLabel.setCellStyle(labelStyle);
    Cell impuestosValue = impuestosRow.createCell(1);
    impuestosValue.setCellValue(orden.getImpuestos() != null ? orden.getImpuestos().doubleValue() : 0.0);
    impuestosValue.setCellStyle(currencyStyle);
    
    Row totalRow = sheet.createRow(rowNum++);
    Cell totalLabel = totalRow.createCell(0);
    totalLabel.setCellValue("TOTAL:");
    CellStyle totalLabelStyle = workbook.createCellStyle();
    Font totalFont = workbook.createFont();
    totalFont.setBold(true);
    totalFont.setFontHeightInPoints((short) 12);
    totalLabelStyle.setFont(totalFont);
    totalLabel.setCellStyle(totalLabelStyle);
    Cell totalValue = totalRow.createCell(1);
    totalValue.setCellValue(orden.getMontoTotal().doubleValue());
    CellStyle totalCurrencyStyle = workbook.createCellStyle();
    totalCurrencyStyle.cloneStyleFrom(currencyStyle);
    totalCurrencyStyle.setFont(totalFont);
    totalValue.setCellStyle(totalCurrencyStyle);
    
    // Ajustar columnas
    sheet.setColumnWidth(0, 6000);
    sheet.setColumnWidth(1, 8000);
}

private void crearHojaDetallesOrdenConEncabezado(Workbook workbook, Sheet sheet, List<OrdenesCompraDetalle> detalles) {
    int rowNum = 0;
    
    // ========== ENCABEZADO EMPRESA ==========
    rowNum = crearEncabezadoEmpresa(workbook, sheet, rowNum);
    
    rowNum++; // Línea en blanco
    
    // ========== TÍTULO SECCIÓN ==========
    Row titleRow = sheet.createRow(rowNum++);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue("DETALLE DE ITEMS");
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
    titleStyle.setFont(titleFont);
    titleCell.setCellStyle(titleStyle);
    
    rowNum++; // Línea en blanco
    
    // ========== ESTILOS PARA ENCABEZADOS ==========
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    
    CellStyle currencyStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("S/ #,##0.00"));
    
    CellStyle numberStyle = workbook.createCellStyle();
    numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0"));
    numberStyle.setAlignment(HorizontalAlignment.CENTER);
    
    // ========== ENCABEZADOS DE TABLA ==========
    Row headerRow = sheet.createRow(rowNum++);
    headerRow.setHeightInPoints(25); // Altura de fila de encabezado
    
    String[] headers = {
        "Tipo", "Código", "Nombre Item", 
        "Cant. Solicitada", "Cant. Recibida", "Cant. Pendiente",
        "Precio Unit.", "Descuento Unit.", "Impuesto Unit.", "Total Línea"
    };
    
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // ========== DATOS ==========
    for (OrdenesCompraDetalle detalle : detalles) {
        Row row = sheet.createRow(rowNum++);
        
        row.createCell(0).setCellValue(detalle.esProducto() ? "Producto" : "Suministro");
        row.createCell(1).setCellValue(detalle.getCodigoItem());
        row.createCell(2).setCellValue(detalle.getNombreItem());
        
        Cell cantSolCell = row.createCell(3);
        cantSolCell.setCellValue(detalle.getCantidadSolicitada());
        cantSolCell.setCellStyle(numberStyle);
        
        Cell cantRecCell = row.createCell(4);
        cantRecCell.setCellValue(detalle.getCantidadRecibida());
        cantRecCell.setCellStyle(numberStyle);
        
        Cell cantPenCell = row.createCell(5);
        cantPenCell.setCellValue(detalle.getCantidadPendiente());
        cantPenCell.setCellStyle(numberStyle);
        
        Cell precioCell = row.createCell(6);
        precioCell.setCellValue(detalle.getPrecioUnitario().doubleValue());
        precioCell.setCellStyle(currencyStyle);
        
        Cell descCell = row.createCell(7);
        descCell.setCellValue(detalle.getDescuentoUnitario() != null ? 
            detalle.getDescuentoUnitario().doubleValue() : 0.0);
        descCell.setCellStyle(currencyStyle);
        
        Cell impCell = row.createCell(8);
        impCell.setCellValue(detalle.getImpuestoUnitario() != null ? 
            detalle.getImpuestoUnitario().doubleValue() : 0.0);
        impCell.setCellStyle(currencyStyle);
        
        Cell totalCell = row.createCell(9);
        totalCell.setCellValue(detalle.getTotalLinea().doubleValue());
        totalCell.setCellStyle(currencyStyle);
    }
    
    // Autoajustar columnas
    for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
    }
}

// ========== MÉTODO PARA CREAR ENCABEZADO DE EMPRESA ==========
private int crearEncabezadoEmpresa(Workbook workbook, Sheet sheet, int startRow) {
    int rowNum = startRow;
    
    // Estilo para el título de la empresa
    CellStyle empresaStyle = workbook.createCellStyle();
    Font empresaFont = workbook.createFont();
    empresaFont.setBold(true);
    empresaFont.setFontHeightInPoints((short) 18);
    empresaFont.setColor(IndexedColors.DARK_BLUE.getIndex());
    empresaStyle.setFont(empresaFont);
    empresaStyle.setAlignment(HorizontalAlignment.CENTER);
    
    // Estilo para la fecha
    CellStyle fechaStyle = workbook.createCellStyle();
    Font fechaFont = workbook.createFont();
    fechaFont.setFontHeightInPoints((short) 10);
    fechaStyle.setFont(fechaFont);
    fechaStyle.setAlignment(HorizontalAlignment.RIGHT);
    
    // Título de la empresa
    Row empresaRow = sheet.createRow(rowNum++);
    empresaRow.setHeightInPoints(30);
    Cell empresaCell = empresaRow.createCell(0);
    empresaCell.setCellValue("Calzados de D'Jhoney");
    empresaCell.setCellStyle(empresaStyle);
    
    // Fecha de exportación (lado derecho) - ⭐ CAMBIO AQUÍ
    Row fechaRow = sheet.createRow(rowNum++);
    Cell fechaCell = fechaRow.createCell(0);
    String fechaExportacion = "Fecha de exportación: " + 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    fechaCell.setCellValue(fechaExportacion);
    fechaCell.setCellStyle(fechaStyle);
    
    // Merge cells para que el título ocupe toda la fila
    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        startRow, startRow, 0, 5
    ));
    
    // Merge cells para la fecha
    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        startRow + 1, startRow + 1, 0, 5
    ));
    
    return rowNum;
}

private void agregarCampo(Sheet sheet, int rowNum, String label, String value, 
                         CellStyle labelStyle, CellStyle valueStyle) {
    Row row = sheet.createRow(rowNum);
    Cell labelCell = row.createCell(0);
    labelCell.setCellValue(label);
    labelCell.setCellStyle(labelStyle);
    
    Cell valueCell = row.createCell(1);
    valueCell.setCellValue(value);
    valueCell.setCellStyle(valueStyle);
}

    // =====================================================
// API PARA OBTENER PRODUCTOS Y SUMINISTROS
// =====================================================

@Autowired
private ProductoService productoService; // Asegúrate de tener este service

@Autowired
private SuministroService suministroService; // Asegúrate de tener este service

// =====================================================
// API PARA OBTENER PRODUCTOS Y SUMINISTROS (GENERAL)
// =====================================================

@GetMapping("/api/productos")
@ResponseBody
public ResponseEntity<List<Map<String, Object>>> obtenerProductos() {
    // 💡 Llama al servicio para obtener la lista, luego pasa la lista al método auxiliar
    List<Producto> productos = productoService.obtenerTodos3(); 
    return obtenerItemsGeneral(productos, "producto");
}

@GetMapping("/api/suministros")
@ResponseBody
public ResponseEntity<List<Map<String, Object>>> obtenerSuministros() {
    // 💡 Llama al servicio para obtener la lista, luego pasa la lista al método auxiliar
    List<Suministro> suministros = suministroService.findAll(); // O el método que uses
    return obtenerItemsGeneral(suministros, "suministro");
}

// -----------------------------------------------------
// API PARA OBTENER PRODUCTOS Y SUMINISTROS (FILTRADO POR PROVEEDOR)
// -----------------------------------------------------

@GetMapping("/api/productos/proveedor/{proveedorId}")
@ResponseBody
public ResponseEntity<List<Map<String, Object>>> obtenerProductosPorProveedor(@PathVariable Integer proveedorId) {
    try {
        Optional<Proveedor> proveedorOpt = proveedorService.obtenerProveedorPorId(proveedorId);
        if (proveedorOpt.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>()); 
        }
        
        // 💡 Llama al servicio filtrado, luego pasa la lista al método auxiliar
        List<Producto> productos = productoService.obtenerProductosPorProveedor(proveedorOpt.get()); 
        return obtenerItemsGeneral(productos, "producto");
        
    } catch (Exception e) {
        System.err.println("Error al obtener productos por proveedor: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }
}

@GetMapping("/api/suministros/proveedor/{proveedorId}")
@ResponseBody
public ResponseEntity<List<Map<String, Object>>> obtenerSuministrosPorProveedor(@PathVariable Integer proveedorId) {
    try {
        Optional<Proveedor> proveedorOpt = proveedorService.obtenerProveedorPorId(proveedorId);
        if (proveedorOpt.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        // 💡 Llama al servicio filtrado, luego pasa la lista al método auxiliar
        List<Suministro> suministros = suministroService.obtenerPorProveedor(proveedorOpt.get());
        return obtenerItemsGeneral(suministros, "suministro");
        
    } catch (Exception e) {
        System.err.println("Error al obtener suministros por proveedor: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }
}

/**
 * Mapea una lista de Productos o Suministros a un formato JSON estándar para el frontend.
 */

 // =====================================================
// MÉTODO AUXILIAR PARA UNIFICAR LA LÓGICA DE MAPEO DE DATOS
// 💡 ESTE MÉTODO ES LLAMADO POR LOS 4 ENDPOINTS ANTERIORES
// =====================================================

private ResponseEntity<List<Map<String, Object>>> obtenerItemsGeneral(List<?> items, String tipo) {
    List<Map<String, Object>> itemsData = new ArrayList<>();
    
    try {
        for (Object item : items) {
            Map<String, Object> itemData = new HashMap<>();
            
            // Lógica de mapeo para Producto
            if (item instanceof Producto p) {
                itemData.put("id", p.getIdProducto());
                itemData.put("nombre", p.getNombreProducto());
                itemData.put("codigo", p.getSerialProducto()); // Estandarizado como 'codigo'
                itemData.put("precioVenta", p.getPrecioVenta()); // ⚠️ ASUMIMOS que este es el precio de compra
                
            // Lógica de mapeo para Suministro
            } else if (item instanceof Suministro s) {
                itemData.put("id", s.getIdSuministro());
                itemData.put("nombre", s.getNombreSuministro());
                itemData.put("codigo", s.getCodigoSuministro()); // Estandarizado como 'codigo'
                itemData.put("precioCompra", s.getPrecioCompra()); 
            } else {
                continue;
            }
            
            itemData.put("tipo", tipo); // "producto" o "suministro"
            itemsData.add(itemData);
        }
        
        return ResponseEntity.ok(itemsData);
        
    } catch (Exception e) {
        System.err.println("Error en el mapeo de items de tipo " + tipo + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }
}
@Autowired 
private ObjectMapper objectMapper; 

private List<OrdenesCompraDetalle> convertirJsonADetalles(String detallesJson) {
    // 💡 This method uses Jackson's ObjectMapper to convert the JSON string 
    // into a List of your detail objects.
    try {
        // TypeReference is necessary to handle the generic type List<T> correctly.
        return objectMapper.readValue(detallesJson, new TypeReference<List<OrdenesCompraDetalle>>() {});
    } catch (IOException e) {
        // Throw a runtime exception if the JSON conversion fails.
        // This will be caught by the main method's catch block.
        throw new RuntimeException("Fallo al deserializar los detalles de la orden: " + e.getMessage(), e);
    }
}

// Agregar estos métodos a tu OrdenesCompraController

@GetMapping("/formulario-edicion/{id}")
public String mostrarFormularioEdicionSimple(@PathVariable Integer id, Model model) {
    try {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraService.obtenerPorIdConDetalles(id);
        
        if (ordenOpt.isEmpty()) {
            model.addAttribute("error", "Orden no encontrada");
            return "redirect:/admin/ordenes-compra";
        }
        
        OrdenesCompra orden = ordenOpt.get();
        
        // Validación crítica
        if (!orden.puedeSerEditada()) {
            model.addAttribute("error", "Solo se pueden editar órdenes en estado Pendiente");
            return "redirect:/admin/ordenes-compra";
        }
        
        model.addAttribute("orden", orden);
        model.addAttribute("almacenes", almacenService.obtenerTodos());
        
        return "admin/modalEdicionOrdenSimple";
        
    } catch (Exception e) {
        model.addAttribute("error", "Error: " + e.getMessage());
        return "redirect:/admin/ordenes-compra";
    }
}

@PostMapping("/actualizar-simple/{id}")
@ResponseBody
@Transactional
public Map<String, Object> actualizarOrdenSimple(
        @PathVariable Integer id,
        @RequestParam(required = false) Integer idAlmacen,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntregaEsperada) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        Optional<OrdenesCompra> ordenOpt = ordenesCompraService.obtenerPorIdConDetalles(id);
        
        if (ordenOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Orden no encontrada");
            return response;
        }
        
        OrdenesCompra orden = ordenOpt.get();
        
        // Validación estricta
        if (!orden.puedeSerEditada()) {
            response.put("status", "error");
            response.put("message", "Esta orden no puede ser editada. Estado actual: " + orden.getEstadoOrden());
            return response;
        }
        
        // Actualizar solo campos permitidos
        if (idAlmacen != null) {
            Almacen almacen = almacenService.obtenerPorId(idAlmacen)
                    .orElseThrow(() -> new IllegalArgumentException("Almacén no válido"));
            orden.setAlmacen(almacen);
        }
        
        if (fechaEntregaEsperada != null) {
            // Validar que la fecha sea futura
            if (fechaEntregaEsperada.isBefore(LocalDate.now())) {
                response.put("status", "error");
                response.put("message", "La fecha de entrega debe ser futura");
                return response;
            }
            orden.setFechaEntregaEsperada(fechaEntregaEsperada);
        }
        
        ordenesCompraService.guardarOrden(orden);
        
        response.put("status", "success");
        response.put("message", "Orden actualizada exitosamente");
        
    } catch (IllegalArgumentException e) {
        response.put("status", "error");
        response.put("message", e.getMessage());
    } catch (Exception e) {
        response.put("status", "error");
        response.put("message", "Error al actualizar: " + e.getMessage());
    }
    
    return response;
}

}
