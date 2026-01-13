package com.example.appsistema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.appsistema.dto.IndicadorStockDTO;
import com.example.appsistema.service.EmpresaService;
import com.example.appsistema.service.IndicadorStockService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
@Autowired
    private EmpresaService empresaService; // 1. Inyectar el servicio
    @Autowired
    private IndicadorStockService indicadorStockService;

    @GetMapping("/vistaAdmin")
    public String vistaAdmin(HttpSession session, Model model) {
        
        // --- Lógica para obtener el Nombre de la Empresa y guardarlo en la Sesión ---
        String nombreEmpresa = (String) session.getAttribute("nombreEmpresa");
        
        // Solo ejecuta esta lógica si el nombre aún no está en la sesión (primera visita post-login)
        if (nombreEmpresa == null || nombreEmpresa.isEmpty() || nombreEmpresa.equals("Empresa Actual")) { 
            
            Integer idEmpresa = null;
            
            // Reutiliza tu lógica de GuiaRemisionController para obtener el ID
            idEmpresa = (Integer) session.getAttribute("idEmpresaActual");
            if (idEmpresa == null) {
                String empresaIdStr = (String) session.getAttribute("empresaId");
                if (empresaIdStr != null) {
                    try {
                        idEmpresa = Integer.parseInt(empresaIdStr);
                    } catch (NumberFormatException ignored) { }
                }
            }
            
            if (idEmpresa != null) {
                // 2. Obtener el nombre de la empresa usando el ID
                nombreEmpresa = empresaService.getNombreEmpresaPorId(idEmpresa);
                
                // 3. Guardar el nombre real en la sesión para todas las vistas futuras
                session.setAttribute("nombreEmpresa", nombreEmpresa);
                System.out.println("Nombre de empresa REAL guardado en sesión por AdminController: " + nombreEmpresa);
            } else {
                nombreEmpresa = "Empresa No Seleccionada";
            }
        }
        
        // Opcional: Agregar al modelo de esta vista también, aunque GuiaRemisionController ya lo maneja.
        model.addAttribute("nombreEmpresa", nombreEmpresa);
        // --- Obtener indicadores de stock ---
        try {
            IndicadorStockDTO indicadores = indicadorStockService.obtenerIndicadoresStock();
            model.addAttribute("indicadores", indicadores);
        } catch (Exception e) {
            System.err.println("Error al obtener indicadores de stock: " + e.getMessage());
            e.printStackTrace();
            // Crear indicadores vacíos en caso de error
            model.addAttribute("indicadores", new IndicadorStockDTO());
        }


        return "admin/vistaAdmin";
    }
}
