package com.example.appsistema.service;

import com.example.appsistema.config.TenantContext;
import com.example.appsistema.model.Empresa;
import com.example.appsistema.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Empresa> obtenerTodasLasEmpresas() {
        try {
            // Para obtener empresas, temporalmente configuramos el tenant principal (main)
            String originalTenant = TenantContext.getCurrentTenant();
            
            try {
                // Usar la base de datos principal para obtener la lista de empresas
                TenantContext.clear(); // Usar default DataSource (main)
                
                return empresaRepository.findAll();
                
            } finally {
                // Restaurar el tenant original si existía
                if (originalTenant != null) {
                    TenantContext.setCurrentTenant(originalTenant);
                } else {
                    TenantContext.clear();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo empresas: " + e.getMessage());
            // Retornar lista vacía en caso de error para evitar que falle la página
            return List.of();
        }
    }

    public Optional<Empresa> obtenerEmpresaPorId(Integer id) {
        try {
            String originalTenant = TenantContext.getCurrentTenant();
            
            try {
                TenantContext.clear(); // Usar default DataSource
                return empresaRepository.findById(id);
                
            } finally {
                if (originalTenant != null) {
                    TenantContext.setCurrentTenant(originalTenant);
                } else {
                    TenantContext.clear();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo empresa por ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // Método para crear empresas hardcodeadas si no tienes datos
    public List<Map<String, Object>> obtenerEmpresasMock() {
        return List.of(
            Map.of("idEmpresa", 1L, "nombre", "Empresa Principal"),
            Map.of("idEmpresa", 2L, "nombre", "Empresa Secundaria")
        );
    }

    public String getNombreEmpresaPorId(Integer idEmpresa) {
        // Reutilizamos el método existente que ya maneja el TenantContext (obtenerEmpresaPorId).
        Optional<Empresa> empresaOptional = obtenerEmpresaPorId(idEmpresa); 

        // Usamos el Optional para devolver el nombre o un valor por defecto.
        // Esto requiere que la entidad Empresa tenga un método getNombre().
        return empresaOptional
                .map(Empresa::getNombre)
                .orElse("Empresa no encontrada"); 
    }
}