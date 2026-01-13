package com.example.appsistema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.appsistema.model.GuiaRemision;

import com.example.appsistema.model.OrdenesCompra;

import com.example.appsistema.model.SalidaInventario;

import com.example.appsistema.service.GuiaRemisionService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders; // ⭐ Nueva Importación
import org.springframework.http.MediaType; // ⭐ Nueva Importación
import org.springframework.http.ResponseEntity; // ⭐ Nueva Importación
import com.example.appsistema.service.PdfGeneratorService;
import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/admin/guias-remision")
@RequiredArgsConstructor
public class GuiaRemisionController {
   @Autowired
    private GuiaRemisionService guiaService;

    @Autowired 
    private PdfGeneratorService pdfGeneratorService;

    // Mostrar vista principal con paginación
    @GetMapping
    public String mostrarVistaGuias(
            @RequestParam(defaultValue = "0") int pageOrdenes,
            @RequestParam(defaultValue = "0") int pageSalidas,
            @RequestParam(defaultValue = "0") int pageGuias,
            @RequestParam(defaultValue = "5") int sizeOrdenes,
            @RequestParam(defaultValue = "5") int sizeSalidas,
            @RequestParam(defaultValue = "10") int sizeGuias,
            @RequestParam(required = false) String search,
            HttpSession session,
            Model model) {
        
        // CORREGIDO: Obtener ID de empresa de la sesión (usa el nombre correcto)
        Integer idEmpresa = (Integer) session.getAttribute("idEmpresaActual");
        
        // Si no existe con ese nombre, intentar con el otro
        if (idEmpresa == null) {
            String empresaIdStr = (String) session.getAttribute("empresaId");
            if (empresaIdStr != null) {
                try {
                    idEmpresa = Integer.parseInt(empresaIdStr);
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir empresaId de sesión");
                }
            }
        }
        
        System.out.println("ID Empresa en sesión: " + idEmpresa); // Para debug
        
        if (idEmpresa == null) {
            return "redirect:/login";
        }

        // Obtener nombre de empresa para mostrar (si lo guardaste en sesión)
        String nombreEmpresa = (String) session.getAttribute("nombreEmpresa");
        if (nombreEmpresa == null) {
            nombreEmpresa = "Empresa Actual"; // Valor por defecto
        }

        // Obtener órdenes y salidas pendientes con paginación
        Page<OrdenesCompra> ordenesPage = guiaService.getOrdenesConfirmadasSinGuia(pageOrdenes, sizeOrdenes);
        Page<SalidaInventario> salidasPage = guiaService.getSalidasCompletadasSinGuia(pageSalidas, sizeSalidas);
        
        // Obtener guías con paginación y búsqueda
        Page<GuiaRemision> guiasPage;
        if (search != null && !search.trim().isEmpty()) {
            guiasPage = guiaService.buscarGuias(idEmpresa, search, pageGuias, sizeGuias);
        } else {
            guiasPage = guiaService.listarGuiasPorEmpresa(idEmpresa, pageGuias, sizeGuias);
        }

        model.addAttribute("nombreEmpresa", nombreEmpresa);
        model.addAttribute("ordenesPage", ordenesPage);
        model.addAttribute("salidasPage", salidasPage);
        model.addAttribute("guiasPage", guiasPage);
        model.addAttribute("search", search);

        // ⭐ AGREGAR ESTAS LÍNEAS - Parámetros de paginación ⭐
        model.addAttribute("pageOrdenes", pageOrdenes);
        model.addAttribute("pageSalidas", pageSalidas);
        model.addAttribute("pageGuias", pageGuias);
        model.addAttribute("sizeOrdenes", sizeOrdenes);
        model.addAttribute("sizeSalidas", sizeSalidas);
        model.addAttribute("sizeGuias", sizeGuias);

        return "admin/vistaGuia";
    }

    // Crear guía desde orden de compra
    @PostMapping("/crear-desde-orden")
    public String crearGuiaDesdeOrden(
            @RequestParam Integer idOrden,
            @ModelAttribute GuiaRemision guiaData,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Integer idEmpresa = (Integer) session.getAttribute("idEmpresaActual");
            if (idEmpresa == null) {
                String empresaIdStr = (String) session.getAttribute("empresaId");
                if (empresaIdStr != null) {
                    idEmpresa = Integer.parseInt(empresaIdStr);
                }
            }
            
            if (idEmpresa == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo identificar la empresa");
                return "redirect:/admin/guias-remision";
            }
            
            guiaService.crearGuiaDesdeOrdenCompra(idOrden, guiaData, idEmpresa);
            
            redirectAttributes.addFlashAttribute("success", 
                "Guía de remisión creada exitosamente desde la orden de compra");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear guía: " + e.getMessage());
            e.printStackTrace(); // Para debug
        }
        
        return "redirect:/admin/guias-remision";
    }

    // Crear guía desde salida de inventario
    @PostMapping("/crear-desde-salida")
    public String crearGuiaDesdeSalida(
            @RequestParam Integer idSalida,
            @ModelAttribute GuiaRemision guiaData,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Integer idEmpresa = (Integer) session.getAttribute("idEmpresaActual");
            if (idEmpresa == null) {
                String empresaIdStr = (String) session.getAttribute("empresaId");
                if (empresaIdStr != null) {
                    idEmpresa = Integer.parseInt(empresaIdStr);
                }
            }
            
            if (idEmpresa == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo identificar la empresa");
                return "redirect:/admin/guias-remision";
            }
            
            guiaService.crearGuiaDesdeSalidaInventario(idSalida, guiaData, idEmpresa);
            
            redirectAttributes.addFlashAttribute("success", 
                "Guía de remisión creada exitosamente desde la salida de inventario");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear guía: " + e.getMessage());
            e.printStackTrace(); // Para debug
        }
        
        return "redirect:/admin/guias-remision";
    }

    // Ver detalles de guía (AJAX)
    @GetMapping("/detalles/{id}")
    @ResponseBody
    // ⭐ CORREGIDO: El tipo de retorno debe ser GuiaRemision, no List<GuiaRemisionDetalle> ⭐
    public GuiaRemision verDetalleGuia(@PathVariable Integer id) {
        // Llama al nuevo método del servicio, que devuelve la GuiaRemision completa con sus relaciones cargadas.
        return guiaService.obtenerGuiaParaReporte(id);
    }
    // Anular guía
    @PostMapping("/anular/{id}")
    public String anularGuia(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            guiaService.anularGuia(id);
            redirectAttributes.addFlashAttribute("success", "Guía anulada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al anular guía: " + e.getMessage());
            e.printStackTrace(); // Para debug
        }
        
        return "redirect:/admin/guias-remision";
    }

    @GetMapping("/pdf/{idGuia}")
    public ResponseEntity<InputStreamResource> generarPdfGuia(@PathVariable Integer idGuia) {
        try {
            // Obtener la guía completa (con todos sus objetos relacionados)
            GuiaRemision guia = guiaService.obtenerGuiaParaReporte(idGuia); 
            
            if (guia == null) {
                return ResponseEntity.notFound().build();
            }

            // Generar el PDF usando el servicio
            ByteArrayInputStream pdfStream = pdfGeneratorService.generarPdfGuia(guia);

            // Configurar la respuesta HTTP para el archivo PDF
            HttpHeaders headers = new HttpHeaders();
            String numGuia = guia.getSerieGuia() + "-" + guia.getCorrelativoGuia();
            String filename = String.format("Guia_Remision_%s.pdf", numGuia);
            
            // Usar 'inline' para mostrarlo en el navegador o 'attachment' para descargarlo
            headers.add("Content-Disposition", "inline; filename=\"" + filename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna error 500 si hay un problema en la generación
            return ResponseEntity.internalServerError().build();
        }
    }
}
