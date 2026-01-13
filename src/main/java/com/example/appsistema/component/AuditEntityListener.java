package com.example.appsistema.component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.appsistema.model.Auditoria;
import com.example.appsistema.model.Auditoria.TipoAccion;
import com.example.appsistema.repository.AuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

@Component
public class AuditEntityListener {
    
     private static AuditoriaRepository auditoriaRepository;
    private static ObjectMapper objectMapper;
    
    // ThreadLocal para almacenar el estado anterior antes de UPDATE y DELETE
    private static final ThreadLocal<String> estadoAnteriorThread = new ThreadLocal<>();
    
    @Autowired
    public void setAuditoriaRepository(AuditoriaRepository repository) {
        AuditEntityListener.auditoriaRepository = repository;
    }
    
    @Autowired
    public void setObjectMapper(ObjectMapper mapper) {
        AuditEntityListener.objectMapper = mapper;
    }
    
    /**
     * Se ejecuta DESPUÉS de insertar una nueva entidad
     */
    @PostPersist
    public void afterInsert(Object entity) {
        try {
            registrarAuditoria(
                entity,
                TipoAccion.INSERT,
                null, // No hay valor anterior
                entityToJson(entity), // Valor nuevo
                "Registro creado"
            );
        } catch (Exception e) {
            System.err.println("Error en auditoría POST INSERT: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Se ejecuta ANTES de actualizar una entidad (captura estado anterior)
     */
    @PreUpdate
    public void beforeUpdate(Object entity) {
        try {
            // Guardar el estado anterior en ThreadLocal
            String estadoAnterior = entityToJson(entity);
            estadoAnteriorThread.set(estadoAnterior);
        } catch (Exception e) {
            System.err.println("Error capturando estado anterior: " + e.getMessage());
        }
    }
    
    /**
     * Se ejecuta DESPUÉS de actualizar una entidad
     */
    @PostUpdate
    public void afterUpdate(Object entity) {
        try {
            String estadoAnterior = estadoAnteriorThread.get();
            String estadoNuevo = entityToJson(entity);
            
            registrarAuditoria(
                entity,
                TipoAccion.UPDATE,
                estadoAnterior,
                estadoNuevo,
                "Registro actualizado"
            );
        } catch (Exception e) {
            System.err.println("Error en auditoría POST UPDATE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            estadoAnteriorThread.remove();
        }
    }
    
    /**
     * Se ejecuta ANTES de eliminar una entidad (captura estado)
     */
    @PreRemove
    public void beforeDelete(Object entity) {
        try {
            // Guardar el estado antes de eliminar
            String estadoAnterior = entityToJson(entity);
            estadoAnteriorThread.set(estadoAnterior);
        } catch (Exception e) {
            System.err.println("Error capturando estado antes de eliminar: " + e.getMessage());
        }
    }
    
    /**
     * Se ejecuta DESPUÉS de eliminar una entidad
     */
    @PostRemove
    public void afterDelete(Object entity) {
        try {
            String estadoAnterior = estadoAnteriorThread.get();
            
            registrarAuditoria(
                entity,
                TipoAccion.DELETE,
                estadoAnterior,
                null, // Ya no existe valor nuevo
                "Registro eliminado"
            );
        } catch (Exception e) {
            System.err.println("Error en auditoría POST DELETE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            estadoAnteriorThread.remove();
        }
    }
    
    /**
     * Registra la auditoría en la base de datos
     */
    private void registrarAuditoria(
        Object entity, 
        TipoAccion tipoAccion, 
        String valorAnterior, 
        String valorNuevo,
        String descripcion
    ) {
        if (auditoriaRepository == null) {
            System.err.println("⚠️ AuditoriaRepository no inicializado");
            return;
        }
        
        try {
            Integer userId = AuditContextHelper.getCurrentUserId();
            
            // Si no hay usuario autenticado, usar ID 1 (sistema) como fallback
            if (userId == null) {
                userId = 1; // Usuario del sistema
                System.out.println("⚠️ Sin usuario autenticado, usando ID sistema: 1");
            }
            
            Auditoria auditoria = new Auditoria();
            auditoria.setIdUsuario(userId);
            auditoria.setTipoAccion(tipoAccion);
            auditoria.setNombreTabla(getNombreTabla(entity));
            auditoria.setIdRegistroAfectado(getIdEntidad(entity));
            auditoria.setDescripcionAccion(descripcion);
            auditoria.setValorAnterior(valorAnterior);
            auditoria.setValorNuevo(valorNuevo);
            auditoria.setIpAcceso(AuditContextHelper.getCurrentUserIp());
            auditoria.setNavegador(AuditContextHelper.getCurrentUserBrowser());
            auditoria.setFechaAccion(LocalDateTime.now());
            
            auditoriaRepository.save(auditoria);
            
            System.out.println("✅ Auditoría registrada: " + tipoAccion + " en " + 
                             auditoria.getNombreTabla() + " (ID: " + auditoria.getIdRegistroAfectado() + ")");
            
        } catch (Exception e) {
            System.err.println("❌ Error registrando auditoría: " + e.getMessage());
            e.printStackTrace();
            // NO lanzar la excepción para evitar que falle la transacción principal
        }
    }
    
    /**
     * Convierte la entidad a JSON (solo campos simples)
     */
    private String entityToJson(Object entity) {
        try {
            Map<String, Object> simpleMap = new HashMap<>();
            Class<?> clazz = entity.getClass();
            
            // Obtener todos los campos declarados
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(entity);
                
                // Solo incluir tipos simples (evitar relaciones)
                if (value != null && isSimpleType(value.getClass())) {
                    simpleMap.put(field.getName(), value);
                }
            }
            
            return objectMapper.writeValueAsString(simpleMap);
        } catch (Exception e) {
            System.err.println("Error convirtiendo entidad a JSON: " + e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Determina si un tipo es simple (no es una relación JPA)
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() 
            || type.equals(String.class)
            || type.equals(Integer.class)
            || type.equals(Long.class)
            || type.equals(Double.class)
            || type.equals(Float.class)
            || type.equals(Boolean.class)
            || type.equals(java.util.Date.class)
            || type.equals(java.time.LocalDate.class)
            || type.equals(java.time.LocalDateTime.class)
            || type.equals(java.math.BigDecimal.class)
            || type.isEnum();
    }
    
    /**
     * Obtiene el nombre de la tabla de la entidad
     */
    private String getNombreTabla(Object entity) {
        jakarta.persistence.Table tableAnnotation = 
            entity.getClass().getAnnotation(jakarta.persistence.Table.class);
        
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        
        // Si no tiene @Table, usar el nombre de la clase
        return entity.getClass().getSimpleName().toLowerCase();
    }
    
    /**
     * Obtiene el ID de la entidad usando reflexión
     */
    private Integer getIdEntidad(Object entity) {
        try {
            Class<?> clazz = entity.getClass();
            
            // Buscar el campo con @Id
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    field.setAccessible(true);
                    Object id = field.get(entity);
                    return id != null ? Integer.valueOf(id.toString()) : null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo ID de entidad: " + e.getMessage());
        }
        return null;
    }
}
