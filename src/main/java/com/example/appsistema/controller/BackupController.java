package com.example.appsistema.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.dto.BackupDTO;
import com.example.appsistema.model.Backup;
import com.example.appsistema.service.BackupService;
import com.example.appsistema.service.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/admin/backups")
public class BackupController {
    
    private static final Logger log = LoggerFactory.getLogger(BackupController.class);
    
    @Autowired
    private BackupService backupService;
    
    /**
     * Muestra la vista principal de backups
     */
    @GetMapping
    public String vistaBackups(Model model) {
             try {
                 List<Backup> backups = backupService.listarBackups();
                 Map<String, Object> estadisticas = backupService.obtenerEstadisticas();
                 
                 // Convertir a DTO para evitar problemas de serialización
                 List<BackupDTO> backupsDTO = backups.stream()
                     .map(BackupDTO::new)
                     .collect(Collectors.toList());
                 
                 model.addAttribute("backups", backups); // Para la tabla Thymeleaf
                 model.addAttribute("backupsJSON", backupsDTO); // Para JavaScript
                 model.addAttribute("estadisticas", estadisticas);
                 
                 return "admin/vistaBackup";
             } catch (Exception e) {
                 log.error("Error al cargar la vista de backups: {}", e.getMessage(), e);
                 model.addAttribute("error", "Error al cargar backups: " + e.getMessage());
                 model.addAttribute("backups", new ArrayList<>());
                 model.addAttribute("backupsJSON", new ArrayList<>());
                 model.addAttribute("estadisticas", new HashMap<>());
                 return "admin/vistaBackup";
             }
    }
    
    /**
     * Crea un nuevo backup (se ejecuta de forma asíncrona)
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearBackup(
                @RequestParam(required = false) String observaciones,
                Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener ID del usuario autenticado
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer idUsuario = userDetails.getUsuario().getIdUsuario();
            
            // Llama al servicio. El método es asíncrono y devuelve VOID.
            // Esto evita el timeout, ya que la respuesta se envía inmediatamente.
            backupService.crearBackup(idUsuario, observaciones);
            
            log.info("Iniciada la solicitud de backup para el usuario: {}", idUsuario);
            
            response.put("success", true);
            // Mensaje clave: Informar al cliente que el proceso ha comenzado en segundo plano.
            response.put("message", "Proceso de backup iniciado exitosamente. Se ejecutará en segundo plano."); 
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al iniciar el proceso de backup: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al iniciar el proceso de backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Restaura un backup
     */
    @PostMapping("/restaurar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> restaurarBackup(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            backupService.restaurarBackup(id);
            
            response.put("success", true);
            response.put("message", "Backup restaurado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al restaurar backup {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al restaurar backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina un backup
     */
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarBackup(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            backupService.eliminarBackup(id);
            
            response.put("success", true);
            response.put("message", "Backup eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al eliminar backup {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al eliminar backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Descarga un backup
     */
    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargarBackup(@PathVariable Integer id) {
        try {
            // Se puede optimizar para usar backupService.descargarBackup(id) directamente para obtener el nombre
            // Pero mantengo la lógica de buscar el backup para obtener el nombre del archivo
            Backup backup = backupService.listarBackups().stream()
                .filter(b -> b.getIdBackup().equals(id))
                .findFirst()
                .orElseThrow(() -> new Exception("Backup no encontrado"));
            
            byte[] data = backupService.descargarBackup(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", backup.getNombreArchivo());
            
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error al descargar backup {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Obtiene estadísticas de backups
     */
    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> stats = backupService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de backup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
