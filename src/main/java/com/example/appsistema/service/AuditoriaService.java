package com.example.appsistema.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.model.Auditoria;
import com.example.appsistema.model.Auditoria.TipoAccion;
import com.example.appsistema.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService {
    
    private final AuditoriaRepository auditoriaRepository;
    
    // Listar todas las auditorías con paginación
    public Page<Auditoria> listarTodas(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return auditoriaRepository.findAll(pageable);
    }
    
    // Búsqueda avanzada
    public Page<Auditoria> busquedaAvanzada(
        Integer idUsuario,
        String tipoAccion,
        String nombreTabla,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaAccion").descending());
        TipoAccion tipo = tipoAccion != null && !tipoAccion.isEmpty() 
            ? TipoAccion.valueOf(tipoAccion) 
            : null;
        
        return auditoriaRepository.busquedaAvanzada(
            idUsuario, tipo, nombreTabla, fechaInicio, fechaFin, pageable
        );
    }
    
    // Obtener por ID
    public Auditoria obtenerPorId(Integer id) {
        return auditoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auditoría no encontrada"));
    }
    
    // Registrar nueva auditoría
    @Transactional
    public Auditoria registrarAuditoria(Auditoria auditoria) {
        return auditoriaRepository.save(auditoria);
    }
    
    // Método auxiliar para crear registro de auditoría
    @Transactional
    public void registrar(
        Integer idUsuario,
        TipoAccion tipoAccion,
        String nombreTabla,
        Integer idRegistro,
        String descripcion,
        String valorAnterior,
        String valorNuevo,
        String ip,
        String navegador
    ) {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdUsuario(idUsuario);
        auditoria.setTipoAccion(tipoAccion);
        auditoria.setNombreTabla(nombreTabla);
        auditoria.setIdRegistroAfectado(idRegistro);
        auditoria.setDescripcionAccion(descripcion);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNuevo(valorNuevo);
        auditoria.setIpAcceso(ip);
        auditoria.setNavegador(navegador);
        auditoria.setFechaAccion(LocalDateTime.now());
        
        auditoriaRepository.save(auditoria);
    }
    
    // Obtener estadísticas
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total de registros
        estadisticas.put("totalRegistros", auditoriaRepository.count());
        
        // Por tipo de acción
        List<Object[]> porTipo = auditoriaRepository.obtenerEstadisticasPorTipoAccion();
        Map<String, Long> tipoMap = new HashMap<>();
        porTipo.forEach(row -> tipoMap.put(row[0].toString(), (Long) row[1]));
        estadisticas.put("porTipoAccion", tipoMap);
        
        // Por tabla
        List<Object[]> porTabla = auditoriaRepository.obtenerEstadisticasPorTabla();
        Map<String, Long> tablaMap = new HashMap<>();
        porTabla.forEach(row -> tablaMap.put((String) row[0], (Long) row[1]));
        estadisticas.put("porTabla", tablaMap);
        
        return estadisticas;
    }
    
    // Obtener lista de tablas únicas
    public List<String> obtenerTablasUnicas() {
        return auditoriaRepository.findAll()
            .stream()
            .map(Auditoria::getNombreTabla)
            .distinct()
            .sorted()
            .toList();
    }
}
