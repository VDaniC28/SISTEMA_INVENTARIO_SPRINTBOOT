package com.example.appsistema.controller;

import com.example.appsistema.config.TenantContext;
import com.example.appsistema.model.Empresa;
import com.example.appsistema.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model) {
        try {
            System.out.println("LoginController: Cargando página de login");
            
            // Guardar el contexto actual
            String originalTenant = TenantContext.getCurrentTenant();
            System.out.println("LoginController: Tenant actual: " + originalTenant);
            
            try {
                // Limpiar el contexto para usar la base de datos principal
                TenantContext.clear();
                System.out.println("LoginController: Usando base de datos principal para obtener empresas");
                
                // Obtener la lista de empresas desde la base de datos principal
                List<Empresa> empresas = empresaRepository.findAll();
                System.out.println("LoginController: Se encontraron " + empresas.size() + " empresas");
                
                // Debug: mostrar las empresas encontradas
                for (Empresa empresa : empresas) {
                    System.out.println("- Empresa ID: " + empresa.getIdEmpresa() + 
                                     ", Nombre: " + empresa.getNombre() + 
                                     ", Estado: " + empresa.getEstado());
                }
                
                // Filtrar solo empresas activas
                List<Empresa> empresasActivas = empresas.stream()
                    .filter(empresa -> empresa.getEstado() != null && empresa.getEstado())
                    .toList();
                
                System.out.println("LoginController: Empresas activas: " + empresasActivas.size());
                
                model.addAttribute("empresas", empresasActivas);
                
            } catch (Exception dbError) {
                System.err.println("LoginController: Error accediendo a la base de datos: " + dbError.getMessage());
                dbError.printStackTrace();
                
                // Fallback: usar datos mock
                System.out.println("LoginController: Usando empresas mock como fallback");
                List<Map<String, Object>> empresasMock = List.of(
                    Map.of("idEmpresa", 1, "nombre", "Empresa Principal"),
                    Map.of("idEmpresa", 2, "nombre", "Empresa Secundaria")
                );
                model.addAttribute("empresas", empresasMock);
                
            } finally {
                // Restaurar el contexto original
                if (originalTenant != null) {
                    TenantContext.setCurrentTenant(originalTenant);
                    System.out.println("LoginController: Restaurando tenant: " + originalTenant);
                } else {
                    TenantContext.clear();
                }
            }
            
            System.out.println("LoginController: Modelo configurado correctamente");
            return "login";
            
        } catch (Exception generalError) {
            System.err.println("LoginController: Error general: " + generalError.getMessage());
            generalError.printStackTrace();
            
            // En caso de error total, usar lista vacía
            model.addAttribute("empresas", List.of());
            return "login";
        }
    }
    
    // Método adicional para la página de inicio si lo necesitas
    @GetMapping("/")
    public String home() {
        return "redirect:/login"; // Redirige al login
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Página por defecto después del login
    }
}