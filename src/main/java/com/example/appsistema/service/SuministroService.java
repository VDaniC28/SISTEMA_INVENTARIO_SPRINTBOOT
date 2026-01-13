package com.example.appsistema.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.appsistema.model.Proveedor;
import com.example.appsistema.model.Suministro;
import com.example.appsistema.repository.SuministroRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SuministroService {
    
    private final SuministroRepository suministroRepository;

    // ==========================================
    // OPERACIONES CRUD BÁSICAS
    // ==========================================

    /**
     * Obtiene todos los suministros
     */
    @Transactional(readOnly = true)
    public List<Suministro> findAll() {
        log.debug("Obteniendo todos los suministros");
        return suministroRepository.findAll();
    }

    /**
     * Obtiene todos los suministros con sus relaciones cargadas
     */
    @Transactional(readOnly = true)
    public List<Suministro> findAllWithRelations() {
        log.debug("Obteniendo todos los suministros con relaciones");
        return suministroRepository.findAllWithRelations();
    }

    /**
     * Obtiene suministros con paginación
     */
    @Transactional(readOnly = true)
    public Page<Suministro> findAllPageable(int page, int size, String sortBy, String sortDir) {
        log.debug("Obteniendo suministros paginados: página={}, tamaño={}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return suministroRepository.findAll(pageable);
    }

    /**
     * Busca suministro por ID
     */
    @Transactional(readOnly = true)
    public Optional<Suministro> findById(Integer id) {
        log.debug("Buscando suministro por ID: {}", id);
        return suministroRepository.findById(id);
    }

    /**
     * Busca suministro por ID con todas sus relaciones
     */
    @Transactional(readOnly = true)
    public Optional<Suministro> findByIdWithRelations(Integer id) {
        log.debug("Buscando suministro por ID con relaciones: {}", id);
        return suministroRepository.findByIdWithRelations(id);
    }

    /**
     * Guarda o actualiza un suministro
     */
    public Suministro save(Suministro suministro) {
        log.info("Guardando suministro: {}", suministro.getNombreSuministro());
        
        // Validaciones antes de guardar
        validateSuministro(suministro);
        
        // Establecer fecha de registro si es nuevo
        if (suministro.getIdSuministro() == null && suministro.getFechaRegistro() == null) {
            suministro.setFechaRegistro(LocalDate.now());
        }
        
        Suministro savedSuministro = suministroRepository.save(suministro);
        log.info("Suministro guardado exitosamente con ID: {}", savedSuministro.getIdSuministro());
        
        return savedSuministro;
    }

    /**
     * Elimina un suministro por ID
     */
    public void deleteById(Integer id) {
        log.info("Eliminando suministro con ID: {}", id);
        
        if (!suministroRepository.existsById(id)) {
            throw new RuntimeException("Suministro no encontrado con ID: " + id);
        }
        
        suministroRepository.deleteById(id);
        log.info("Suministro eliminado exitosamente");
    }

    // ==========================================
    // OPERACIONES DE BÚSQUEDA Y FILTRADO
    // ==========================================

    /**
     * Busca suministros con múltiples filtros
     */
    @Transactional(readOnly = true)
    public List<Suministro> findWithFilters(String nombreSuministro, Integer idTipoItem, 
                                           Integer idProveedor, LocalDate fechaInicio, 
                                           LocalDate fechaFin) {
        
        log.debug("Aplicando filtros - Nombre: {}, TipoItem: {}, Proveedor: {}", 
                 nombreSuministro, idTipoItem, idProveedor);
        
        return suministroRepository.findSuministrosWithFilters(
            StringUtils.hasText(nombreSuministro) ? nombreSuministro.trim() : null,
            idTipoItem,
            idProveedor,
            fechaInicio,
            fechaFin
        );
    }

    /**
     * Busca suministros con filtros y paginación
     */
    @Transactional(readOnly = true)
    public Page<Suministro> findWithFiltersPageable(String nombreSuministro, Integer idTipoItem,
                                                   Integer idProveedor,
                                                   LocalDate fechaInicio,
                                                   LocalDate fechaFin, int page, int size) {
        
        log.debug("Aplicando filtros paginados - página: {}, tamaño: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        
        return suministroRepository.findSuministrosWithFiltersPageable(
            StringUtils.hasText(nombreSuministro) ? nombreSuministro.trim() : null,
            idTipoItem,
            idProveedor,
            fechaInicio,
            fechaFin,
            pageable
        );
    }

    /**
     * Busca suministros por código o nombre
     */
    @Transactional(readOnly = true)
    public List<Suministro> findByCodigoOrNombre(String busqueda) {
        if (!StringUtils.hasText(busqueda)) {
            return List.of();
        }
        
        log.debug("Buscando suministros por código o nombre: {}", busqueda);
        return suministroRepository.findByCodigoOrNombre(busqueda.trim());
    }

    /**
     * Obtiene los últimos suministros registrados
     */
    @Transactional(readOnly = true)
    public List<Suministro> findLatestSuministros(int limit) {
        log.debug("Obteniendo últimos {} suministros", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return suministroRepository.findLatestSuministros(pageable);
    }

    // ==========================================
    // VALIDACIONES Y VERIFICACIONES
    // ==========================================

    /**
     * Valida si un código de suministro ya existe
     */
    @Transactional(readOnly = true)
    public boolean existsByCodigoSuministro(String codigoSuministro) {
        return StringUtils.hasText(codigoSuministro) && 
               suministroRepository.existsByCodigoSuministro(codigoSuministro.trim());
    }

    /**
     * Valida si un código existe excluyendo un ID específico (para actualizaciones)
     */
    @Transactional(readOnly = true)
    public boolean existsByCodigoSuministroAndNotId(String codigoSuministro, Integer idSuministro) {
        return StringUtils.hasText(codigoSuministro) && 
               suministroRepository.existsByCodigoSuministroAndIdSuministroNot(
                   codigoSuministro.trim(), idSuministro);
    }

    /**
     * Busca suministros duplicados por nombre y proveedor
     */
    @Transactional(readOnly = true)
    public List<Suministro> findDuplicates(String nombreSuministro, Integer idProveedor, Integer excludeId) {
        if (!StringUtils.hasText(nombreSuministro) || idProveedor == null) {
            return List.of();
        }
        
        return suministroRepository.findDuplicatesByNombreAndProveedor(
            nombreSuministro.trim(), idProveedor, excludeId);
    }

    // ==========================================
    // GENERACIÓN DE REPORTES EXCEL
    // ==========================================

    /**
     * Genera reporte Excel de suministros
     */
    @Transactional(readOnly = true)
    public byte[] generateExcelReport(List<Suministro> suministros) throws IOException {
        log.info("Generando reporte Excel con {} suministros", suministros.size());
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Reporte de Suministros");
            
            // Crear estilos
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            // Título principal
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Calzados D'JHONEY E.I.R.L");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            
            // Subtítulo
            Row subtitleRow = sheet.createRow(rowNum++);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Informe de Suministros");
            subtitleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));
            
            // Fecha de generación
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(8);
            dateCell.setCellValue("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(dataStyle);
            
            // Fila vacía
            rowNum++;
            
            // Encabezados
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "ID", "Código", "Nombre", "Tipo Item", "Categoría", 
                "Proveedor", "Color", "Precio Compra", "Stock Min", "Stock Max", "Fecha Registro"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            for (Suministro suministro : suministros) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(suministro.getIdSuministro());
                dataRow.createCell(1).setCellValue(suministro.getCodigoSuministro());
                dataRow.createCell(2).setCellValue(suministro.getNombreSuministro());
                dataRow.createCell(3).setCellValue(suministro.getTipoItemDescripcion());
                dataRow.createCell(5).setCellValue(suministro.getNombreProveedor());
                
                
                Cell precioCell = dataRow.createCell(7);
                precioCell.setCellValue(suministro.getPrecioCompra().doubleValue());
                precioCell.setCellStyle(currencyStyle);
                
                dataRow.createCell(8).setCellValue(suministro.getStockMinimo());
                dataRow.createCell(9).setCellValue(suministro.getStockMaximo());
                
                Cell fechaCell = dataRow.createCell(10);
                if (suministro.getFechaRegistro() != null) {
                    fechaCell.setCellValue(suministro.getFechaRegistro().toString());
                }
                fechaCell.setCellStyle(dateStyle);
                
                // Aplicar estilo de datos a todas las celdas
                for (int i = 0; i < 11; i++) {
                    if (i != 7 && i != 10) { // Excluir precio y fecha que ya tienen su estilo
                        dataRow.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            log.info("Reporte Excel generado exitosamente");
            return outputStream.toByteArray();
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==========================================

    /**
     * Valida los datos del suministro antes de guardar
     */
    private void validateSuministro(Suministro suministro) {
        if (suministro == null) {
            throw new IllegalArgumentException("El suministro no puede ser nulo");
        }
        
        // Validar código único
        if (suministro.getIdSuministro() == null) {
            // Nuevo suministro
            if (existsByCodigoSuministro(suministro.getCodigoSuministro())) {
                throw new RuntimeException("Ya existe un suministro con el código: " + 
                                         suministro.getCodigoSuministro());
            }
        } else {
            // Suministro existente
            if (existsByCodigoSuministroAndNotId(suministro.getCodigoSuministro(), 
                                               suministro.getIdSuministro())) {
                throw new RuntimeException("Ya existe otro suministro con el código: " + 
                                         suministro.getCodigoSuministro());
            }
        }
        
        // Validar stock
        if (suministro.getStockMinimo() != null && suministro.getStockMaximo() != null) {
            if (suministro.getStockMaximo() <= suministro.getStockMinimo()) {
                throw new RuntimeException("El stock máximo debe ser mayor al stock mínimo");
            }
        }
        
        // Validar precio
        if (suministro.getPrecioCompra() != null && 
            suministro.getPrecioCompra().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio de compra debe ser mayor a cero");
        }
    }

    // Métodos para crear estilos de Excel
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }
    
    public List<Suministro> obtenerTodos3() {
        return suministroRepository.findAllOrderByNombreSuministro();
    }

    @Transactional(readOnly = true)
    public List<Suministro> obtenerPorProveedor(Proveedor proveedor) {
        log.debug("Obteniendo suministros por proveedor con ID: {}", proveedor.getIdProveedor());
        return suministroRepository.findByProveedor(proveedor);
    }
}
