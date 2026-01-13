package com.example.appsistema.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class ReporteService {
    
    // Inyección por constructor (mejor práctica)
    private final EntityManager entityManager;

    
    public ReporteService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // =====================================================
    // MÉTODO PRINCIPAL: OBTENER 
    // =====================================================
    public DashboardDto obtenerDashboardCompleto() {
        DashboardDto dashboard = new DashboardDto();
        
        int anioActual = LocalDate.now().getYear();
        int anioAnterior = anioActual - 1;
        
        dashboard.setKpis(obtenerKPIs());
        dashboard.setVentasMensuales(obtenerVentasMensuales(anioActual, anioAnterior));
        dashboard.setTopProductos(obtenerTopProductos(10));
        dashboard.setVentasPorCategoria(obtenerVentasPorCategoria());
        dashboard.setRotacionInventario(obtenerRotacionInventario());
        dashboard.setExcesoStock(obtenerProductosExcesoStock());
        dashboard.setTopProveedores(obtenerTopProveedores(5));
        dashboard.setComprasMensuales(obtenerComprasMensuales(anioActual));
        dashboard.setClasificacionClientes(obtenerClasificacionClientes());
        dashboard.setNuevosClientes(obtenerNuevosClientesMensuales(anioActual));
        
        return dashboard;
    }

    // =====================================================
    // 1. KPIs (CARDS SUPERIORES)
    // =====================================================
    public KPIDto obtenerKPIs() {
        KPIDto kpi = new KPIDto();
        
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();
        
        // Lógica segura para el mes anterior
        LocalDate mesAnteriorDate = hoy.minusMonths(1);
        int mesAnterior = mesAnteriorDate.getMonthValue();
        int anioMesAnterior = mesAnteriorDate.getYear();

        String sqlVentasMes = """
            SELECT COALESCE(SUM(montoTotal), 0) 
            FROM salida_inventario 
            WHERE MONTH(fechaSalida) = :mes 
            AND YEAR(fechaSalida) = :anio 
            AND estadoSalida = 'Completada'
            AND tipoSalida = 'Venta'
        """;
        
        // 1. Obtener Ventas Mes Actual
        BigDecimal ventasMes = obtenerSingleResultBigDecimal(sqlVentasMes, mesActual, anioActual);
        
        // 2. Obtener Ventas Mes Anterior
        BigDecimal ventasMesAnterior = obtenerSingleResultBigDecimal(sqlVentasMes, mesAnterior, anioMesAnterior);
        
        // Calcular porcentaje de crecimiento
        Double porcentajeCrecimiento = 0.0;
        if (ventasMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeCrecimiento = ventasMes.subtract(ventasMesAnterior)
                .divide(ventasMesAnterior, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .doubleValue();
        }
        
        kpi.setVentasMes(ventasMes);
        kpi.setPorcentajeCrecimiento(porcentajeCrecimiento);
        
        // 3. Productos en stock
        String sqlStock = "SELECT COALESCE(SUM(stock), 0) FROM inventario_almacenes";
        Integer productosStock = obtenerSingleResultInt(sqlStock);
        kpi.setProductosStock(productosStock);
        
        // 4. Órdenes pendientes
        String sqlOrdenes = """
            SELECT COUNT(*) FROM ordenes_compras 
            WHERE estadoOrden IN ('Pendiente', 'Confirmada')
        """;
        Integer ordenesPendientes = obtenerSingleResultInt(sqlOrdenes);
        kpi.setOrdenesPendientes(ordenesPendientes);
        
        // 5. Clientes activos del mes
        String sqlClientes = """
            SELECT COUNT(DISTINCT idCliente) 
            FROM salida_inventario 
            WHERE MONTH(fechaSalida) = :mes 
            AND YEAR(fechaSalida) = :anio
        """;
        Integer clientesActivos = obtenerSingleResultInt(sqlClientes, mesActual, anioActual);
        kpi.setClientesActivos(clientesActivos);
        
        // 6. Tasa de cumplimiento de entregas
        String sqlCumplimiento = """
            SELECT 
                COALESCE(COUNT(CASE WHEN fechaEntregaReal <= fechaEntregaEsperada THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 0.0)
            FROM ordenes_compras 
            WHERE fechaEntregaReal IS NOT NULL 
            AND YEAR(fechaOrden) = :anio
        """;
        Double tasaCumplimiento = obtenerSingleResultDouble(sqlCumplimiento, anioActual);
        kpi.setTasaCumplimiento(tasaCumplimiento);
        
        return kpi;
    }

    // =====================================================
    // 2. EVOLUCIÓN DE VENTAS MENSUALES
    // =====================================================
    public List<VentasMensualesDto> obtenerVentasMensuales(int anioActual, int anioAnterior) {
        String sql = """
            SELECT 
                MONTH(fechaSalida) as mes,
                YEAR(fechaSalida) as anio,
                SUM(montoTotal) as total
            FROM salida_inventario
            WHERE YEAR(fechaSalida) IN (:anioActual, :anioAnterior)
            AND estadoSalida = 'Completada'
            AND tipoSalida = 'Venta'
            GROUP BY YEAR(fechaSalida), MONTH(fechaSalida)
            ORDER BY YEAR(fechaSalida), MONTH(fechaSalida)
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anioActual", anioActual)
            .setParameter("anioAnterior", anioAnterior);
        
        List<Object[]> results = getNativeResultList(query);
        
        Map<Integer, VentasMensualesDto> ventasMap = new HashMap<>();
        
        // Inicializar todos los meses
        for (int i = 1; i <= 12; i++) {
            VentasMensualesDto dto = new VentasMensualesDto();
            dto.setNumeroMes(i);
            dto.setMes(obtenerNombreMes(i));
            dto.setAnioActual(anioActual);
            dto.setAnioAnterior(anioAnterior);
            dto.setVentasAnioActual(BigDecimal.ZERO);
            dto.setVentasAnioAnterior(BigDecimal.ZERO);
            ventasMap.put(i, dto);
        }
        
        // Llenar con datos reales
        for (Object[] row : results) {
            int mes = ((Number) row[0]).intValue();
            int anio = ((Number) row[1]).intValue();
            BigDecimal total = (BigDecimal) row[2];
            
            VentasMensualesDto dto = ventasMap.get(mes);
            if (anio == anioActual) {
                dto.setVentasAnioActual(total);
            } else {
                dto.setVentasAnioAnterior(total);
            }
        }
        
        return new ArrayList<>(ventasMap.values());
    }

    // =====================================================
    // 3. TOP 10 PRODUCTOS MÁS VENDIDOS
    // =====================================================
    public List<TopProductoDto> obtenerTopProductos(int limite) {
        String sql = """
            SELECT 
                p.idProducto,
                p.nombreProducto,
                c.nombreCategoria,
                SUM(sd.cantidad) as cantidadVendida,
                SUM(sd.subtotalLinea) as montoTotal,
                p.imagenURL
            FROM salidas_inventario_detalle sd
            INNER JOIN productos p ON sd.idProducto = p.idProducto
            INNER JOIN categorias c ON p.idCategoria = c.idCategoria
            INNER JOIN salida_inventario s ON sd.idSalida = s.idSalida
            WHERE s.estadoSalida = 'Completada'
            AND s.tipoSalida = 'Venta'
            AND YEAR(s.fechaSalida) = :anio
            GROUP BY p.idProducto, p.nombreProducto, c.nombreCategoria, p.imagenURL
            ORDER BY cantidadVendida DESC
            LIMIT :limite
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", LocalDate.now().getYear())
            .setParameter("limite", limite);
            
        List<Object[]> results = getNativeResultList(query);
        
        return results.stream().map(row -> new TopProductoDto(
            ((Number) row[0]).intValue(),
            (String) row[1],
            (String) row[2],
            ((Number) row[3]).intValue(),
            (BigDecimal) row[4],
            (String) row[5]
        )).collect(Collectors.toList());
    }

    // =====================================================
    // 4. VENTAS POR CATEGORÍA
    // =====================================================
    public List<VentasPorCategoriaDto> obtenerVentasPorCategoria() {
        String sql = """
            SELECT 
                c.nombreCategoria,
                SUM(sd.cantidad) as cantidadVendida,
                SUM(sd.subtotalLinea) as montoTotal
            FROM salidas_inventario_detalle sd
            INNER JOIN productos p ON sd.idProducto = p.idProducto
            INNER JOIN categorias c ON p.idCategoria = c.idCategoria
            INNER JOIN salida_inventario s ON sd.idSalida = s.idSalida
            WHERE s.estadoSalida = 'Completada'
            AND s.tipoSalida = 'Venta'
            AND YEAR(s.fechaSalida) = :anio
            GROUP BY c.nombreCategoria
            ORDER BY montoTotal DESC
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", LocalDate.now().getYear());
        
        List<Object[]> results = getNativeResultList(query);
        
        BigDecimal totalGeneral = results.stream()
            .map(row -> (BigDecimal) row[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return results.stream().map(row -> {
            BigDecimal monto = (BigDecimal) row[2];
            Double porcentaje = totalGeneral.compareTo(BigDecimal.ZERO) > 0 
                ? monto.divide(totalGeneral, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue()
                : 0.0;
            
            return new VentasPorCategoriaDto(
                (String) row[0],
                ((Number) row[1]).intValue(),
                monto,
                porcentaje
            );
        }).collect(Collectors.toList());
    }

    // =====================================================
    // 5. ROTACIÓN DE INVENTARIO
    // =====================================================
    public List<RotacionInventarioDto> obtenerRotacionInventario() {
        String sql = """
            SELECT 
                c.nombreCategoria,
                COALESCE(SUM(ia.stock), 0) as stockActual,
                COALESCE(SUM(sd.cantidad), 0) as cantidadVendida
            FROM categorias c
            LEFT JOIN productos p ON c.idCategoria = p.idCategoria
            LEFT JOIN inventario_almacenes ia ON p.idProducto = ia.idProducto
            LEFT JOIN salidas_inventario_detalle sd ON p.idProducto = sd.idProducto
            LEFT JOIN salida_inventario s ON sd.idSalida = s.idSalida 
                AND s.estadoSalida = 'Completada' 
                AND YEAR(s.fechaSalida) = :anio
            GROUP BY c.nombreCategoria
            HAVING stockActual > 0
            ORDER BY cantidadVendida DESC
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", LocalDate.now().getYear());
            
        List<Object[]> results = getNativeResultList(query);
        
        return results.stream().map(row -> {
            Integer stockActual = ((Number) row[1]).intValue();
            Integer cantidadVendida = ((Number) row[2]).intValue();
            Double indiceRotacion = stockActual > 0 ? (double) cantidadVendida / stockActual : 0.0;
            
            String clasificacion;
            if (indiceRotacion >= 2.0) clasificacion = "Alta";
            else if (indiceRotacion >= 1.0) clasificacion = "Media";
            else clasificacion = "Baja";
            
            return new RotacionInventarioDto(
                (String) row[0],
                stockActual,
                cantidadVendida,
                indiceRotacion,
                clasificacion
            );
        }).collect(Collectors.toList());
    }

    // =====================================================
    // 6. PRODUCTOS CON EXCESO DE STOCK
    // =====================================================
    public List<ExcesoStockDto> obtenerProductosExcesoStock() {
        String sql = """
            SELECT 
                p.idProducto,
                p.nombreProducto,
                c.nombreCategoria,
                COALESCE(SUM(ia.stock), 0) as stockActual,
                p.stockMaximo,
                p.imagenURL
            FROM productos p
            INNER JOIN categorias c ON p.idCategoria = c.idCategoria
            LEFT JOIN inventario_almacenes ia ON p.idProducto = ia.idProducto
            GROUP BY p.idProducto, p.nombreProducto, c.nombreCategoria, p.stockMaximo, p.imagenURL
            HAVING stockActual > p.stockMaximo
            ORDER BY (stockActual - p.stockMaximo) DESC
            LIMIT 10
        """;
        
        List<Object[]> results = getNativeResultList(createNativeQuery(sql));
        
        return results.stream().map(row -> {
            Integer stockActual = ((Number) row[3]).intValue();
            Integer stockMaximo = ((Number) row[4]).intValue();
            Integer exceso = stockActual - stockMaximo;
            Double porcentajeExceso = stockMaximo > 0 
                ? (double) exceso / stockMaximo * 100 
                : 0.0;
            
            return new ExcesoStockDto(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                stockActual,
                stockMaximo,
                exceso,
                porcentajeExceso,
                (String) row[5]
            );
        }).collect(Collectors.toList());
    }

    // =====================================================
    // 7. TOP 5 PROVEEDORES
    // =====================================================
    public List<TopProveedorDto> obtenerTopProveedores(int limite) {
        String sql = """
            SELECT 
                pr.idProveedor,
                pr.nombreProveedor,
                pr.tipoDocumento,
                pr.numeroDocumento,
                COUNT(DISTINCT oc.idOrdenes) as cantidadOrdenes,
                COALESCE(SUM(oc.montoSubtotal + oc.impuestos - oc.descuentos), 0) as montoTotal,
                pr.email
            FROM proveedores pr
            LEFT JOIN ordenes_compras oc ON pr.idProveedor = oc.idProveedor
            WHERE YEAR(oc.fechaOrden) = :anio OR oc.fechaOrden IS NULL
            GROUP BY pr.idProveedor, pr.nombreProveedor, pr.tipoDocumento, pr.numeroDocumento, pr.email
            ORDER BY montoTotal DESC
            LIMIT :limite
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", LocalDate.now().getYear())
            .setParameter("limite", limite);
            
        List<Object[]> results = getNativeResultList(query);
        
        return results.stream().map(row -> new TopProveedorDto(
            ((Number) row[0]).intValue(),
            (String) row[1],
            (String) row[2],
            (String) row[3],
            ((Number) row[4]).intValue(),
            (BigDecimal) row[5],
            (String) row[6]
        )).collect(Collectors.toList());
    }

    // =====================================================
    // 8. EVOLUCIÓN DE COMPRAS MENSUALES
    // =====================================================
    public List<ComprasMensualesDto> obtenerComprasMensuales(int anio) {
        String sql = """
            SELECT 
                MONTH(fechaOrden) as mes,
                COUNT(*) as cantidadOrdenes,
                SUM(montoSubtotal + impuestos - descuentos) as montoTotal
            FROM ordenes_compras
            WHERE YEAR(fechaOrden) = :anio
            GROUP BY MONTH(fechaOrden)
            ORDER BY MONTH(fechaOrden)
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", anio);
        
        List<Object[]> results = getNativeResultList(query);
        
        Map<Integer, ComprasMensualesDto> comprasMap = new HashMap<>();
        
        // Inicializar todos los meses
        for (int i = 1; i <= 12; i++) {
            ComprasMensualesDto dto = new ComprasMensualesDto();
            dto.setMes(obtenerNombreMes(i));
            dto.setNumeroMes(i);
            dto.setCantidadOrdenes(0);
            dto.setMontoTotal(BigDecimal.ZERO);
            dto.setAnio(anio);
            comprasMap.put(i, dto);
        }
        
        // Llenar con datos reales
        for (Object[] row : results) {
            int mes = ((Number) row[0]).intValue();
            ComprasMensualesDto dto = comprasMap.get(mes);
            dto.setCantidadOrdenes(((Number) row[1]).intValue());
            dto.setMontoTotal((BigDecimal) row[2]);
        }
        
        return new ArrayList<>(comprasMap.values());
    }

    // =====================================================
    // 9. CLASIFICACIÓN DE CLIENTES
    // =====================================================
    public List<ClasificacionClienteDto> obtenerClasificacionClientes() {
        String sql = """
            SELECT 
                estadoValor,
                COUNT(*) as cantidad,
                COALESCE(SUM(s.montoTotal), 0) as montoTotal
            FROM clientes c
            LEFT JOIN salida_inventario s ON c.idCliente = s.idCliente 
                AND s.estadoSalida = 'Completada'
                AND YEAR(s.fechaSalida) = :anio
            WHERE estadoValor IN ('BUEN PAGADOR', 'MAL PAGADOR')
            GROUP BY estadoValor
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", LocalDate.now().getYear());
            
        List<Object[]> results = getNativeResultList(query);
        
        BigDecimal totalGeneral = results.stream()
            .map(row -> (BigDecimal) row[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return results.stream().map(row -> {
            BigDecimal monto = (BigDecimal) row[2];
            Double porcentaje = totalGeneral.compareTo(BigDecimal.ZERO) > 0 
                ? monto.divide(totalGeneral, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue()
                : 0.0;
            
            return new ClasificacionClienteDto(
                (String) row[0],
                ((Number) row[1]).intValue(),
                monto,
                porcentaje
            );
        }).collect(Collectors.toList());
    }

    // =====================================================
    // 10. NUEVOS CLIENTES POR MES
    // =====================================================
    public List<NuevosClientesDto> obtenerNuevosClientesMensuales(int anio) {
        String sql = """
            SELECT 
                MONTH(fechaRegistro) as mes,
                COUNT(*) as cantidadNuevos
            FROM clientes
            WHERE YEAR(fechaRegistro) = :anio
            GROUP BY MONTH(fechaRegistro)
            ORDER BY MONTH(fechaRegistro)
        """;
        
        Query query = createNativeQuery(sql)
            .setParameter("anio", anio);
            
        List<Object[]> results = getNativeResultList(query);
        
        Map<Integer, NuevosClientesDto> clientesMap = new HashMap<>();
        
        // Inicializar todos los meses
        for (int i = 1; i <= 12; i++) {
            NuevosClientesDto dto = new NuevosClientesDto();
            dto.setMes(obtenerNombreMes(i));
            dto.setNumeroMes(i);
            dto.setCantidadNuevos(0);
            dto.setAnio(anio);
            clientesMap.put(i, dto);
        }
        
        // Llenar con datos reales
        for (Object[] row : results) {
            int mes = ((Number) row[0]).intValue();
            NuevosClientesDto dto = clientesMap.get(mes);
            dto.setCantidadNuevos(((Number) row[1]).intValue());
        }
        
        return new ArrayList<>(clientesMap.values());
    }

    // =====================================================
    // MÉTODOS AUXILIARES (Para Type Safety y Results)
    // =====================================================
    
    /**
     * Auxiliar para simplemente crear la Native Query.
     */
    private Query createNativeQuery(String sql) {
        return entityManager.createNativeQuery(sql);
    }

    /**
     * Auxiliar para ejecutar la Native Query y obtener la List<Object[]>
     * eliminando la advertencia de Type Safety en el punto exacto de la conversión.
     */
    @SuppressWarnings("unchecked")
    private List<Object[]> getNativeResultList(Query query) {
        return query.getResultList();
    }
    
    /**
     * Auxiliar para obtener resultados únicos de tipo BigDecimal (para KPIs).
     */
    private BigDecimal obtenerSingleResultBigDecimal(String sql, Object... params) {
       Query query = createNativeQuery(sql);
        // Asignación de parámetros
        if (params.length >= 1) query.setParameter("mes", params[0]);
        if (params.length >= 2) query.setParameter("anio", params[1]);
        
        // MODIFICACIÓN CLAVE: Usar getResultList() y manejar el caso vacío/nulo
        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList(); 
        
        // 1. Verificar si la lista está vacía (NoResultException)
        // 2. Verificar si el resultado es nulo (si la consulta SQL devuelve NULL)
        if (results.isEmpty() || results.get(0) == null) {
            return BigDecimal.ZERO; 
        }
        
        return (BigDecimal) results.get(0);
    }

    /**
     * Auxiliar para obtener resultados únicos de tipo Integer (para KPIs).
     */
    private Integer obtenerSingleResultInt(String sql, Object... params) {
        Query query = createNativeQuery(sql);
        // Asignación de parámetros
        if (params.length >= 1) query.setParameter("mes", params[0]);
        if (params.length >= 2) query.setParameter("anio", params[1]);

        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList(); 
        
        if (results.isEmpty() || results.get(0) == null) {
            return 0; 
        }
        
        // Se convierte a Integer
        return ((Number) results.get(0)).intValue();
    }
    
    /**
     * Auxiliar para obtener resultados únicos de tipo Double (para KPIs).
     * El primer parámetro opcional se asume que es el año (:anio).
     */
    private Double obtenerSingleResultDouble(String sql, Object... params) {
       Query query = createNativeQuery(sql);
        // Asignación de parámetros
        if (params.length >= 1) query.setParameter("anio", params[0]);

        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList();
        
        if (results.isEmpty() || results.get(0) == null) {
            return 0.0; 
        }
        
        // Se convierte a Double
        return ((Number) results.get(0)).doubleValue();
    }

    /**
     * Auxiliar para obtener el nombre del mes en español (requiere java.util.Locale).
     */
    private String obtenerNombreMes(int numeroMes) {
        // Se usa 2024 solo como año de referencia, no afecta el nombre del mes
        return LocalDate.of(2024, numeroMes, 1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }
}