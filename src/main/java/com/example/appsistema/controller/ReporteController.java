package com.example.appsistema.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.dto.reportes.ClasificacionClienteDto;
import com.example.appsistema.dto.reportes.ComprasMensualesDto;
import com.example.appsistema.dto.reportes.DashboardDto;
import com.example.appsistema.dto.reportes.ExcesoStockDto;
import com.example.appsistema.dto.reportes.KPIDto;
import com.example.appsistema.dto.reportes.NuevosClientesDto;
import com.example.appsistema.dto.reportes.RotacionInventarioDto;
import com.example.appsistema.dto.reportes.TopProductoDto;
import com.example.appsistema.dto.reportes.TopProveedorDto;
import com.example.appsistema.dto.reportes.VentasMensualesDto;
import com.example.appsistema.dto.reportes.VentasPorCategoriaDto;
import com.example.appsistema.service.ReporteService;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {
    
     private final ReporteService reporteService;

    // Inyección por constructor (mejor práctica)
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // =====================================================
    // VISTA PRINCIPAL DEL DASHBOARD
    // =====================================================
    @GetMapping({"", "/dashboard"})
    public String mostrarDashboard() {
        return "admin/vistaDashboard"; // Retorna vistaDashboard.html
    }

    // =====================================================
    // ENDPOINT: OBTENER todo del dashboard
    // =====================================================
    @GetMapping("/api/dashboard-completo")
    @ResponseBody
    public ResponseEntity<DashboardDto> obtenerDashboardCompleto() {
        try {
            DashboardDto dashboard = reporteService.obtenerDashboardCompleto();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: OBTENER SOLO KPIs
    // =====================================================
    @GetMapping("/api/kpis")
    @ResponseBody
    public ResponseEntity<KPIDto> obtenerKPIs() {
        try {
            KPIDto kpis = reporteService.obtenerKPIs();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: EVOLUCIÓN DE VENTAS MENSUALES
    // =====================================================
    @GetMapping("/api/ventas-mensuales")
    @ResponseBody
    public ResponseEntity<List<VentasMensualesDto>> obtenerVentasMensuales(
            @RequestParam(required = false) Integer anioActual,
            @RequestParam(required = false) Integer anioAnterior) {
        try {
            int anioAct = (anioActual != null) ? anioActual : LocalDate.now().getYear();
            int anioAnt = (anioAnterior != null) ? anioAnterior : anioAct - 1;
            
            List<VentasMensualesDto> ventas = reporteService.obtenerVentasMensuales(anioAct, anioAnt);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: TOP PRODUCTOS MÁS VENDIDOS
    // =====================================================
    @GetMapping("/api/top-productos")
    @ResponseBody
    public ResponseEntity<List<TopProductoDto>> obtenerTopProductos(
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<TopProductoDto> topProductos = reporteService.obtenerTopProductos(limite);
            return ResponseEntity.ok(topProductos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: VENTAS POR CATEGORÍA
    // =====================================================
    @GetMapping("/api/ventas-por-categoria")
    @ResponseBody
    public ResponseEntity<List<VentasPorCategoriaDto>> obtenerVentasPorCategoria() {
        try {
            List<VentasPorCategoriaDto> ventasCategoria = reporteService.obtenerVentasPorCategoria();
            return ResponseEntity.ok(ventasCategoria);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: ROTACIÓN DE INVENTARIO
    // =====================================================
    @GetMapping("/api/rotacion-inventario")
    @ResponseBody
    public ResponseEntity<List<RotacionInventarioDto>> obtenerRotacionInventario() {
        try {
            List<RotacionInventarioDto> rotacion = reporteService.obtenerRotacionInventario();
            return ResponseEntity.ok(rotacion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: PRODUCTOS CON EXCESO DE STOCK
    // =====================================================
    @GetMapping("/api/exceso-stock")
    @ResponseBody
    public ResponseEntity<List<ExcesoStockDto>> obtenerProductosExcesoStock() {
        try {
            List<ExcesoStockDto> excesoStock = reporteService.obtenerProductosExcesoStock();
            return ResponseEntity.ok(excesoStock);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: TOP PROVEEDORES
    // =====================================================
    @GetMapping("/api/top-proveedores")
    @ResponseBody
    public ResponseEntity<List<TopProveedorDto>> obtenerTopProveedores(
            @RequestParam(defaultValue = "5") int limite) {
        try {
            List<TopProveedorDto> topProveedores = reporteService.obtenerTopProveedores(limite);
            return ResponseEntity.ok(topProveedores);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: EVOLUCIÓN DE COMPRAS MENSUALES
    // =====================================================
    @GetMapping("/api/compras-mensuales")
    @ResponseBody
    public ResponseEntity<List<ComprasMensualesDto>> obtenerComprasMensuales(
            @RequestParam(required = false) Integer anio) {
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();
            List<ComprasMensualesDto> compras = reporteService.obtenerComprasMensuales(anioConsulta);
            return ResponseEntity.ok(compras);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: CLASIFICACIÓN DE CLIENTES
    // =====================================================
    @GetMapping("/api/clasificacion-clientes")
    @ResponseBody
    public ResponseEntity<List<ClasificacionClienteDto>> obtenerClasificacionClientes() {
        try {
            List<ClasificacionClienteDto> clasificacion = reporteService.obtenerClasificacionClientes();
            return ResponseEntity.ok(clasificacion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // ENDPOINT: NUEVOS CLIENTES POR MES
    // =====================================================
    @GetMapping("/api/nuevos-clientes-mensuales")
    @ResponseBody
    public ResponseEntity<List<NuevosClientesDto>> obtenerNuevosClientesMensuales(
            @RequestParam(required = false) Integer anio) {
        try {
            int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();
            List<NuevosClientesDto> nuevosClientes = reporteService.obtenerNuevosClientesMensuales(anioConsulta);
            return ResponseEntity.ok(nuevosClientes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
