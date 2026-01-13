package com.example.appsistema.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    
    private KPIDto kpis;
    private java.util.List<VentasMensualesDto> ventasMensuales;
    private java.util.List<TopProductoDto> topProductos;
    private java.util.List<VentasPorCategoriaDto> ventasPorCategoria;
    private java.util.List<RotacionInventarioDto> rotacionInventario;
    private java.util.List<ExcesoStockDto> excesoStock;
    private java.util.List<TopProveedorDto> topProveedores;
    private java.util.List<ComprasMensualesDto> comprasMensuales;
    private java.util.List<ClasificacionClienteDto> clasificacionClientes;
    private java.util.List<NuevosClientesDto> nuevosClientes;
}
