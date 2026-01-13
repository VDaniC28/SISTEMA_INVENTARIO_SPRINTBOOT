package com.example.appsistema.controller;


import java.time.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.dto.SalidaInventarioDTO;
import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.Cliente;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.Producto;

import com.example.appsistema.model.Suministro;
import com.example.appsistema.repository.AlmacenRepository;
import com.example.appsistema.repository.ClienteRepository;
import com.example.appsistema.repository.InventarioAlmacenRepository;

import com.example.appsistema.repository.UsuarioRepository;
import com.example.appsistema.service.SalidaInventarioService;

@Controller
@RequestMapping("/admin/salidas-inventario")
public class SalidaInventarioController {
    @Autowired
    private SalidaInventarioService salidaService;
    
   

    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;

    
    @Autowired
    private InventarioAlmacenRepository inventarioRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Muestra la vista principal de salidas de inventario
     */
    @GetMapping
    public String verSalidas(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("almacenes", almacenRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/vistaSalidaInventario";
    }
    
    /**
     * Lista todas las salidas con filtros y paginación
     */
    @GetMapping("/listar")
    @ResponseBody
    public Page<SalidaInventarioDTO> listarSalidas(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String estadoSalida,
            @RequestParam(required = false) String tipoSalida,
            @RequestParam(required = false) Integer almacenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaSalida").descending());
        return salidaService.buscarConFiltros(clienteId, fechaInicio, fechaFin, 
                estadoSalida, tipoSalida, almacenId, pageable);
    }
    
    /**
     * Obtiene una salida específica por ID con sus detalles
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<SalidaInventarioDTO> obtenerSalida(@PathVariable Integer id) {
        System.out.println("ID de Salida Recibido: " + id);
        try {
            SalidaInventarioDTO salida = salidaService.obtenerPorId(id);
            return ResponseEntity.ok(salida);
        } catch (RuntimeException e) {
            // Si tu servicio lanza una excepción específica (ej. NotFoundException) 
            // cuando el ID no existe, es mejor devolver 404.
            return ResponseEntity.notFound().build(); // Devuelve 404 sin cuerpo JSON (JS aún fallará si no lo corriges)
        }catch (Exception e) {
            // Para errores inesperados, devuelve 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
        }
    }
    
    /**
     * Crea una nueva salida de inventario
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearSalida(@RequestBody SalidaInventarioDTO dto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ✅ VALIDACIONES PREVIAS
            if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe agregar al menos un item a la salida");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getIdAlmacen() == null) {
                response.put("success", false);
                response.put("message", "Debe seleccionar un almacén");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getFechaSalida() == null) {
                response.put("success", false);
                response.put("message", "Debe especificar una fecha de salida");
                return ResponseEntity.badRequest().body(response);
            }
            
            /**
             * 
            // ✅ LOG PARA DEBUGGING - Ver qué llega del frontend
            System.out.println("=== DATOS RECIBIDOS PARA CREAR SALIDA ===");
            System.out.println("Número Salida: " + dto.getNumeroSalida());
            System.out.println("ID Cliente: " + dto.getIdCliente());
            System.out.println("ID Almacén: " + dto.getIdAlmacen());
            System.out.println("Cantidad de Detalles: " + dto.getDetalles().size());
            System.out.println("==========================================");*/
            
            // ✅ LLAMAR AL SERVICIO (aquí es donde debe estar la lógica)
            SalidaInventarioDTO salidaGuardada = salidaService.crearSalida(dto);
            
             /**
             * 
            // ✅ LOG DESPUÉS DE GUARDAR
            System.out.println("=== SALIDA GUARDADA EXITOSAMENTE ===");
            System.out.println("ID Salida: " + salidaGuardada.getIdSalida());
            System.out.println("Número Salida: " + salidaGuardada.getNumeroSalida());
            System.out.println("=====================================");*/
            
            response.put("success", true);
            response.put("message", "Salida registrada correctamente");
            response.put("data", salidaGuardada);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Errores de validación del servicio
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            // Errores de negocio (ej: stock insuficiente)
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            // Errores inesperados
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Anula una salida y devuelve el stock al inventario
     */
    @PutMapping("/anular/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> anularSalida(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            salidaService.anularSalida(id);
            response.put("success", true);
            response.put("message", "Salida anulada correctamente. Stock devuelto al inventario.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Genera el siguiente número de salida disponible
     */
    @GetMapping("/generar-numero")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generarNumero() {
        Map<String, String> response = new HashMap<>();
        response.put("numeroSalida", salidaService.generarNumeroSalida());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene los productos disponibles en un almacén específico
     */
    @GetMapping("/productos-almacen/{almacenId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerProductosPorAlmacen(@PathVariable Integer almacenId) {
        try {
            // 1. Usar el método optimizado del Repository
            List<InventarioAlmacen> inventarios = inventarioRepository.findAvailableProductsByAlmacenId(almacenId);
            
            // 2. Mapear directamente los inventarios (el filtro de stock > 0 ya lo hace la consulta)
            List<Map<String, Object>> productos = inventarios.stream()
                .map(inv -> {
                    Map<String, Object> prod = new HashMap<>();
                    Producto p = inv.getProducto();
                    
                    // Mapeo de la información necesaria para la Salida de Inventario
                    prod.put("idProducto", p.getIdProducto());
                    prod.put("nombreProducto", p.getNombreProducto());
                    prod.put("serialProducto", p.getSerialProducto());
                    prod.put("precioVenta", p.getPrecioVenta());
                    
                    // Detalles de inventario esenciales para la selección de stock
                    prod.put("stockDisponible", inv.getStock()); 
                    prod.put("lote", inv.getLote());               
                    prod.put("idInventario", inv.getIdInventario()); // Útil si necesitas la referencia directa al registro
                    
                    return prod;
                })
                .toList();

            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            // Logger.error("Error al obtener productos por almacén: {}", e.getMessage());
            // Deberías devolver un 500 o un 404 más específico si el almacén no existe.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene los suministros disponibles en un almacén específico
     */
    @GetMapping("/suministros-almacen/{almacenId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerSuministrosPorAlmacen(@PathVariable Integer almacenId) {
        try {
            // 1. Usar el método optimizado para obtener suministros disponibles
            List<InventarioAlmacen> inventarios = inventarioRepository.findAvailableSuministrosByAlmacenId(almacenId);
            
            // 2. Mapear los resultados
            List<Map<String, Object>> suministros = inventarios.stream()
                // Ya no es necesario el filtro .filter(inv -> inv.getStock() > 0)
                .map(inv -> {
                    Map<String, Object> sum = new HashMap<>();
                    Suministro s = inv.getSuministro();
                    
                    // Mapeo de la información esencial
                    sum.put("idSuministro", s.getIdSuministro());
                    sum.put("nombreSuministro", s.getNombreSuministro());
                    sum.put("codigoSuministro", s.getCodigoSuministro());
                    
                    // Nota: Usualmente se usa precioCompra para suministros, 
                    // pero si se venden al cliente, el precioVenta es más apropiado. 
                    // Asumo que el DTO solo necesita el dato del ítem.
                    sum.put("precioCompra", s.getPrecioCompra()); 
                    
                    // Detalles de inventario esenciales para la selección de stock
                    sum.put("stockDisponible", inv.getStock());
                    sum.put("lote", inv.getLote());
                    sum.put("idInventario", inv.getIdInventario()); // Útil para referenciar el registro exacto
                    
                    return sum;
                })
                .toList();

            return ResponseEntity.ok(suministros);
        } catch (Exception e) {
            // Mejor manejar el error para saber si es un error 500 (interno) o 400 (solicitud incorrecta)
            // Por ejemplo, si el error es solo por la consulta, un 500 es mejor.
            // Aquí dejo un HttpStatus.INTERNAL_SERVER_ERROR
            // System.err.println("Error al obtener suministros por almacén: " + e.getMessage()); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
        
    /**
     * Obtiene el stock disponible de un producto en un almacén
     */
    @GetMapping("/stock-producto/{almacenId}/{productoId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerStockProductoPorLote(
            @PathVariable Integer almacenId, 
            @PathVariable Integer productoId) {
        try {
            // Obtenemos todos los registros de inventario (cada uno es un lote)
            List<InventarioAlmacen> inventarios = inventarioRepository
                .findByAlmacenIdAlmacenAndProductoIdProducto(almacenId, productoId);

            if (inventarios.isEmpty()) {
                // Si la lista está vacía, no hay registros de ese producto
                throw new RuntimeException("Producto no encontrado en el almacén.");
            }
            
            // Mapeamos cada registro de inventario (lote) a un objeto Map
            List<Map<String, Object>> lotesDisponibles = inventarios.stream()
                .filter(inv -> inv.getStock() > 0) // Filtrar solo los lotes con stock > 0
                .map(inv -> {
                    Map<String, Object> loteInfo = new HashMap<>();
                    loteInfo.put("stockDisponible", inv.getStock());
                    loteInfo.put("lote", inv.getLote());
                    loteInfo.put("idInventario", inv.getIdInventario()); // Útil para operaciones futuras
                    return loteInfo;
                })
                .toList();
                
            return ResponseEntity.ok(lotesDisponibles);
        } catch (Exception e) {
            // En este caso, el endpoint devuelve List, por lo que el manejo de error cambia
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }
    }
    
    /**
     * Obtiene el stock disponible de un suministro en un almacén
     */
    @GetMapping("/stock-suministro/{almacenId}/{suministroId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerStockSuministro(
                @PathVariable Integer almacenId, 
                @PathVariable Integer suministroId) {
        try {
            // Usar el método de agregación para obtener el stock total (suma de todos los lotes)
            Long stockTotal = inventarioRepository.sumStockByAlmacenAndSuministro(almacenId, suministroId);
            
            // Si no hay stock, lanzamos una excepción o devolvemos un mensaje apropiado.
            if (stockTotal == 0) {
                throw new RuntimeException("Suministro no encontrado o sin stock en el almacén.");
            }
            
            Map<String, Object> response = new HashMap<>();
            // Devolvemos el stock consolidado
            response.put("stockDisponible", stockTotal);
            // NOTA: No se puede devolver un lote específico, ya que el stock es la suma de varios.
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Obtiene todos los clientes activos
     */
    @GetMapping("/clientes")
    @ResponseBody
    public ResponseEntity<List<Cliente>> obtenerClientes() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }
    
    /**
     * Obtiene todos los almacenes activos
     */
    @GetMapping("/almacenes")
    @ResponseBody
    public ResponseEntity<List<Almacen>> obtenerAlmacenes() {
        return ResponseEntity.ok(almacenRepository.findAll());
    }

    /**
     * Genera el documento (Boleta/Factura/Nota/Guía) en formato PDF para una Salida específica.
     * @param id El ID de la Salida de Inventario.
     * @return ResponseEntity con el PDF en bytes.
     */
    @GetMapping("/documento/{id}")
    public ResponseEntity<byte[]> generarDocumento(@PathVariable Integer id) {
        try {
            // 1. Obtener el DTO completo. La búsqueda y el manejo de "no encontrado"
            //    se delega al servicio (método obtenerPorId()).
            SalidaInventarioDTO salidaDTO = salidaService.obtenerPorId(id); 

            // 2. Usar la información del DTO para determinar la plantilla y el nombre del archivo.
            String tipoComprobante = salidaDTO.getTipoComprobante();
            String tipoDocumentoParaServicio;

            // 3. Determinar la lógica de generación
            switch (tipoComprobante) {
                case "Boleta":
                    tipoDocumentoParaServicio = "Boleta";
                    break;
                case "Factura":
                    tipoDocumentoParaServicio = "Factura";
                    break;
                case "Guia":
                    // Se mapea a la plantilla base
                    tipoDocumentoParaServicio = "GuiaBase"; 
                    break;
                case "Nota":
                    tipoDocumentoParaServicio = "NotaVenta";
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de comprobante no soportado: " + tipoComprobante);
            }

            // 4. Llamar al servicio que genera el PDF genérico
            byte[] pdfBytes = salidaService.generarDocumentoPDF(id, tipoDocumentoParaServicio); 

            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }

            // 5. Configurar Headers
            String filename = tipoDocumentoParaServicio + "_" + salidaDTO.getNumeroSalida() + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Captura errores como "Salida no encontrada" o "Tipo de documento no válido"
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(("Error en el proceso: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error interno al generar el documento: " + e.getMessage()).getBytes());
        }
    }
}
