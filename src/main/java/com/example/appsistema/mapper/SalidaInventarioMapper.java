package com.example.appsistema.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.appsistema.dto.SalidaInventarioDTO;
import com.example.appsistema.dto.SalidaInventarioDetalleDTO;
import com.example.appsistema.model.SalidaInventario;
import com.example.appsistema.model.SalidaInventarioDetalle;

@Component
public class SalidaInventarioMapper {
    /**
     * Mapea SalidaInventario (Entidad) a SalidaInventarioDTO.
     */
    public SalidaInventarioDTO toDTO(SalidaInventario salida) {
        SalidaInventarioDTO dto = new SalidaInventarioDTO();
        dto.setIdSalida(salida.getIdSalida());
        dto.setNumeroSalida(salida.getNumeroSalida());
        dto.setFechaSalida(salida.getFechaSalida());
        // Se asume que getTipoSalida(), getTipoComprobante(), getEstadoSalida() devuelven enums
        dto.setTipoSalida(salida.getTipoSalida().name()); 
        dto.setTipoComprobante(salida.getTipoComprobante().name());
        dto.setSubtotal(salida.getSubtotal());
        dto.setImpuestos(salida.getImpuestos());
        dto.setDescuentos(salida.getDescuentos());
        dto.setMontoTotal(salida.getMontoTotal());
        dto.setEstadoSalida(salida.getEstadoSalida().name());
        
        // Mapeo de Cliente
        if (salida.getCliente() != null) {
            dto.setIdCliente(salida.getCliente().getIdCliente());
            dto.setNombreCliente(salida.getCliente().getNombreDisplay());
            if (salida.getCliente().getTipoDocumento() != null) {
                dto.setTipoDocumentoCliente(salida.getCliente().getTipoDocumento().name()); 
            } else {
                dto.setTipoDocumentoCliente(null);
            }
            dto.setNumeroDocumentoCliente(salida.getCliente().getNumeroDocumento());
            dto.setDireccionCliente(salida.getCliente().getDireccion());
        }
        
        // Mapeo de Almacén
        if (salida.getAlmacen() != null) {
            dto.setIdAlmacen(salida.getAlmacen().getIdAlmacen());
            dto.setNombreAlmacen(salida.getAlmacen().getNombreAlmacen());
            dto.setDireccionAlmacen(salida.getAlmacen().getUbicacion());
        }
        
        // Mapeo de Usuario
        if (salida.getUsuario() != null) {
            dto.setIdUsuario(salida.getUsuario().getIdUsuario());
            dto.setNombreUsuario(salida.getUsuario().getUsername());
        }
        
        // Mapear detalles recursivamente usando el método del mapper
        List<SalidaInventarioDetalleDTO> detallesDTO = (salida.getDetalles() != null) 
             ? salida.getDetalles().stream()
                .map(this::toDetalleDTO)
                .collect(Collectors.toList())
             : new ArrayList<>();
        dto.setDetalles(detallesDTO);
        
        return dto;
    }

    /**
     * Mapea SalidaInventarioDetalle (Entidad) a SalidaInventarioDetalleDTO.
     */
    public SalidaInventarioDetalleDTO toDetalleDTO(SalidaInventarioDetalle detalle) {
        SalidaInventarioDetalleDTO dto = new SalidaInventarioDetalleDTO();
        dto.setIdDetalleSalida(detalle.getIdDetalleSalida());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setDescuentoUnitario(detalle.getDescuentoUnitario());
        dto.setImpuestoUnitario(detalle.getImpuestoUnitario());
        dto.setSubtotalLinea(detalle.getSubtotalLinea());
        dto.setLote(detalle.getLote());
        
        // Mapeo de Producto
        if (detalle.getProducto() != null) {
            dto.setIdProducto(detalle.getProducto().getIdProducto());
            dto.setNombreItem(detalle.getProducto().getNombreProducto());
            dto.setTipoItem("Producto");
        }
        
        // Mapeo de Suministro
        if (detalle.getSuministro() != null) {
            dto.setIdSuministro(detalle.getSuministro().getIdSuministro());
            dto.setNombreItem(detalle.getSuministro().getNombreSuministro());
            dto.setTipoItem("Suministro");
        }
        
        // Mapeo de Almacén en el detalle
        if (detalle.getAlmacen() != null) {
            dto.setIdAlmacen(detalle.getAlmacen().getIdAlmacen());
            dto.setNombreAlmacen(detalle.getAlmacen().getNombreAlmacen());
        }
        
        return dto;
    }
}
