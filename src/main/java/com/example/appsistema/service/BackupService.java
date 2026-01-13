package com.example.appsistema.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Asegúrate de tener esta importación

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async; // Importación clave: Permite ejecución en segundo plano
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.config.TenantContext;
import com.example.appsistema.model.Backup;
import com.example.appsistema.model.Backup.EstadoBackup;
import com.example.appsistema.model.Backup.TipoBackup;
import com.example.appsistema.model.Usuario;
import com.example.appsistema.repository.BackupRepository;
import com.example.appsistema.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Importación para logging


@Service
public class BackupService {
    
    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    @Autowired
    private BackupRepository backupRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Value("${backup.directory:backups}")
    private String backupDirectory;
    
    // Usar valores por defecto si no existen las propiedades
    @Value("${backup.db.username:root}")
    private String dbUsername;
    
    @Value("${backup.db.password:}")
    private String dbPassword;
    
    @Value("${backup.db.host:localhost}")
    private String dbHost;
    
    @Value("${backup.db.port:3306}")
    private String dbPort;
    
    private static final String MYSQLDUMP_PATH = "mysqldump";
    
    /**
     * Obtiene el nombre de la base de datos según el tenant actual
     */
    private String getDatabaseName() {
        String tenant = TenantContext.getCurrentTenant();
        if ("1".equals(tenant)) {
            return "sisinven";
        } else if ("2".equals(tenant)) {
            return "sisinventario2";
        }
        return "sisinven"; // Default
    }
    
    /**
     * Crea un backup completo de la base de datos.
     * ANOTACIÓN CLAVE: Se ejecuta en un hilo separado, liberando el hilo HTTP.
     * El método retorna void porque el controlador no necesita esperar.
     */
    @Async
    @Transactional
    public void crearBackup(Integer idUsuario, String observaciones) {
        // Inicializamos el objeto Backup. Si hay un fallo grave antes de la DB, este fallo será logged.
        Backup backup = new Backup();
        long startTime = System.currentTimeMillis();
        
        try {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
            
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }
            
            String dbName = getDatabaseName();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("backup_%s_%s.sql", dbName, timestamp);
            String filePath = backupPath.resolve(fileName).toString();
            
            // 1. Inicializar registro en la DB como EN_PROCESO
            backup.setNombreArchivo(fileName);
            backup.setRutaArchivo(filePath);
            backup.setTipoBackup(TipoBackup.COMPLETO);
            backup.setEstadoBackup(EstadoBackup.EN_PROCESO);
            backup.setFechaBackup(LocalDateTime.now());
            backup.setUsuario(usuario);
            backup.setObservaciones(observaciones);
            
            backup = backupRepository.save(backup);
            
            // 2. Construir y ejecutar el comando mysqldump
            List<String> command = new ArrayList<>();
            command.add(MYSQLDUMP_PATH);
            command.add("-h" + dbHost);
            command.add("-P" + dbPort);
            command.add("-u" + dbUsername);
            
            // Solo agregar password si no está vacío
            if (dbPassword != null && !dbPassword.isEmpty()) {
                command.add("-p" + dbPassword);
            }
            
            command.add("--routines");
            command.add("--triggers");
            command.add("--events");
            command.add("--single-transaction");
            command.add("--add-drop-database");
            command.add("--databases");
            command.add(dbName);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(new File(filePath));
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            // 3. Evaluar resultado y actualizar registro
            if (exitCode == 0) {
                File file = new File(filePath);
                backup.setTamanoArchivo(file.length());
                backup.setEstadoBackup(EstadoBackup.EXITOSO);
                backup.setNumeroTablas(contarTablas(filePath));
                
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                backup.setDuracionSegundos((int) duration);
                
                log.info("✅ Backup creado exitosamente: {}", fileName);
            } else {
                // Leer el error stream si falla
                String errorDetails;
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    errorDetails = errorReader.lines().collect(Collectors.joining("\n"));
                }
                
                backup.setEstadoBackup(EstadoBackup.FALLIDO);
                String fullObs = (observaciones != null ? observaciones + " | " : "") + 
                                 "Error en mysqldump. Código de salida: " + exitCode + 
                                 ". Detalles: " + (errorDetails.isEmpty() ? "No se detectaron detalles de error." : errorDetails);
                
                backup.setObservaciones(fullObs);
                log.error("❌ Error creando backup. Código: {}. Detalles: {}", exitCode, fullObs);
            }
            
        } catch (Exception e) {
            // Manejo de excepción de Java (IO, InterruptedException, Usuario no encontrado)
            backup.setEstadoBackup(EstadoBackup.FALLIDO);
            backup.setObservaciones(
                (observaciones != null ? observaciones + " | " : "") + 
                "Excepción de ejecución: " + e.getMessage()
            );
            log.error("❌ Excepción en el proceso de backup: {}", e.getMessage(), e);
        } finally {
            // 4. Guardar estado final (sea EXITOSO o FALLIDO)
            backupRepository.save(backup);
        }
    }
    
    /**
     * Restaura un backup
     */
    @Transactional
    public void restaurarBackup(Integer idBackup) throws Exception {
        Backup backup = backupRepository.findById(idBackup)
            .orElseThrow(() -> new Exception("Backup no encontrado"));
        
        if (backup.getEstadoBackup() != EstadoBackup.EXITOSO) {
            throw new Exception("No se puede restaurar un backup que no fue exitoso");
        }
        
        File file = new File(backup.getRutaArchivo());
        if (!file.exists()) {
            throw new Exception("Archivo de backup no encontrado: " + backup.getRutaArchivo());
        }
        
        String dbName = getDatabaseName();
        
        List<String> command = new ArrayList<>();
        command.add("mysql");
        command.add("-h" + dbHost);
        command.add("-P" + dbPort);
        command.add("-u" + dbUsername);
        
        if (dbPassword != null && !dbPassword.isEmpty()) {
            command.add("-p" + dbPassword);
        }
        
        command.add(dbName);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(file);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new Exception("Error al restaurar backup. Código de salida: " + exitCode);
        }
        
        log.info("✅ Backup restaurado exitosamente: {}", backup.getNombreArchivo());
    }
    
    @Transactional
    public void eliminarBackup(Integer idBackup) throws Exception {
        Backup backup = backupRepository.findById(idBackup)
            .orElseThrow(() -> new Exception("Backup no encontrado"));
        
        File file = new File(backup.getRutaArchivo());
        if (file.exists()) {
            if (!file.delete()) {
                throw new Exception("No se pudo eliminar el archivo de backup");
            }
        }
        
        backupRepository.delete(backup);
        log.info("✅ Backup eliminado: {}", backup.getNombreArchivo());
    }
    
    public byte[] descargarBackup(Integer idBackup) throws Exception {
        Backup backup = backupRepository.findById(idBackup)
            .orElseThrow(() -> new Exception("Backup no encontrado"));
        
        File file = new File(backup.getRutaArchivo());
        if (!file.exists()) {
            throw new Exception("Archivo de backup no encontrado");
        }
        
        return Files.readAllBytes(file.toPath());
    }

    @Transactional(readOnly = true)
    public List<Backup> listarBackups() {
        List<Backup> backups = backupRepository.findByOrderByFechaBackupDesc();
        // Forzar la carga del usuario para evitar lazy loading issues
        backups.forEach(backup -> {
            if (backup.getUsuario() != null) {
                backup.getUsuario().getUsername();
            }
        });
        return backups;
    }
    
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalBackups", backupRepository.count());
        stats.put("backupsExitosos", backupRepository.countBackupsExitosos());
        stats.put("backupsFallidos", backupRepository.countBackupsFallidos());
        
        Long tamanoTotal = backupRepository.getTamanoTotalBackups();
        stats.put("espacioUtilizado", tamanoTotal != null ? tamanoTotal : 0L);
        
        return stats;
    }
    
    private int contarTablas(String filePath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().toUpperCase().startsWith("CREATE TABLE")) {
                    count++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error contando tablas: " + e.getMessage());
        }
        return count;
    }
}
