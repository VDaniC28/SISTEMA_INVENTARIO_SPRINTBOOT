package com.example.appsistema.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/static/**", 
                    "/webjars/**",
                    "/uploads/**",
                    "/login" // Excluir el login para evitar conflictos
                );
    }

     @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🧩 Permite acceder a las imágenes descargadas (ej: /uploads/productos/imagen.jpg)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // 🧩 Mantiene compatibilidad con recursos estáticos tradicionales
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}