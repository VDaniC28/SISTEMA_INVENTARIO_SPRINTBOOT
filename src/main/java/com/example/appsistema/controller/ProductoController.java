package com.example.appsistema.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.model.Producto;
import com.example.appsistema.service.CategoriaService;
import com.example.appsistema.service.ColorService;
import com.example.appsistema.service.EstadoProductoService;
import com.example.appsistema.service.GeneroService;
import com.example.appsistema.service.MarcaService;
import com.example.appsistema.service.MaterialPrincipalService;
import com.example.appsistema.service.MaterialSuelaService;
import com.example.appsistema.service.ProductoService;
import com.example.appsistema.service.ProveedorService;
import com.example.appsistema.service.TallaService;
import com.example.appsistema.service.TipoItemService;
import com.example.appsistema.service.TipoPersonaService;
import com.example.appsistema.util.ImageDownloader;





@Controller
@RequestMapping("/admin/productos")
public class ProductoController {
    
    @Autowired
    private ProductoService productoService;

    @Autowired
    private TipoItemService tipoItemService;

    @Autowired
    private MarcaService marcaService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private GeneroService generoService;

    @Autowired
    private MaterialSuelaService materialSuelaService;

    @Autowired
    private TallaService tallaService;

    @Autowired
    private MaterialPrincipalService materialPrincipalService;

    @Autowired
    private TipoPersonaService tipoPersonaService;

    @Autowired
    private EstadoProductoService estadoProductoService;

    // Mostrar vista principal de productos
    @GetMapping
    public String mostrarVistaProductos(Model model) {
        // Cargar todos los productos
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        model.addAttribute("productos", productos);

        // Cargar todos los catálogos para los filtros
        cargarCatalogosEnModelo(model);

        return "admin/vistaProducto";
    }

    // Obtener productos con filtros (AJAX)
    @GetMapping("/filtrar")
    @ResponseBody
    public ResponseEntity<List<Producto>> filtrarProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRegistro,
            @RequestParam(required = false) Integer idTipoItem,
            @RequestParam(required = false) Integer idMarca,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idProveedor,
            @RequestParam(required = false) Integer idColor,
            @RequestParam(required = false) Integer idGenero,
            @RequestParam(required = false) Integer idMaterialSuela,
            @RequestParam(required = false) Integer idTalla,
            @RequestParam(required = false) Integer idMaterialPrincipal,
            @RequestParam(required = false) Integer idTipoPersona,
            @RequestParam(required = false) Integer idEstadoProducto) {

        try {
            List<Producto> productos = productoService.filtrarProductos(
                    fechaRegistro, idTipoItem, idMarca, idCategoria, idProveedor,
                    idColor, idGenero, idMaterialSuela, idTalla, idMaterialPrincipal,
                    idTipoPersona, idEstadoProducto);

            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Obtener producto por ID (AJAX)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        
        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Guardar nuevo producto
    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarProducto(@RequestBody Producto producto) {
        try {
            // 🧩 Validar duplicado por serial
            if (productoService.existeProductoConSerial(producto.getSerialProducto())) {
                return ResponseEntity.badRequest()
                    .body("Ya existe un producto con el serial: " + producto.getSerialProducto());
            }

            // 🧩 Validar campos del producto
            productoService.validarProducto(producto);

            // 📸 Si la URL de imagen apunta a internet, descargarla y guardarla localmente
            if (producto.getImagenURL() != null && producto.getImagenURL().startsWith("http")) {
                try {
                    URL url = new URL(producto.getImagenURL());
                    
                    // Detectar extensión (.jpg / .png)
                    String extension = ".jpg";
                    if (producto.getImagenURL().toLowerCase().endsWith(".png")) {
                        extension = ".png";
                    } else if (producto.getImagenURL().toLowerCase().endsWith(".jpeg")) {
                        extension = ".jpeg";
                    }

                    // Generar nombre único
                    String fileName = UUID.randomUUID() + extension;

                    // Crear carpeta si no existe
                    Path uploadDir = Paths.get("uploads/productos");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    // Descargar imagen
                    Path target = uploadDir.resolve(fileName);
                    try (InputStream in = url.openStream()) {
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Guardar la nueva ruta local accesible
                    producto.setImagenURL("/uploads/productos/" + fileName);
                } catch (Exception e) {
                    System.err.println("⚠️ Error al descargar la imagen: " + e.getMessage());
                    // En caso de error, mantiene la URL original sin interrumpir el proceso
                }
            }

            // 💾 Guardar producto en la base de datos
            Producto productoGuardado = productoService.guardarProducto(producto);
            return ResponseEntity.ok(productoGuardado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage());
        }
    }


    // Actualizar producto existente
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody Producto productoActualizado) {
        try {
            Optional<Producto> optProducto = productoService.buscarPorId(id);
            if (optProducto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado con ID: " + id);
            }

            Producto productoExistente = optProducto.get();

            // Actualizamos los campos básicos
            productoExistente.setNombreProducto(productoActualizado.getNombreProducto());
            productoExistente.setDescripcionProducto(productoActualizado.getDescripcionProducto());
            productoExistente.setPrecioVenta(productoActualizado.getPrecioVenta());
            productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
            productoExistente.setStockMaximo(productoActualizado.getStockMaximo());
            productoExistente.setCategoria(productoActualizado.getCategoria());
            productoExistente.setMarca(productoActualizado.getMarca());
            productoExistente.setProveedor(productoActualizado.getProveedor());
            productoExistente.setTipoItem(productoActualizado.getTipoItem());
            productoExistente.setEstadoProducto(productoActualizado.getEstadoProducto());

            // Descarga automática si se cambió la imagen
            if (productoActualizado.getImagenURL() != null
                    && productoActualizado.getImagenURL().startsWith("http")
                    && !productoActualizado.getImagenURL().equals(productoExistente.getImagenURL())) {
                try {
                    String rutaLocal = ImageDownloader.guardarImagenDesdeUrl(productoActualizado.getImagenURL(), "productos");
                    productoExistente.setImagenURL(rutaLocal);
                } catch (Exception e) {
                    System.err.println("⚠️ Error al descargar nueva imagen: " + e.getMessage());
                }
            }

            Producto actualizado = productoService.guardarProducto(productoExistente);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar producto: " + e.getMessage());
        }
    }


    // Eliminar producto
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        try {
            boolean eliminado = productoService.eliminarProducto(id);
            
            if (eliminado) {
                return ResponseEntity.ok("Producto eliminado exitosamente");
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar el producto: " + e.getMessage());
        }
    }

    // Generar reporte Excel
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> generarReporteExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRegistro,
            @RequestParam(required = false) Integer idTipoItem,
            @RequestParam(required = false) Integer idMarca,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idProveedor,
            @RequestParam(required = false) Integer idColor,
            @RequestParam(required = false) Integer idGenero,
            @RequestParam(required = false) Integer idMaterialSuela,
            @RequestParam(required = false) Integer idTalla,
            @RequestParam(required = false) Integer idMaterialPrincipal,
            @RequestParam(required = false) Integer idTipoPersona,
            @RequestParam(required = false) Integer idEstadoProducto) {

        try {
            // Obtener productos filtrados
            List<Producto> productos = productoService.filtrarProductos(
                    fechaRegistro, idTipoItem, idMarca, idCategoria, idProveedor,
                    idColor, idGenero, idMaterialSuela, idTalla, idMaterialPrincipal,
                    idTipoPersona, idEstadoProducto);

            // Crear el workbook de Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Productos");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Título del reporte
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Calzados D'JHONEY E.I.R.L");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

            // Subtítulo
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Informe de Productos");
            subtitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));

            // Fecha del reporte
            Row dateRow = sheet.createRow(2);
            Cell dateCell = dateRow.createCell(10);
            dateCell.setCellValue("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Espacio en blanco
            sheet.createRow(3);

            // Encabezados de la tabla
            Row headerRow = sheet.createRow(4);
            String[] headers = {"ID", "Serial", "Nombre", "Descripción", "Tipo Item", "Marca", "Categoría",
                                "Proveedor", "Color", "Género", "Talla", "Precio Venta", "Fecha Registro"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos de productos
            int rowNum = 5;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(producto.getIdProducto() != null ? producto.getIdProducto() : 0);
                row.createCell(1).setCellValue(producto.getSerialProducto() != null ? producto.getSerialProducto() : "");
                row.createCell(2).setCellValue(producto.getNombreProducto() != null ? producto.getNombreProducto() : "");
                row.createCell(3).setCellValue(producto.getDescripcionProducto() != null ? producto.getDescripcionProducto() : "");
                row.createCell(4).setCellValue(producto.getTipoItem() != null ? producto.getTipoItem().getDescripcion().toString() : "");
                row.createCell(5).setCellValue(producto.getMarca() != null ? producto.getMarca().getNombre() : "");
                row.createCell(6).setCellValue(producto.getCategoria() != null ? producto.getCategoria().getNombreCategoria() : "");
                row.createCell(7).setCellValue(producto.getProveedor() != null ? producto.getProveedor().getNombreProveedor() : "");
                row.createCell(8).setCellValue(producto.getColor() != null ? producto.getColor().getNombreColor() : "");
                row.createCell(9).setCellValue(producto.getGenero() != null ? producto.getGenero().getNombreGenero() : "");
                row.createCell(10).setCellValue(producto.getTalla() != null ? producto.getTalla().getValor() : "");
                row.createCell(11).setCellValue(producto.getPrecioVenta() != null ? producto.getPrecioVenta().doubleValue() : 0.0);
                row.createCell(12).setCellValue(producto.getFechaRegistro() != null ?
                    producto.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headersResponse.setContentDispositionFormData("attachment", "productos_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headersResponse)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Obtener catálogos para AJAX
    @GetMapping("/catalogos")
    @ResponseBody
    public ResponseEntity<?> obtenerCatalogos() {
        try {
            Map<String, Object> catalogos = new HashMap<>();
            catalogos.put("tiposItem", tipoItemService.obtenerTodos2());
            catalogos.put("marcas", marcaService.obtenerTodos2());
            catalogos.put("categorias", categoriaService.obtenerTodos2());
            catalogos.put("proveedores", proveedorService.obtenerTodos2());
            catalogos.put("colores", colorService.obtenerTodos2());
            catalogos.put("generos", generoService.obtenerTodos2());
            catalogos.put("materialesSuela", materialSuelaService.obtenerTodos2());
            catalogos.put("tallas", tallaService.obtenerTodos2());
            catalogos.put("materialesPrincipales", materialPrincipalService.obtenerTodos2());
            catalogos.put("tiposPersona", tipoPersonaService.obtenerTodos2());
            catalogos.put("estadosProducto", estadoProductoService.obtenerTodos2());
            
            return ResponseEntity.ok(catalogos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al cargar los catálogos: " + e.getMessage());
        }
    }

    // Buscar productos por nombre (para autocompletado)
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam String nombre) {
        try {
            List<Producto> productos = productoService.buscarProductosPorNombre(nombre);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Verificar disponibilidad de serial
    @GetMapping("/verificar-serial")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarSerial(
            @RequestParam String serial, 
            @RequestParam(required = false) Integer idExcluir) {
        
        try {
            boolean existe = (idExcluir != null) ? 
                productoService.existeProductoConSerial(serial, idExcluir) :
                productoService.existeProductoConSerial(serial);
                
            Map<String, Boolean> respuesta = new HashMap<>();
            respuesta.put("existe", existe);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Método auxiliar para cargar catálogos en el modelo
    private void cargarCatalogosEnModelo(Model model) {
        try {
            model.addAttribute("tiposItem", tipoItemService.obtenerTodos2());
            model.addAttribute("marcas", marcaService.obtenerTodos2());
            model.addAttribute("categorias", categoriaService.obtenerTodos2());
            model.addAttribute("proveedores", proveedorService.obtenerTodos2());
            model.addAttribute("colores", colorService.obtenerTodos2());
            model.addAttribute("generos", generoService.obtenerTodos2());
            model.addAttribute("materialesSuela", materialSuelaService.obtenerTodos2());
            model.addAttribute("tallas", tallaService.obtenerTodos2());
            model.addAttribute("materialesPrincipales", materialPrincipalService.obtenerTodos2());
            model.addAttribute("tiposPersona", tipoPersonaService.obtenerTodos2());
            model.addAttribute("estadosProducto", estadoProductoService.obtenerTodos2());
        } catch (Exception e) {
            // Log del error si es necesario
            System.err.println("Error cargando catálogos: " + e.getMessage());
        }
    }
}
