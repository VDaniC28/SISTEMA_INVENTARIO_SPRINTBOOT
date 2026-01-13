package com.example.appsistema.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appsistema.model.Almacen;
import com.example.appsistema.model.InventarioAlmacen;
import com.example.appsistema.model.Producto;
import com.example.appsistema.model.Suministro;
import com.example.appsistema.service.AlmacenService;
import com.example.appsistema.service.InventarioAlmacenService;
import com.example.appsistema.service.ProductoService;
import com.example.appsistema.service.SuministroService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/almacenes")
public class AlmacenController {
    @Autowired
    private AlmacenService almacenService;
    
    @Autowired
    private InventarioAlmacenService inventarioService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private SuministroService suministroService;
    
    // ==========================================
    // VISTAS PRINCIPALES
    // ==========================================
    
    @GetMapping
    @Transactional(readOnly = true)
    public String vistaAlmacenes(Model model,
                            @RequestParam(defaultValue = "0") int pageAlmacen,
                            @RequestParam(defaultValue = "0") int pageInventario,
                            @RequestParam(defaultValue = "") String filtroNombre,
                            @RequestParam(required = false) Integer filtroAlmacen,
                            @RequestParam(required = false) Integer filtroProducto,
                            @RequestParam(required = false) Integer filtroSuministro,
                            @RequestParam(defaultValue = "") String filtroLote) {
        
        // Paginación para almacenes (5 por página)
        Pageable pageableAlmacen = PageRequest.of(pageAlmacen, 5, Sort.by("nombreAlmacen").ascending());
        Page<Almacen> almacenes = almacenService.buscarConFiltro(filtroNombre, pageableAlmacen);
        
        // Paginación para inventario (10 por página) - SIN SORT
        Pageable pageableInventario = PageRequest.of(pageInventario, 10);
        Page<InventarioAlmacen> inventarios = inventarioService.buscarConFiltros(
            filtroAlmacen, filtroProducto, filtroSuministro, filtroLote, pageableInventario);
        
        // DEBUG: Verificar datos cargados
        System.out.println("\n=== DEBUG INVENTARIO ALMACENES ===");
        System.out.println("Total elementos: " + inventarios.getTotalElements());
        System.out.println("Página actual: " + (pageInventario + 1) + " de " + inventarios.getTotalPages());
        System.out.println("Elementos mostrados: " + inventarios.getNumberOfElements());
        System.out.println("\nDetalle de items:");
        inventarios.getContent().forEach(inv -> {
            System.out.printf("  [ID:%d] Almacén: %s | Tipo: %s | Item: %s | Stock: %d%n",
                inv.getIdInventario(),
                inv.getAlmacen().getNombreAlmacen(),
                inv.getTipoItem(),
                inv.getNombreItem(),
                inv.getStock()
            );
        });
        System.out.println("==================================\n");
        
        // Datos para los formularios
        model.addAttribute("almacenes", almacenes);
        model.addAttribute("inventarios", inventarios);
        model.addAttribute("almacen", new Almacen());
        model.addAttribute("inventario", new InventarioAlmacen());
        
        // Listas para dropdowns
        model.addAttribute("listaAlmacenes", almacenService.obtenerTodos());
        model.addAttribute("listaProductos", productoService.obtenerTodos3());
        model.addAttribute("listaSuministros", suministroService.obtenerTodos3());
        model.addAttribute("tiposAlmacen", Almacen.TipoAlmacen.values());
        
        // Filtros actuales
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("filtroAlmacen", filtroAlmacen);
        model.addAttribute("filtroProducto", filtroProducto);
        model.addAttribute("filtroSuministro", filtroSuministro);
        model.addAttribute("filtroLote", filtroLote);
        
        return "admin/vistaAlmacen";
    }
    
    // ==========================================
    // OPERACIONES ALMACENES
    // ==========================================
    
    @PostMapping("/guardar")
    public String guardarAlmacen(@Valid @ModelAttribute Almacen almacen, 
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
                return "redirect:/admin/almacenes";
            }
            
            almacenService.guardar(almacen);
            redirectAttributes.addFlashAttribute("success", "Almacén guardado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el almacén: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }
    
    @PostMapping("/actualizar")
    public String actualizarAlmacen(@Valid @ModelAttribute Almacen almacen, 
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
                return "redirect:/admin/almacenes";
            }
            
            almacenService.actualizar(almacen);
            redirectAttributes.addFlashAttribute("success", "Almacén actualizado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el almacén: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }
    
    @PostMapping("/eliminar/{id}")
    public String eliminarAlmacen(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            almacenService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Almacén eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el almacén: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }
    
    // API REST para obtener datos del almacén para edición
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Almacen> obtenerAlmacenPorId(@PathVariable Integer id) {
        Optional<Almacen> almacen = almacenService.obtenerPorId(id);
        if (almacen.isPresent()) {
            return ResponseEntity.ok(almacen.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // ==========================================
    // OPERACIONES INVENTARIO ALMACENES
    // ==========================================
    @PostMapping("/inventario/guardar")
    public String guardarInventario(@Valid @ModelAttribute InventarioAlmacen inventario, 
                                @RequestParam(required = false) String tipoItem,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder("Errores: ");
                result.getFieldErrors().forEach(error -> 
                    errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
                );
                redirectAttributes.addFlashAttribute("error", errorMsg.toString());
                return "redirect:/admin/almacenes";
            }
            
            // Validación del tipo de item
            if (tipoItem == null || tipoItem.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un tipo de item");
                return "redirect:/admin/almacenes";
            }
            
            // Preparar el objeto según el tipo seleccionado
            if ("PRODUCTO".equals(tipoItem)) {
                if (inventario.getProducto() == null || inventario.getProducto().getIdProducto() == null) {
                    redirectAttributes.addFlashAttribute("error", "Debe seleccionar un producto");
                    return "redirect:/admin/almacenes";
                }
                // Asegurar que suministro esté vacío
                inventario.setSuministro(null);
                
                // Cargar el producto completo desde la base de datos
                Producto producto = productoService.obtenerProductoPorId(inventario.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                inventario.setProducto(producto);
                
            } else if ("SUMINISTRO".equals(tipoItem)) {
                if (inventario.getSuministro() == null || inventario.getSuministro().getIdSuministro() == null) {
                    redirectAttributes.addFlashAttribute("error", "Debe seleccionar un suministro");
                    return "redirect:/admin/almacenes";
                }
                // Asegurar que producto esté vacío
                inventario.setProducto(null);
                
                // Cargar el suministro completo desde la base de datos
                Suministro suministro = suministroService.findById(inventario.getSuministro().getIdSuministro())
                    .orElseThrow(() -> new RuntimeException("Suministro no encontrado"));
                inventario.setSuministro(suministro);
            }
            
            // Las validaciones adicionales las maneja tu @PrePersist/@PreUpdate
            inventarioService.guardar(inventario);
            redirectAttributes.addFlashAttribute("success", "Inventario guardado exitosamente");
            
        } catch (IllegalStateException e) {
            // Errores de las validaciones de tu modelo
            redirectAttributes.addFlashAttribute("error", "Error de validación: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el inventario: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }

    @PostMapping("/inventario/actualizar")
    public String actualizarInventario(@Valid @ModelAttribute InventarioAlmacen inventario, 
                                    @RequestParam(required = false) String tipoItem,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder("Errores: ");
                result.getFieldErrors().forEach(error -> 
                    errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
                );
                redirectAttributes.addFlashAttribute("error", errorMsg.toString());
                return "redirect:/admin/almacenes";
            }
            
            // Misma lógica de validación que en guardar
            if (tipoItem == null || tipoItem.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un tipo de item");
                return "redirect:/admin/almacenes";
            }
            
            if ("PRODUCTO".equals(tipoItem)) {
                if (inventario.getProducto() == null || inventario.getProducto().getIdProducto() == null) {
                    redirectAttributes.addFlashAttribute("error", "Debe seleccionar un producto");
                    return "redirect:/admin/almacenes";
                }
                inventario.setSuministro(null);
                
                Producto producto = productoService.obtenerProductoPorId(inventario.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                inventario.setProducto(producto);
                
            } else if ("SUMINISTRO".equals(tipoItem)) {
                if (inventario.getSuministro() == null || inventario.getSuministro().getIdSuministro() == null) {
                    redirectAttributes.addFlashAttribute("error", "Debe seleccionar un suministro");
                    return "redirect:/admin/almacenes";
                }
                inventario.setProducto(null);
                
                Suministro suministro = suministroService.findById(inventario.getSuministro().getIdSuministro())
                    .orElseThrow(() -> new RuntimeException("Suministro no encontrado"));
                inventario.setSuministro(suministro);
            }
            
            inventarioService.actualizar(inventario);
            redirectAttributes.addFlashAttribute("success", "Inventario actualizado exitosamente");
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Error de validación: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el inventario: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }
    
    @PostMapping("/inventario/eliminar/{id}")
    public String eliminarInventario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            inventarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Inventario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el inventario: " + e.getMessage());
        }
        
        return "redirect:/admin/almacenes";
    }
    
    @GetMapping("/inventario/api/{id}")
    @ResponseBody
    public ResponseEntity<InventarioAlmacen> obtenerInventarioPorId(@PathVariable Integer id) {
        // LLAMADA CLAVE: Usamos el nuevo método que tiene el JOIN FETCH
        Optional<InventarioAlmacen> inventario = inventarioService.obtenerPorIdWithDetails(id); 
        
        if (inventario.isPresent()) {
            return ResponseEntity.ok(inventario.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // ==========================================
    // GENERACIÓN DE EXCEL
    // ==========================================
    
    @GetMapping("/inventario/excel")
    public ResponseEntity<byte[]> descargarExcelInventario(
            @RequestParam(required = false) Integer filtroAlmacen,
            @RequestParam(required = false) Integer filtroProducto,
            @RequestParam(required = false) Integer filtroSuministro,
            @RequestParam(required = false) String filtroLote) {
        
        try {
            // Obtener datos para el reporte
            List<InventarioAlmacen> inventarios = inventarioService.obtenerParaExcel(
                filtroAlmacen, filtroProducto, filtroSuministro, filtroLote);
            
            // Crear el workbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Inventario Almacenes");
            
            // Estilos
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 12);
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setColor(IndexedColors.WHITE.getIndex());
            totalStyle.setFont(totalFont);
            totalStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setBorderBottom(BorderStyle.THIN);
            totalStyle.setBorderTop(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            int rowNum = 0;
            
            // Título principal
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Calzados D'JHONEY E.I.R.L");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));
            
            // Subtítulo
            rowNum++;
            Row subtitleRow = sheet.createRow(rowNum++);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Informe de Inventario - Existencias");
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 8));
            
            // Fecha del reporte
            rowNum++;
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(8);
            dateCell.setCellValue("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(dateStyle);
            
            // Espacio
            rowNum++;
            
            // Encabezados de la tabla
            Row headerRow = sheet.createRow(rowNum++);
            String[] columnheaders = {"ID", "Almacén", "Tipo Item", "Nombre Item", "Modelo", 
                              "Lote", "Stock Total", "Stock Reservado", "Stock Disponible"};
            
            for (int i = 0; i < columnheaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnheaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Variables para calcular totales
            double totalStock = 0;
            double totalStockReservado = 0;
            double totalStockDisponible = 0;
            
            // Datos
            for (InventarioAlmacen inventario : inventarios) {
                Row dataRow = sheet.createRow(rowNum++);
                
                Cell cell0 = dataRow.createCell(0);
                cell0.setCellValue(inventario.getIdInventario());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = dataRow.createCell(1);
                cell1.setCellValue(inventario.getAlmacen().getNombreAlmacen());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = dataRow.createCell(2);
                cell2.setCellValue(inventario.getTipoItem());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = dataRow.createCell(3);
                cell3.setCellValue(inventario.getNombreItem());
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = dataRow.createCell(4);
                cell4.setCellValue(inventario.getCodigoItem());
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = dataRow.createCell(5);
                cell5.setCellValue(inventario.getLote() != null ? inventario.getLote() : "N/A");
                cell5.setCellStyle(dataStyle);
                
                // Stock Total
                Cell cell6 = dataRow.createCell(6);
                double stock = inventario.getStock();
                cell6.setCellValue(stock);
                cell6.setCellStyle(dataStyle);
                totalStock += stock;
                
                // Stock Reservado
                Cell cell7 = dataRow.createCell(7);
                double stockReservado = inventario.getStockReservado();
                cell7.setCellValue(stockReservado);
                cell7.setCellStyle(dataStyle);
                totalStockReservado += stockReservado;
                
                // Stock Disponible
                Cell cell8 = dataRow.createCell(8);
                double stockDisponible = inventario.getStockDisponible();
                cell8.setCellValue(stockDisponible);
                cell8.setCellStyle(dataStyle);
                totalStockDisponible += stockDisponible;
            }
            
            // Fila de totales
            rowNum++;
            Row totalRow = sheet.createRow(rowNum++);
            
            // Celda "TOTALES"
            Cell totalLabelCell = totalRow.createCell(5);
            totalLabelCell.setCellValue("TOTALES:");
            totalLabelCell.setCellStyle(totalStyle);
            
            // Total Stock
            Cell totalStockCell = totalRow.createCell(6);
            totalStockCell.setCellValue(totalStock);
            totalStockCell.setCellStyle(totalStyle);
            
            // Total Stock Reservado
            Cell totalReservadoCell = totalRow.createCell(7);
            totalReservadoCell.setCellValue(totalStockReservado);
            totalReservadoCell.setCellStyle(totalStyle);
            
            // Total Stock Disponible
            Cell totalDisponibleCell = totalRow.createCell(8);
            totalDisponibleCell.setCellValue(totalStockDisponible);
            totalDisponibleCell.setCellStyle(totalStyle);

            // Ajustar ancho de columnas
            for (int i = 0; i < columnheaders.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Preparar respuesta
            String filename = "inventario_almacenes_" + 
                            LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
