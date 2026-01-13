package com.example.appsistema.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        System.out.println("=== TENANT AUTHENTICATION FILTER ===");
        System.out.println("Método: " + request.getMethod());
        System.out.println("URI: " + request.getServletPath());
        System.out.println("Query String: " + request.getQueryString());
        
        try {
            // Solo procesar en requests de login POST
            if ("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath())) {
                System.out.println(">>> PROCESANDO LOGIN POST <<<");
                
                String empresaId = request.getParameter("empresaId");
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                
                System.out.println("Parámetros recibidos:");
                System.out.println("- empresaId: " + empresaId);
                System.out.println("- username: " + username);
                System.out.println("- password: " + (password != null ? "***" : "null"));
                
                if (empresaId != null && !empresaId.trim().isEmpty()) {
                    // Configurar el contexto del tenant antes de la autenticación
                    TenantContext.setCurrentTenant(empresaId);
                    System.out.println("✅ Tenant configurado exitosamente: " + empresaId);
                } else {
                    System.out.println("❌ ERROR: No se encontró empresaId o está vacío");
                    // Si no hay empresaId, redirigir al login con error
                    response.sendRedirect("/login?error=empresa");
                    return;
                }
            } else {
                // Para otras requests, usar el tenant de la sesión si existe
                String sessionTenant = (String) request.getSession().getAttribute("empresaId");
                if (sessionTenant != null) {
                    TenantContext.setCurrentTenant(sessionTenant);
                    System.out.println("Usando tenant de sesión: " + sessionTenant);
                }
            }
            
            System.out.println("Tenant actual antes de continuar: " + TenantContext.getCurrentTenant());
            System.out.println("=== CONTINUANDO CON FILTERCHAIN ===");
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            System.err.println("ERROR en TenantAuthenticationFilter: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // NO limpiar el contexto aquí para requests de login, 
            // ya que necesitamos que persista durante todo el proceso de autenticación
            if (!("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()))) {
                System.out.println("Limpiando contexto de tenant");
                TenantContext.clear();
            } else {
                System.out.println("Manteniendo contexto de tenant para login");
            }
        }
    }
}