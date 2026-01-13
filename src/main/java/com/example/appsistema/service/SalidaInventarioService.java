package com.example.appsistema.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;

import com.example.appsistema.dto.SalidaInventarioDTO;
import com.example.appsistema.dto.SalidaInventarioDetalleDTO;
import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.Cliente;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.SalidaInventario;
import com.example.appsistema.model.SalidaInventario.EstadoSalida;
import com.example.appsistema.model.SalidaInventario.TipoComprobante;
import com.example.appsistema.model.SalidaInventario.TipoSalida;
import com.example.appsistema.model.SalidaInventarioDetalle;
import com.example.appsistema.model.Suministro;
import com.example.appsistema.model.Usuario;
import com.example.appsistema.repository.AlmacenRepository;
import com.example.appsistema.repository.ClienteRepository;
import com.example.appsistema.repository.InventarioAlmacenRepository;
import com.example.appsistema.repository.ProductoRepository;
import com.example.appsistema.repository.SalidaInventarioDetalleRepository;
import com.example.appsistema.repository.SalidaInventarioRepository;
import com.example.appsistema.repository.SuministroRepository;
import com.example.appsistema.repository.UsuarioRepository;


// Dependencias para la conversión a PDF
import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.thymeleaf.context.Context;

@Service
public class SalidaInventarioService {
    
    @Autowired
    private SalidaInventarioRepository salidaInventarioRepository;
    
    @Autowired
    private SalidaInventarioDetalleRepository detalleRepository;
    
    @Autowired
    private InventarioAlmacenRepository inventarioRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private SuministroRepository suministroRepository;

    @Autowired
    private TemplateEngine templateEngine;

    // ============================================
    // MÉTODOS CRUD BÁSICOS
    // ============================================
    
    public List<SalidaInventario> listarTodas() {
        return salidaInventarioRepository.findAll(Sort.by("idSalida").descending());
    }
    
    public Page<SalidaInventario> listarPaginadas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaSalida").descending());
        return salidaInventarioRepository.findAll(pageable);
    }
    
    public Optional<SalidaInventario> buscarPorId(Integer id) {
        return salidaInventarioRepository.findById(id);
    }
    
    public Optional<SalidaInventario> buscarPorNumeroSalida(String numeroSalida) {
        return salidaInventarioRepository.findByNumeroSalida(numeroSalida);
    }
    
    public SalidaInventario guardar(SalidaInventario salidaInventario) {
        return salidaInventarioRepository.save(salidaInventario);
    }
    
    public void eliminar(Integer id) {
        salidaInventarioRepository.deleteById(id);
    }
    
    public boolean existeNumeroSalida(String numeroSalida) {
        return salidaInventarioRepository.existsByNumeroSalida(numeroSalida);
    }
    
    // ============================================
    // MÉTODOS PARA HISTORIAL DE COMPRAS
    // ============================================
    
    /**
     * Obtener historial de compras por cliente con filtros
     */
    @Transactional(readOnly = true)
    public Page<SalidaInventario> obtenerHistorialPorCliente(
        Integer idCliente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoComprobanteStr,
        String estadoSalidaStr,
        String tipoSalidaStr,
        int page,
        int size) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("fechaSalida").descending().and(Sort.by("idSalida").descending()));
        
        // Convertir strings a enums
        TipoComprobante tipoComprobante = null;
        if (tipoComprobanteStr != null && !tipoComprobanteStr.isEmpty()) {
            try {
                tipoComprobante = TipoComprobante.valueOf(tipoComprobanteStr);
            } catch (IllegalArgumentException e) {}
        }
        
        EstadoSalida estadoSalida = null;
        if (estadoSalidaStr != null && !estadoSalidaStr.isEmpty()) {
            try {
                estadoSalida = EstadoSalida.valueOf(estadoSalidaStr);
            } catch (IllegalArgumentException e) {}
        }
        
        TipoSalida tipoSalida = null;
        if (tipoSalidaStr != null && !tipoSalidaStr.isEmpty()) {
            try {
                tipoSalida = TipoSalida.valueOf(tipoSalidaStr);
            } catch (IllegalArgumentException e) {}
        }
        
        Page<SalidaInventario> historialPage;

        // Si no hay filtros, retornar todas las salidas del cliente
        if (fechaInicio == null && fechaFin == null && 
            tipoComprobante == null && estadoSalida == null && tipoSalida == null) {
            
            historialPage = salidaInventarioRepository.findByIdCliente(idCliente, pageable);
        } else {
            // Usar query con filtros
            historialPage = salidaInventarioRepository.buscarConFiltros(
                idCliente, fechaInicio, fechaFin, 
                tipoComprobante, estadoSalida, tipoSalida, 
                pageable
            );
        }
        
        // Inicializar todas las entidades lazy necesarias
        historialPage.getContent().forEach(salida -> {
            // Inicialización de entidades relacionadas principales
            if (salida.getCliente() != null) {
                Hibernate.initialize(salida.getCliente());
            }
            if (salida.getUsuario() != null) {
                Hibernate.initialize(salida.getUsuario());
            }
            if (salida.getAlmacen() != null) {
                Hibernate.initialize(salida.getAlmacen());
            }
            
            // Inicialización de la colección de detalles
            if (salida.getDetalles() != null) {
                Hibernate.initialize(salida.getDetalles());
                
                // CRÍTICO: Inicializar las entidades dentro de cada detalle
                salida.getDetalles().forEach(detalle -> {
                    // Inicializar Producto si existe
                    if (detalle.getProducto() != null) {
                        Hibernate.initialize(detalle.getProducto());
                        
                        // Si el producto tiene relaciones lazy que necesitas, inicialízalas también
                        Producto producto = detalle.getProducto();
                        if (producto.getMarca() != null) {
                            Hibernate.initialize(producto.getMarca());
                        }
                        if (producto.getCategoria() != null) {
                            Hibernate.initialize(producto.getCategoria());
                        }
                        if (producto.getColor() != null) {
                            Hibernate.initialize(producto.getColor());
                        }
                        if (producto.getTalla() != null) {
                            Hibernate.initialize(producto.getTalla());
                        }
                        // Agrega otros campos si tu mapper los necesita
                    }
                    
                    // Inicializar Suministro si existe
                    if (detalle.getSuministro() != null) {
                        Hibernate.initialize(detalle.getSuministro());
                        
                        // Si el suministro tiene relaciones lazy, inicialízalas también
                        Suministro suministro = detalle.getSuministro();
                        if (suministro.getProveedor() != null) {
                            Hibernate.initialize(suministro.getProveedor());
                        }
                    }
                    
                    // Inicializar Almacen del detalle si existe
                    if (detalle.getAlmacen() != null) {
                        Hibernate.initialize(detalle.getAlmacen());
                    }
                });
            }
        });
        
        return historialPage;
    }
    
    /**
     * Obtener detalles de una salida
     */
    public List<SalidaInventarioDetalle> obtenerDetallesPorSalida(Integer idSalida) {
        return detalleRepository.findBySalidaInventario_IdSalidaOrderByIdDetalleSalidaAsc(idSalida);
    }
    
    /**
     * Contar total de compras de un cliente
     */
    public Long contarComprasPorCliente(Integer idCliente) {
        return salidaInventarioRepository.contarComprasPorCliente(idCliente);
    }
    
    /**
     * Calcular monto total de compras de un cliente
     */
    public BigDecimal calcularMontoTotalPorCliente(Integer idCliente) {
        return salidaInventarioRepository.calcularMontoTotalPorCliente(idCliente);
    }
    
    /**
     * Obtener última compra de un cliente
     */
    public Optional<SalidaInventario> obtenerUltimaCompraPorCliente(Integer idCliente) {
        Pageable pageable = PageRequest.of(0, 1);
        List<SalidaInventario> ultimas = salidaInventarioRepository
            .obtenerUltimasCompras(idCliente, pageable);
        
        return ultimas.isEmpty() ? Optional.empty() : Optional.of(ultimas.get(0));
    }
    
    /**
     * Contar compras pendientes de un cliente
     */
    public Long contarComprasPendientesPorCliente(Integer idCliente) {
        return salidaInventarioRepository.contarComprasPendientesPorCliente(idCliente);
    }
    
    /**
     * Guardar salida con detalles
     */
    @Transactional
    public SalidaInventario guardarConDetalles(SalidaInventario salida, List<SalidaInventarioDetalle> detalles) {
        // Guardar la salida
        SalidaInventario salidaGuardada = salidaInventarioRepository.save(salida);
        
        // Guardar los detalles
        if (detalles != null && !detalles.isEmpty()) {
            for (SalidaInventarioDetalle detalle : detalles) {
                detalle.setSalidaInventario(salidaGuardada);
                detalleRepository.save(detalle);
            }
        }
        
        return salidaGuardada;
    }
    
    /**
     * Actualizar estado de salida
     */
    public void actualizarEstado(Integer idSalida, EstadoSalida nuevoEstado) {
        Optional<SalidaInventario> salidaOpt = salidaInventarioRepository.findById(idSalida);
        if (salidaOpt.isPresent()) {
            SalidaInventario salida = salidaOpt.get();
            salida.setEstadoSalida(nuevoEstado);
            salidaInventarioRepository.save(salida);
        }
    }
    
    /**
     * Anular salida
     
    public void anularSalida(Integer idSalida) {
        actualizarEstado(idSalida, EstadoSalida.Anulada);
    }*/
    
    /**
     * Completar salida
     */
    public void completarSalida(Integer idSalida) {
        actualizarEstado(idSalida, EstadoSalida.Completada);
    }


    //          VISTA SALIDA INVENTARIO:

    @Transactional
    public SalidaInventarioDTO crearSalida(SalidaInventarioDTO dto) {
        
        // ✅ LOG INICIAL
        System.out.println("=== INICIANDO CREACIÓN DE SALIDA ===");
        System.out.println("ID Cliente recibido: " + dto.getIdCliente());
        System.out.println("ID Almacén recibido: " + dto.getIdAlmacen());
        System.out.println("Cantidad de detalles: " + dto.getDetalles().size());
        
        // PASO 1: Validar stock antes de crear
        for (SalidaInventarioDetalleDTO detalleDTO : dto.getDetalles()) {
            validarStock(detalleDTO);
        }
        
        // PASO 2: Crear y configurar la salida principal
        SalidaInventario salida = new SalidaInventario();
        salida.setNumeroSalida(dto.getNumeroSalida()); // Usar el número generado del frontend
        salida.setFechaSalida(dto.getFechaSalida());
        salida.setTipoSalida(TipoSalida.valueOf(dto.getTipoSalida()));
        salida.setTipoComprobante(SalidaInventario.TipoComprobante.valueOf(dto.getTipoComprobante()));
        salida.setSubtotal(dto.getSubtotal());
        salida.setImpuestos(dto.getImpuestos());
        salida.setDescuentos(dto.getDescuentos());
        salida.setMontoTotal(dto.getMontoTotal());
        salida.setEstadoSalida(EstadoSalida.Completada); // Directamente Completada
        
        // PASO 3: Asociar Usuario (obtenerlo del contexto de seguridad)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        salida.setUsuario(usuario);
        
        System.out.println("Usuario asignado: " + usuario.getUsername());
        
        // PASO 4: Asociar Cliente (SOLO SI EXISTE) ✅ SOLUCIÓN PARA idCliente NULL
        if (dto.getIdCliente() != null) {
            System.out.println("Buscando cliente con ID: " + dto.getIdCliente());
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getIdCliente()));
            salida.setCliente(cliente);
            System.out.println("Cliente asignado: " + cliente.getNombreCliente());
        } else {
            System.out.println("⚠️ No se proporcionó ID de cliente");
        }
        
        // PASO 5: Asociar Almacén
        Almacen almacen = almacenRepository.findById(dto.getIdAlmacen())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + dto.getIdAlmacen()));
        salida.setAlmacen(almacen);
        System.out.println("Almacén asignado: " + almacen.getNombreAlmacen());
        
        // ⭐ PASO 6: GUARDAR LA SALIDA PRIMERO (para obtener su ID)
        SalidaInventario salidaGuardada = salidaInventarioRepository.save(salida);
        
        System.out.println("✅ SALIDA GUARDADA - ID: " + salidaGuardada.getIdSalida());
        
        // ⭐ PASO 7: Crear detalles CON LA SALIDA GUARDADA (clave para la relación)
        List<SalidaInventarioDetalle> detalles = new ArrayList<>();
        
        for (SalidaInventarioDetalleDTO detalleDTO : dto.getDetalles()) {
            SalidaInventarioDetalle detalle = new SalidaInventarioDetalle();
            
            // ✅ ASIGNAR LA SALIDA GUARDADA (esto soluciona idSalida NULL)
            detalle.setSalidaInventario(salidaGuardada);
            
            // Configurar cantidades y precios
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setDescuentoUnitario(detalleDTO.getDescuentoUnitario());
            detalle.setImpuestoUnitario(detalleDTO.getImpuestoUnitario());
            detalle.setSubtotalLinea(detalleDTO.getSubtotalLinea());
            detalle.setLote(detalleDTO.getLote());
            
            // Asignar almacén del detalle
            detalle.setAlmacen(almacen);
            
            // Asignar Producto o Suministro
            if (detalleDTO.getIdProducto() != null) {
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                detalle.setProducto(producto);
                System.out.println("- Producto agregado: " + producto.getNombreProducto() + 
                                " | Cantidad: " + detalleDTO.getCantidad() + 
                                " | Lote: " + detalleDTO.getLote());
            }
            
            if (detalleDTO.getIdSuministro() != null) {
                Suministro suministro = suministroRepository.findById(detalleDTO.getIdSuministro())
                        .orElseThrow(() -> new RuntimeException("Suministro no encontrado"));
                detalle.setSuministro(suministro);
                System.out.println("- Suministro agregado: " + suministro.getNombreSuministro() + 
                                " | Cantidad: " + detalleDTO.getCantidad() + 
                                " | Lote: " + detalleDTO.getLote());
            }
            
            detalles.add(detalle);
            
            // PASO 8: Actualizar inventario (restar stock)
            actualizarInventario(detalleDTO, false);
        }
        
        // ⭐ PASO 9: Guardar todos los detalles con la relación establecida
        detalleRepository.saveAll(detalles);
        
        System.out.println("✅ DETALLES GUARDADOS: " + detalles.size() + " items");
        System.out.println("=== SALIDA CREADA EXITOSAMENTE ===\n");
        
        // PASO 10: Retornar el DTO con todos los datos
        return convertirADTO(salidaGuardada);
    }
    
    // ✅ Método auxiliar para validar stock
    private void validarStock(SalidaInventarioDetalleDTO detalle) {
        InventarioAlmacen inventario;
        String lote = detalle.getLote() != null ? detalle.getLote() : "";

        if (detalle.getIdProducto() != null) {
            inventario = inventarioRepository.findByAlmacenIdAlmacenAndProductoIdProductoAndLote(
                detalle.getIdAlmacen(), 
                detalle.getIdProducto(), 
                lote) 
                .orElseThrow(() -> new RuntimeException(
                    "Producto (Lote: " + lote + ") no disponible en el almacén"));
        } else {
            inventario = inventarioRepository.findByAlmacenIdAlmacenAndSuministroIdSuministroAndLote(
                detalle.getIdAlmacen(), 
                detalle.getIdSuministro(), 
                lote) 
                .orElseThrow(() -> new RuntimeException(
                    "Suministro (Lote: " + lote + ") no disponible en el almacén"));
        }
        
        // Validar stock disponible
        int stockDisponible = inventario.getStock() - inventario.getStockReservado();
        if (stockDisponible < detalle.getCantidad()) {
            throw new RuntimeException(
                "Stock insuficiente para el Lote " + lote + 
                ". Disponible: " + stockDisponible + 
                ", Solicitado: " + detalle.getCantidad());
        }
    }
    
    // ✅ Método auxiliar para actualizar inventario
    private void actualizarInventario(SalidaInventarioDetalleDTO detalle, boolean devolver) {
        InventarioAlmacen inventario;
        String lote = detalle.getLote() != null ? detalle.getLote() : "";

        if (detalle.getIdProducto() != null) {
            inventario = inventarioRepository.findByAlmacenIdAlmacenAndProductoIdProductoAndLote(
                detalle.getIdAlmacen(), 
                detalle.getIdProducto(), 
                lote) 
                .orElseThrow(() -> new RuntimeException(
                    "Inventario de Producto (Lote: " + lote + ") no encontrado"));
        } else {
            inventario = inventarioRepository.findByAlmacenIdAlmacenAndSuministroIdSuministroAndLote(
                detalle.getIdAlmacen(), 
                detalle.getIdSuministro(), 
                lote) 
                .orElseThrow(() -> new RuntimeException(
                    "Inventario de Suministro (Lote: " + lote + ") no encontrado"));
        }

        // Actualizar stock
        if (devolver) {
            inventario.setStock(inventario.getStock() + detalle.getCantidad());
            System.out.println("✅ Stock devuelto - Lote: " + lote + 
                            " | Nueva cantidad: " + inventario.getStock());
        } else {
            inventario.setStock(inventario.getStock() - detalle.getCantidad());
            System.out.println("✅ Stock descontado - Lote: " + lote + 
                            " | Nueva cantidad: " + inventario.getStock());
        }

        inventarioRepository.save(inventario);
    }
    
    @Transactional
    public void anularSalida(Integer idSalida) {
        SalidaInventario salida = salidaInventarioRepository.findByIdWithDetalles(idSalida)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));
        
        if (salida.getEstadoSalida() == EstadoSalida.Anulada) {
            throw new RuntimeException("La salida ya está anulada");
        }
        
        System.out.println("=== ANULANDO SALIDA: " + salida.getNumeroSalida() + " ===");
        
        // Devolver stock al inventario
        for (SalidaInventarioDetalle detalle : salida.getDetalles()) {
            SalidaInventarioDetalleDTO detalleDTO = new SalidaInventarioDetalleDTO();
            detalleDTO.setIdAlmacen(detalle.getAlmacen().getIdAlmacen());
            detalleDTO.setIdProducto(detalle.getProducto() != null ? 
                                    detalle.getProducto().getIdProducto() : null);
            detalleDTO.setIdSuministro(detalle.getSuministro() != null ? 
                                    detalle.getSuministro().getIdSuministro() : null);
            detalleDTO.setLote(detalle.getLote());
            detalleDTO.setCantidad(detalle.getCantidad());
            
            actualizarInventario(detalleDTO, true); // true = devolver stock
        }
        
        salida.setEstadoSalida(EstadoSalida.Anulada);
        salidaInventarioRepository.save(salida);
        
        System.out.println("✅ SALIDA ANULADA Y STOCK DEVUELTO\n");
    }
    
    public String generarNumeroSalida() {
        Integer maxNumero = salidaInventarioRepository.findMaxNumeroSalida();
        if (maxNumero == null) maxNumero = 0;
        return String.format("SAL%06d", maxNumero + 1);
    }
    
    public Page<SalidaInventarioDTO> buscarConFiltros(
            Integer clienteId, LocalDate fechaInicio, LocalDate fechaFin,
            String estadoSalida, String tipoSalida, Integer almacenId,
            Pageable pageable) {
        
        EstadoSalida estado = estadoSalida != null ? EstadoSalida.valueOf(estadoSalida) : null;
        TipoSalida tipo = tipoSalida != null ? TipoSalida.valueOf(tipoSalida) : null;
        
        Page<SalidaInventario> salidas = salidaInventarioRepository.findByFiltersWithDetails(
                clienteId, fechaInicio, fechaFin, estado, tipo, almacenId, pageable);
        
        return salidas.map(this::convertirADTO);
    }

    @Transactional
    public SalidaInventarioDTO obtenerPorId(Integer id) {
        SalidaInventario salida = salidaInventarioRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));
        return convertirADTO(salida);
    }
    
    public List<SalidaInventarioDTO> listarTodas2() {
        return salidaInventarioRepository.findAllWithEagerFetch().stream() 
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    private SalidaInventarioDTO convertirADTO(SalidaInventario salida) {
        SalidaInventarioDTO dto = new SalidaInventarioDTO();
        dto.setIdSalida(salida.getIdSalida());
        dto.setNumeroSalida(salida.getNumeroSalida());
        dto.setFechaSalida(salida.getFechaSalida());
        dto.setTipoSalida(salida.getTipoSalida().name());
        dto.setTipoComprobante(salida.getTipoComprobante().name());
        dto.setSubtotal(salida.getSubtotal());
        dto.setImpuestos(salida.getImpuestos());
        dto.setDescuentos(salida.getDescuentos());
        dto.setMontoTotal(salida.getMontoTotal());
        dto.setEstadoSalida(salida.getEstadoSalida().name());
        
        if (salida.getCliente() != null) {
            dto.setIdCliente(salida.getCliente().getIdCliente());
            dto.setNombreCliente(salida.getCliente().getNombreDisplay());
            // Asumo que la Entidad Cliente tiene estos campos:
            if (salida.getCliente().getTipoDocumento() != null) {
                dto.setTipoDocumentoCliente(salida.getCliente().getTipoDocumento().name()); 
            } else {
                dto.setTipoDocumentoCliente(null);
            }
            dto.setNumeroDocumentoCliente(salida.getCliente().getNumeroDocumento());
            dto.setDireccionCliente(salida.getCliente().getDireccion());
        }
        
        if (salida.getAlmacen() != null) {
            dto.setIdAlmacen(salida.getAlmacen().getIdAlmacen());
            dto.setNombreAlmacen(salida.getAlmacen().getNombreAlmacen());
            dto.setDireccionAlmacen(salida.getAlmacen().getUbicacion());
        }
        
        if (salida.getUsuario() != null) {
            dto.setIdUsuario(salida.getUsuario().getIdUsuario());
            dto.setNombreUsuario(salida.getUsuario().getUsername());
        }
        
        List<SalidaInventarioDetalleDTO> detallesDTO = salida.getDetalles().stream()

                .map(this::convertirDetalleADTO)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);
        
        return dto;
    }
    
    private SalidaInventarioDetalleDTO convertirDetalleADTO(SalidaInventarioDetalle detalle) {
        SalidaInventarioDetalleDTO dto = new SalidaInventarioDetalleDTO();
        dto.setIdDetalleSalida(detalle.getIdDetalleSalida());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setDescuentoUnitario(detalle.getDescuentoUnitario());
        dto.setImpuestoUnitario(detalle.getImpuestoUnitario());
        dto.setSubtotalLinea(detalle.getSubtotalLinea());
        dto.setLote(detalle.getLote());
        
        if (detalle.getProducto() != null) {
            dto.setIdProducto(detalle.getProducto().getIdProducto());
            dto.setNombreItem(detalle.getProducto().getNombreProducto());
            dto.setTipoItem("Producto");
        }
        
        if (detalle.getSuministro() != null) {
            dto.setIdSuministro(detalle.getSuministro().getIdSuministro());
            dto.setNombreItem(detalle.getSuministro().getNombreSuministro());
            dto.setTipoItem("Suministro");
        }
        
        if (detalle.getAlmacen() != null) {
            dto.setIdAlmacen(detalle.getAlmacen().getIdAlmacen());
            dto.setNombreAlmacen(detalle.getAlmacen().getNombreAlmacen());
        }
        
        return dto;
    }

     // ====================================================================
    // MÉTODO UNIFICADO DE GENERACIÓN PDF (Lógica de Enrutamiento)
    // ====================================================================

    public byte[] generarDocumentoPDF(Integer idSalida, String tipoDocumento) {
        
        SalidaInventarioDTO salida = obtenerPorId(idSalida); 

        // ===== AGREGAR ESTE DEBUG =====
        System.out.println("========== DEBUG PDF ==========");
        System.out.println("ID Salida: " + salida.getIdSalida());
        System.out.println("Número Salida: " + salida.getNumeroSalida());
        System.out.println("Cliente: " + salida.getNombreCliente());
        System.out.println("Fecha: " + salida.getFechaSalida());
        System.out.println("Total: " + salida.getMontoTotal());
        
        if (salida.getDetalles() != null) {
            System.out.println("Detalles count: " + salida.getDetalles().size());
            salida.getDetalles().forEach(d -> {
                System.out.println("  - Item: " + d.getNombreItem() + " | Cant: " + d.getCantidad());
            });
        } else {
            System.out.println("Detalles: NULL");
        }
        System.out.println("================================");
        // ===== FIN DEBUG =====
        
        // 1. Contexto de Thymeleaf (Variables compartidas)
        Context context = new Context();
        context.setVariable("salida", salida);
        context.setVariable("tituloEmpresa", "Empresa de Calzado D'Jhoney");
        context.setVariable("fechaDescarga", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Opcional: Si tienes un servicio de empresa, puedes cargar datos aquí:
        // context.setVariable("datosEmpresa", empresaService.obtenerDatosEmpresa());

        String nombrePlantilla = "";
        
        // 2. Lógica de Enrutamiento de Plantillas y Datos Específicos
        switch (tipoDocumento) {
            case "Boleta":
                nombrePlantilla = "documentos/boleta";
                break;
            case "Factura":
                nombrePlantilla = "documentos/factura";
                // Asegurar que el formato de Factura exija RUC y desglose de impuestos
                break;
            case "GuiaBase":
                nombrePlantilla = "documentos/guia_base";
                // Lógica clave: Punto de Partida (Almacén) y Punto de Llegada (Cliente)
                break;
            case "NotaVenta":
                nombrePlantilla = "documentos/nota_venta";
                break;
            default:
                throw new IllegalArgumentException("Tipo de documento PDF no válido: " + tipoDocumento);
        }
        
        // 3. Renderizar y Convertir
        String htmlContent = renderizarPlantilla(nombrePlantilla, context);
        return convertHtmlToPdf(htmlContent);
    }

    // ====================================================================
    // MÉTODOS PRIVADOS DE CONVERSIÓN (Implementación de Librerías)
    // ====================================================================

    private String renderizarPlantilla(String templateName, Context context) {
        String htmlContent = templateEngine.process(templateName, context);
        
        // DEBUG: Guarda el HTML en un archivo temporal para inspeccionarlo
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get("debug_output.html"), 
                htmlContent.getBytes()
            );
            System.out.println("HTML generado guardado en: debug_output.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return htmlContent;
    }
    
    private byte[] convertHtmlToPdf(String htmlContent) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(os, true);
            
            return os.toByteArray();
            
        } catch (DocumentException e) {
            throw new RuntimeException("Error fatal al convertir HTML a PDF. " + e.getMessage(), e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                // Ignorar o loggear
            }
        }
    }
}
