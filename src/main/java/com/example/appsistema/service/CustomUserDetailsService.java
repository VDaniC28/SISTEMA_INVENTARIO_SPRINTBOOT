package com.example.appsistema.service;

import com.example.appsistema.config.TenantContext;
import com.example.appsistema.model.Usuario;
import com.example.appsistema.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("==================================================");
        System.out.println("=== CUSTOM USER DETAILS SERVICE - LOAD USER ===");
        System.out.println("==================================================");
        System.out.println("Usuario solicitado: " + username);
        
        String empresaId = TenantContext.getCurrentTenant();
        System.out.println("Empresa seleccionada (tenant): " + empresaId);

        if (empresaId == null) {
            throw new UsernameNotFoundException("No se ha seleccionado una empresa");
        }

        try {
            // Mostrar qué BD se está usando según el tenant
            String dbInfo = empresaId.equals("1") ? "sisinventario (BD principal)" : 
                           empresaId.equals("2") ? "sisinventario2 (BD empresa2)" : "BD desconocida";
            System.out.println("--- Buscando en: " + dbInfo + " (tenant: " + empresaId + ") ---");
            
            // Buscar directamente en la BD correspondiente según el tenant
            Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("❌ Usuario no encontrado en " + dbInfo + ": " + username);
                    return new UsernameNotFoundException("Usuario no encontrado en la empresa seleccionada");
                });

            System.out.println("✅ Usuario encontrado: " + usuario.getUsername());
            System.out.println("   - ID: " + usuario.getIdUsuario());
            System.out.println("   - Email: " + usuario.getEmail());
            System.out.println("   - Estado: " + usuario.getEstado());
            
            // Verificar si el usuario está activo
            if (!usuario.getEstado()) {
                System.err.println("❌ Usuario INACTIVO: " + username);
                throw new UsernameNotFoundException("Usuario inactivo: " + username);
            }

            // Verificar roles
            System.out.println("--- Verificando roles ---");
            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                System.out.println("Roles (" + usuario.getRoles().size() + "):");
                usuario.getRoles().forEach(rol -> 
                    System.out.println("   - " + rol.getNombre())
                );
            } else {
                System.out.println("⚠️ Usuario sin roles");
            }

            System.out.println("--- Creando CustomUserDetails ---");
            CustomUserDetails userDetails = new CustomUserDetails(usuario);
            
            System.out.println("✅ AUTENTICACIÓN EXITOSA para empresa: " + empresaId);
            System.out.println("==================================================");
            
            return userDetails;
            
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new UsernameNotFoundException("Error durante la autenticación: " + e.getMessage(), e);
        }
    }
}