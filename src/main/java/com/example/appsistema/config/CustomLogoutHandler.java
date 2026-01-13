package com.example.appsistema.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.example.appsistema.model.Auditoria;
import com.example.appsistema.model.Auditoria.TipoAccion;
import com.example.appsistema.repository.AuditoriaRepository;
import com.example.appsistema.service.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutHandler implements LogoutHandler {
    
     @Autowired
    private AuditoriaRepository auditoriaRepository;
    
    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            try {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Integer userId = userDetails.getUsuario().getIdUsuario();
                String username = userDetails.getUsername();
                
                // Obtener información de la petición
                String ip = obtenerIpReal(request);
                String navegador = obtenerNavegadorSimplificado(request);
                
                // Registrar en auditoría
                Auditoria auditoria = new Auditoria();
                auditoria.setIdUsuario(userId);
                auditoria.setTipoAccion(TipoAccion.LOGOUT);
                auditoria.setNombreTabla("usuarios");
                auditoria.setIdRegistroAfectado(userId);
                auditoria.setDescripcionAccion("Usuario " + username + " cerró sesión");
                auditoria.setIpAcceso(ip);
                auditoria.setNavegador(navegador);
                auditoria.setFechaAccion(LocalDateTime.now());
                
                auditoriaRepository.save(auditoria);
                
                System.out.println("✅ Logout auditado para usuario: " + username + " (ID: " + userId + ")");
            } catch (Exception e) {
                System.err.println("❌ Error en CustomLogoutHandler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Método auxiliar para obtener la IP real del cliente
     */
    private String obtenerIpReal(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
    
    /**
     * Método auxiliar para simplificar el User-Agent
     */
    private String obtenerNavegadorSimplificado(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }
        
        // Simplificar el user agent
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Opera") || userAgent.contains("OPR")) return "Opera";
        
        // Truncar si es muy largo
        return userAgent.length() > 100 
            ? userAgent.substring(0, 97) + "..." 
            : userAgent;
    }
}
