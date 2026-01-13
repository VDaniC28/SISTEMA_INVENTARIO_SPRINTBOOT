package com.example.appsistema.config;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {
    
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(String tenantId) {
        System.out.println("TenantContext.setCurrentTenant: " + tenantId);
        currentTenant.set(tenantId);
    }
    
    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        System.out.println("TenantContext.getCurrentTenant: " + tenant);
        return tenant;
    }
    
    public static void clear() {
        String oldTenant = currentTenant.get();
        System.out.println("TenantContext.clear: Limpiando tenant " + oldTenant);
        currentTenant.remove();
    }
}