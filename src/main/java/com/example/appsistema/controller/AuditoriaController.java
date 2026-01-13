package com.example.appsistema.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.model.Auditoria;
import com.example.appsistema.model.Auditoria.TipoAccion;
import com.example.appsistema.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
     
    private final AuditoriaService auditoriaService;
    
    /**
     * Mostrar vista principal de auditoría (bitácora)
     */
    @GetMapping
    public String mostrarVistaAuditoria(Model model) {
        // Cargar datos iniciales para filtros
        model.addAttribute("tiposAccion", TipoAccion.values());
        model.addAttribute("tablas", auditoriaService.obtenerTablasUnicas());
        
        // Cargar primera página de auditorías
        Page<Auditoria> auditorias = auditoriaService.listarTodas(0, 20, "fechaAccion", "desc");
        model.addAttribute("auditorias", auditorias);
        
        return "admin/vistaAuditoria";
    }
    
    /**
     * API REST para obtener auditorías con paginación
     */
    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<Page<Auditoria>> listarAuditorias(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "fechaAccion") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<Auditoria> auditorias = auditoriaService.listarTodas(page, size, sortBy, direction);
        return ResponseEntity.ok(auditorias);
    }
    
    /**
     * API REST para búsqueda avanzada con filtros
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<Page<Auditoria>> buscarAuditorias(
        @RequestParam(required = false) Integer idUsuario,
        @RequestParam(required = false) String tipoAccion,
        @RequestParam(required = false) String nombreTabla,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<Auditoria> auditorias = auditoriaService.busquedaAvanzada(
            idUsuario, tipoAccion, nombreTabla, fechaInicio, fechaFin, page, size
        );
        return ResponseEntity.ok(auditorias);
    }
    
    /**
     * API REST para obtener detalle de una auditoría específica
     */
    @GetMapping("/detalle/{id}")
    @ResponseBody
    public ResponseEntity<Auditoria> obtenerDetalle(@PathVariable Integer id) {
        try {
            Auditoria auditoria = auditoriaService.obtenerPorId(id);
            return ResponseEntity.ok(auditoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * API REST para obtener estadísticas generales
     */
    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = auditoriaService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }
    
    /**
     * API REST para obtener lista de tablas con actividad
     */
    @GetMapping("/api/tablas")
    @ResponseBody
    public ResponseEntity<java.util.List<String>> obtenerTablasUnicas() {
        java.util.List<String> tablas = auditoriaService.obtenerTablasUnicas();
        return ResponseEntity.ok(tablas);
    }
    
    /**
     * Exportar auditorías a CSV (opcional)
     */
    @GetMapping("/exportar")
    public void exportarCSV(
        @RequestParam(required = false) Integer idUsuario,
        @RequestParam(required = false) String tipoAccion,
        @RequestParam(required = false) String nombreTabla,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
        jakarta.servlet.http.HttpServletResponse response
    ) throws Exception {
        
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"auditoria_" + 
            LocalDateTime.now().toString().replace(":", "-") + ".csv\"");
        
        // Obtener todas las auditorías que coincidan con los filtros
        Page<Auditoria> auditorias = auditoriaService.busquedaAvanzada(
            idUsuario, tipoAccion, nombreTabla, fechaInicio, fechaFin, 0, Integer.MAX_VALUE
        );
        
        java.io.PrintWriter writer = response.getWriter();
        
        // Encabezados CSV con BOM para Excel
        writer.write('\uFEFF'); // BOM para UTF-8
        writer.println("ID,Usuario ID,Tipo Acción,Tabla,Registro Afectado,Descripción,IP,Navegador,Fecha");
        
        // Datos
        for (Auditoria a : auditorias.getContent()) {
            writer.printf("%d,%d,%s,%s,%s,\"%s\",%s,%s,%s%n",
                a.getIdAuditoria(),
                a.getIdUsuario(),
                a.getTipoAccion(),
                a.getNombreTabla(),
                a.getIdRegistroAfectado() != null ? a.getIdRegistroAfectado() : "",
                a.getDescripcionAccion() != null ? a.getDescripcionAccion().replace("\"", "\"\"") : "",
                a.getIpAcceso() != null ? a.getIpAcceso() : "",
                a.getNavegador() != null ? a.getNavegador() : "",
                a.getFechaAccion()
            );
        }
        
        writer.flush();
    }
}
