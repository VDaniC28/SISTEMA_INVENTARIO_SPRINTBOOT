package com.example.appsistema.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.appsistema.config.TenantContext;
import com.example.appsistema.service.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuditContextHelper {
    
    /**
     * Obtiene el ID del usuario actual autenticado
     */
    public static Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof CustomUserDetails) {
                
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUsuario().getIdUsuario();
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo usuario actual: " + e.getMessage());
        }
        
        return null; // Usuario anónimo o error
    }
    
    /**
     * Obtiene la dirección IP del cliente
     */
    public static String getCurrentUserIp() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Intentar obtener IP real (considerando proxies)
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
                
                return ip;
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo IP: " + e.getMessage());
        }
        
        return "unknown";
    }
    
    /**
     * Obtiene el navegador del usuario
     */
    public static String getCurrentUserBrowser() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userAgent = request.getHeader("User-Agent");
                
                if (userAgent != null && !userAgent.isEmpty()) {
                    // Simplificar el user agent
                    if (userAgent.contains("Edge")) return "Edge";
                    if (userAgent.contains("Chrome")) return "Chrome";
                    if (userAgent.contains("Firefox")) return "Firefox";
                    if (userAgent.contains("Safari")) return "Safari";
                    if (userAgent.contains("Opera")) return "Opera";
                    
                    // Truncar si es muy largo
                    return userAgent.length() > 100 
                        ? userAgent.substring(0, 97) + "..." 
                        : userAgent;
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo navegador: " + e.getMessage());
        }
        
        return "unknown";
    }
    
    /**
     * Obtiene el tenant (empresa) actual
     */
    public static String getCurrentTenant() {
        return TenantContext.getCurrentTenant();
    }
}
