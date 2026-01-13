package com.example.appsistema.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appsistema.model.Suministro;
import com.example.appsistema.service.ProveedorService;
import com.example.appsistema.service.SuministroService;
import com.example.appsistema.service.TipoItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/suministros")
@RequiredArgsConstructor
@Slf4j
public class SuministroController {
    private final SuministroService suministroService;
    private final TipoItemService tipoItemService;
    private final ProveedorService proveedorService;


    // ==========================================
    // VISTA PRINCIPAL
    // ==========================================

    /**
     * Muestra la vista principal de suministros
     */
    @GetMapping
    public String vistaSuministros(Model model) {
        log.info("Accediendo a vista principal de suministros");
        
        try {
            // Cargar datos para los filtros
            model.addAttribute("tiposItem", tipoItemService.obtenerTodos2());
            model.addAttribute("proveedores", proveedorService.obtenerTodos2());
            
            // Cargar suministros iniciales (últimos 20)
            List<Suministro> suministrosIniciales = suministroService.findLatestSuministros(20);
            model.addAttribute("suministros", suministrosIniciales);
            model.addAttribute("totalSuministros", suministrosIniciales.size());
            
            return "admin/vistaSuministros";
            
        } catch (Exception e) {
            log.error("Error al cargar vista de suministros", e);
            model.addAttribute("error", "Error al cargar la página de suministros");
            return "admin/vistaSuministros";
        }
    }

    // ==========================================
    // OPERACIONES DE FILTRADO Y BÚSQUEDA
    // ==========================================

    /**
     * Aplica filtros y retorna suministros filtrados
     */
    @PostMapping("/filtrar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> filtrarSuministros(
            @RequestParam(required = false) String nombreSuministro,
            @RequestParam(required = false) Integer idTipoItem,
            @RequestParam(required = false) Integer idProveedor,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Aplicando filtros - Nombre: {}, TipoItem: {}, Categoria: {}, Proveedor: {}, Color: {}",
                nombreSuministro, idTipoItem, idProveedor);
        
        try {
            List<Suministro> suministrosFiltrados = suministroService.findWithFilters(
                nombreSuministro, idTipoItem, idProveedor, fechaInicio, fechaFin
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suministros", suministrosFiltrados);
            response.put("totalElements", suministrosFiltrados.size());
            response.put("message", "Filtros aplicados correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al aplicar filtros", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al aplicar filtros: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Búsqueda rápida por código o nombre
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarSuministros(
            @RequestParam String query) {
        
        log.debug("Búsqueda rápida: {}", query);
        
        try {
            List<Suministro> resultados = suministroService.findByCodigoOrNombre(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suministros", resultados);
            response.put("totalElements", resultados.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en búsqueda rápida", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==========================================
    // OPERACIONES CRUD
    // ==========================================

    /**
     * Muestra formulario para nuevo suministro
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.info("Mostrando formulario de nuevo suministro");
        
        model.addAttribute("suministro", new Suministro());
        cargarDatosFormulario(model);
        model.addAttribute("esNuevo", true);
        
        return "admin/formularioSuministro";
    }

    /**
     * Guarda nuevo suministro
     */
    @PostMapping("/registrar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarSuministro(
            @RequestParam("nombreSuministro") String nombreSuministro,
            @RequestParam("codigoSuministro") String codigoSuministro,

            @RequestParam("stockMinimo") int stockMinimo,
            @RequestParam("stockMaximo") int stockMaximo,
            @RequestParam("precioCompra") double precioCompra,
            @RequestParam("loteActual") String loteActual,
            @RequestParam("tipoItem.idTipoItem") int idTipoItem,
            @RequestParam("proveedor.idProveedor") int idProveedor,
            @RequestParam(value = "imagenURL", required = false) String imagenURL) {

        log.info("Registrando nuevo suministro: {}", nombreSuministro);
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validar si el código ya existe
            if (suministroService.existsByCodigoSuministro(codigoSuministro)) {
                response.put("success", false);
                response.put("message", "El código del suministro ya existe. Intente con otro.");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. Crear el objeto Suministro a partir de los parámetros
            Suministro suministro = new Suministro();
            suministro.setNombreSuministro(nombreSuministro);
            suministro.setCodigoSuministro(codigoSuministro);
            suministro.setStockMinimo(stockMinimo);
            suministro.setStockMaximo(stockMaximo);
            suministro.setPrecioCompra(BigDecimal.valueOf(precioCompra));
            suministro.setLoteActual(loteActual);

            // Cargar las entidades relacionadas usando los IDs
            suministro.setTipoItem(tipoItemService.obtenerPorId(idTipoItem).orElse(null));
            suministro.setProveedor(proveedorService.obtenerProveedorPorId(idProveedor).orElse(null));
            suministro.setImagenURL(imagenURL);

            // 4. Guardar el suministro en la base de datos
            suministroService.save(suministro);
            
            response.put("success", true);
            response.put("message", "Suministro registrado exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al registrar suministro", e);
            response.put("success", false);
            response.put("message", "Error al registrar el suministro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/actualizar") 
    @ResponseBody // <--- CRUCIAL: Esto asegura que se devuelva JSON, no una vista Thymeleaf
    public ResponseEntity<Map<String, Object>> actualizarSuministro(
            @RequestParam("idSuministro") int idSuministro,
            @RequestParam("nombreSuministro") String nombreSuministro,
            @RequestParam("codigoSuministro") String codigoSuministro,
            @RequestParam("stockMinimo") int stockMinimo,
            @RequestParam("stockMaximo") int stockMaximo,
            @RequestParam("precioCompra") double precioCompra,
            @RequestParam("loteActual") String loteActual,
            @RequestParam("tipoItem.idTipoItem") int idTipoItem,
            @RequestParam("proveedor.idProveedor") int idProveedor,
            @RequestParam(value = "imagenURL", required = false) String imagenURL) {
        
        log.info("Iniciando actualización de suministro ID: {}", idSuministro);
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validación de código: Verifica si el código ya existe en OTRO suministro
            if (suministroService.existsByCodigoSuministroAndNotId(codigoSuministro, idSuministro)) {
                response.put("success", false);
                response.put("message", "El código **" + codigoSuministro + "** ya está asignado a otro artículo.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 2. Obtener el suministro existente
            Suministro suministro = suministroService.findByIdWithRelations(idSuministro)
                                .orElseThrow(() -> new RuntimeException("Suministro no encontrado."));

            // 3. Establecer los nuevos valores
            suministro.setNombreSuministro(nombreSuministro);
            suministro.setCodigoSuministro(codigoSuministro);
            suministro.setStockMinimo(stockMinimo);
            suministro.setStockMaximo(stockMaximo);
            suministro.setPrecioCompra(BigDecimal.valueOf(precioCompra));
            suministro.setImagenURL(imagenURL);
            suministro.setLoteActual(loteActual);
            
            // Cargar las entidades relacionadas
            suministro.setTipoItem(tipoItemService.obtenerPorId(idTipoItem).orElse(null));
            suministro.setProveedor(proveedorService.obtenerProveedorPorId(idProveedor).orElse(null));
            
            // 4. Guardar (actualizar) en la base de datos
            suministroService.save(suministro); 
            
            response.put("success", true);
            response.put("message", "Suministro **actualizado exitosamente** en el sistema.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al actualizar suministro ID {}", idSuministro, e);
            response.put("success", false);
            response.put("message", "Error interno al procesar la actualización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * Obtiene detalles de un suministro (para modal)
     */
    @GetMapping("/{id}/detalles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetalles(@PathVariable Integer id) {
        log.debug("Obteniendo detalles del suministro ID: {}", id);
        
        try {
            Optional<Suministro> suministroOpt = suministroService.findByIdWithRelations(id);
            
            if (suministroOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Suministro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Suministro suministro = suministroOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suministro", suministro);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener detalles del suministro", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al cargar detalles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Muestra formulario de edición
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Integer id, Model model, 
                                          RedirectAttributes redirectAttributes) {
        
        log.info("Mostrando formulario de edición para suministro ID: {}", id);
        
        try {
            Optional<Suministro> suministroOpt = suministroService.findByIdWithRelations(id);
            
            if (suministroOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Suministro no encontrado");
                return "redirect:/admin/suministros";
            }
            
            model.addAttribute("suministro", suministroOpt.get());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", false);
            
            return "admin/formularioSuministro";
            
        } catch (Exception e) {
            log.error("Error al cargar suministro para edición", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar suministro para edición");
            return "redirect:/admin/suministros";
        }
    }

    /**
     * Elimina un suministro
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarSuministro(@PathVariable Integer id) {
        log.info("Eliminando suministro ID: {}", id);
        
        try {
            suministroService.deleteById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Suministro eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al eliminar suministro", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar suministro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==========================================
    // VALIDACIONES AJAX
    // ==========================================

    /**
     * Valida si un código de suministro ya existe
     */
    @GetMapping("/validar-codigo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarCodigoSuministro(
            @RequestParam String codigoSuministro,
            @RequestParam(required = false) Integer idSuministro) {
        
        try {
            boolean existe;
            if (idSuministro == null) {
                existe = suministroService.existsByCodigoSuministro(codigoSuministro);
            } else {
                existe = suministroService.existsByCodigoSuministroAndNotId(codigoSuministro, idSuministro);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("existe", existe);
            response.put("message", existe ? "El código ya está en uso" : "Código disponible");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al validar código", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en validación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==========================================
    // GENERACIÓN DE REPORTES
    // ==========================================

    /**
     * Genera y descarga reporte Excel
     */
    @PostMapping("/excel")
    public ResponseEntity<byte[]> generarReporteExcel(
            @RequestParam(required = false) String nombreSuministro,
            @RequestParam(required = false) Integer idTipoItem,
            @RequestParam(required = false) Integer idProveedor,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {
        
        log.info("Generando reporte Excel de suministros");
        
        try {
            // Obtener suministros según filtros
            List<Suministro> suministros;
            if (tieneAlgunFiltro(nombreSuministro, idTipoItem, idProveedor, fechaInicio, fechaFin)) {
                suministros = suministroService.findWithFilters(
                    nombreSuministro, idTipoItem, idProveedor, fechaInicio, fechaFin
                );
            } else {
                suministros = suministroService.findAllWithRelations();
            }
            
            // Generar Excel
            byte[] excelBytes = suministroService.generateExcelReport(suministros);
            
            // Configurar headers de respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nombreArchivo = "Reporte_Suministros_" + fechaActual + ".xlsx";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(excelBytes.length);
            
            log.info("Reporte Excel generado exitosamente: {} suministros", suministros.size());
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Error al generar reporte Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    /**
     * Carga los datos necesarios para el formulario
     */
    private void cargarDatosFormulario(Model model) {
        try {
            model.addAttribute("tiposItem", tipoItemService.obtenerTodos2());
            model.addAttribute("proveedores", proveedorService.obtenerTodos2());
        } catch (Exception e) {
            log.error("Error al cargar datos del formulario", e);
            model.addAttribute("error", "Error al cargar datos del formulario");
        }
    }

    /**
     * Verifica si hay algún filtro aplicado
     */
    private boolean tieneAlgunFiltro(String nombre, Integer tipoItem, 
                                   Integer proveedor, LocalDate fechaInicio, LocalDate fechaFin) {
        return (nombre != null && !nombre.trim().isEmpty()) ||
               tipoItem != null ||
               proveedor != null ||
               fechaInicio != null ||
               fechaFin != null;
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    /**
     * Maneja excepciones generales del controlador
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model, RedirectAttributes redirectAttributes) {
        log.error("Error no controlado en SuministroController", e);
        
        if (model != null) {
            model.addAttribute("error", "Ha ocurrido un error inesperado");
            return "admin/vistaSuministros";
        } else {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error inesperado");
            return "redirect:/admin/suministros";
        }
    }
}
