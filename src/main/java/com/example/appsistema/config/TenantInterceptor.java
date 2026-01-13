package com.example.appsistema.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Solo manejar requests que NO sean el proceso de login POST
        if (!("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()))) {
            
            // Primero intentar obtener el tenant de la sesión
            String empresaId = (String) request.getSession().getAttribute("empresaId");
            
            // Si no hay en sesión, intentar desde parámetros (para otros casos)
            if (empresaId == null) {
                empresaId = request.getParameter("empresaId");
            }
            
            if (empresaId != null) {
                TenantContext.setCurrentTenant(empresaId);
                System.out.println("TenantInterceptor: Configurando tenant " + empresaId + " para " + request.getServletPath());
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                          @Nullable ModelAndView modelAndView) throws Exception {
        // Solo limpiar el contexto si NO estamos en el proceso de login
        if (!("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()))) {
            TenantContext.clear();
        }
    }
}