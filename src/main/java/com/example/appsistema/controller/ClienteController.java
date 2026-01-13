package com.example.appsistema.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.dto.SalidaInventarioDTO;
import com.example.appsistema.dto.SalidaInventarioDetalleDTO;
import com.example.appsistema.mapper.SalidaInventarioMapper;
import com.example.appsistema.model.Cliente;
import com.example.appsistema.model.SalidaInventario;
import com.example.appsistema.model.SalidaInventarioDetalle;
import com.example.appsistema.service.ClienteService;
import com.example.appsistema.service.SalidaInventarioService;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteController {
     @Autowired
    private ClienteService clienteService;

    
    @Autowired
    private SalidaInventarioService salidaInventarioService;

    @Autowired 
    private SalidaInventarioMapper salidaInventarioMapper;
    
    @GetMapping
    public String vistaClientes(Model model) {
        return "admin/vistaCliente";
    }
    
    @GetMapping("/listar")
    @ResponseBody
    public Page<Cliente> listarClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clienteService.listarClientesPaginados(page, size);
    }
    
    @GetMapping("/buscar/{id}")
    @ResponseBody
    public ResponseEntity<Cliente> buscarCliente(@PathVariable Integer id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarCliente(@RequestBody Cliente cliente) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que los campos requeridos no estén vacíos
            if (cliente.getTipoCliente() == null || 
                cliente.getTipoDocumento() == null || 
                cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().trim().isEmpty() ||
                cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
                
                response.put("success", false);
                response.put("message", "Todos los campos obligatorios deben ser completados");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar longitud del documento según tipo
            String numeroDoc = cliente.getNumeroDocumento().trim();
            if (cliente.getTipoDocumento() == Cliente.TipoDocumento.DNI && numeroDoc.length() != 8) {
                response.put("success", false);
                response.put("message", "El DNI debe tener 8 dígitos");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cliente.getTipoDocumento() == Cliente.TipoDocumento.RUC && numeroDoc.length() != 11) {
                response.put("success", false);
                response.put("message", "El RUC debe tener 11 dígitos");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cliente.getTipoDocumento() == Cliente.TipoDocumento.Pasaporte && 
                (numeroDoc.length() < 6 || numeroDoc.length() > 20)) {
                response.put("success", false);
                response.put("message", "El Pasaporte debe tener entre 6 y 20 caracteres");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar documento único solo si es un nuevo cliente o cambió el documento
            if (cliente.getIdCliente() == null) {
                // Es un nuevo cliente, verificar si el documento ya existe
                if (clienteService.existeDocumento(cliente.getNumeroDocumento(), null)) {
                    response.put("success", false);
                    response.put("message", "El número de documento ya está registrado");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                // Es una actualización, verificar si cambió el documento
                Cliente clienteExistente = clienteService.buscarPorId(cliente.getIdCliente()).orElse(null);
                
                if (clienteExistente != null && 
                    !clienteExistente.getNumeroDocumento().equals(cliente.getNumeroDocumento())) {
                    // El documento cambió, verificar si el nuevo ya existe
                    if (clienteService.existeDocumento(cliente.getNumeroDocumento(), cliente.getIdCliente())) {
                        response.put("success", false);
                        response.put("message", "El número de documento ya está registrado");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }
            
            // Validar estado valor si se proporcionó
            if (cliente.getEstadoValor() != null && !cliente.getEstadoValor().trim().isEmpty()) {
                String estadoValor = cliente.getEstadoValor().toUpperCase();
                if (!estadoValor.equals("BUEN PAGADOR") && !estadoValor.equals("MAL PAGADOR")) {
                    response.put("success", false);
                    response.put("message", "El estado valor debe ser 'BUEN PAGADOR' o 'MAL PAGADOR'");
                    return ResponseEntity.badRequest().body(response);
                }
                cliente.setEstadoValor(estadoValor);
            }
            
            // Limpiar espacios en blanco
            cliente.setNumeroDocumento(numeroDoc);
            cliente.setNombreCliente(cliente.getNombreCliente().trim());
            if (cliente.getNombreComercial() != null) {
                cliente.setNombreComercial(cliente.getNombreComercial().trim());
            }
            
            // Guardar cliente
            Cliente clienteGuardado = clienteService.guardarCliente(cliente);
            
            response.put("success", true);
            response.put("message", cliente.getIdCliente() == null ? 
                "Cliente registrado exitosamente" : "Cliente actualizado exitosamente");
            response.put("data", clienteGuardado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar si el cliente existe
            if (!clienteService.buscarPorId(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            // Aquí puedes agregar validación adicional
            // Por ejemplo, verificar si tiene ventas asociadas
            
            clienteService.eliminarCliente(id);
            
            response.put("success", true);
            response.put("message", "Cliente eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/buscar")
    @ResponseBody
    public Page<Cliente> buscarClientes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clienteService.buscarClientes(search, page, size);
    }

    // ============================================
    // ENDPOINTS PARA HISTORIAL DE COMPRAS
    // ============================================
    
    /**
     * Obtener historial de compras de un cliente con filtros
     */
    @GetMapping("/historial/{idCliente}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerHistorialCompras(
            @PathVariable Integer idCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String tipoComprobante,
            @RequestParam(required = false) String estadoSalida,
            @RequestParam(required = false) String tipoSalida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el cliente existe
            if (!clienteService.buscarPorId(idCliente).isPresent()) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            // Validar rango de fechas
            if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                response.put("success", false);
                response.put("message", "La fecha de inicio no puede ser mayor a la fecha fin");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Obtener historial con filtros
            Page<SalidaInventario> historial = salidaInventarioService.obtenerHistorialPorCliente(
                idCliente, 
                fechaInicio, 
                fechaFin, 
                tipoComprobante, 
                estadoSalida, 
                tipoSalida, 
                page, 
                size
            );

            Page<SalidaInventarioDTO> historialDtoPage = historial.map(salidaInventarioMapper::toDTO);
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", historialDtoPage.getContent());
            data.put("totalElements", historialDtoPage.getTotalElements());
            data.put("totalPages", historialDtoPage.getTotalPages());
            data.put("currentPage", historialDtoPage.getNumber());
            data.put("size", historialDtoPage.getSize());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "Historial obtenido correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener historial: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener detalle de una salida específica
     */
    @GetMapping("/historial/{idCliente}/detalle/{idSalida}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetalleSalida(
            @PathVariable Integer idCliente,
            @PathVariable Integer idSalida) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el cliente existe
            if (!clienteService.buscarPorId(idCliente).isPresent()) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            SalidaInventario salida = salidaInventarioService.buscarPorId(idSalida)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));
            
            // Verificar que la salida pertenece al cliente
            if (!salida.getCliente().getIdCliente().equals(idCliente)) {
                response.put("success", false);
                response.put("message", "Esta salida no pertenece al cliente especificado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Obtener detalles de la salida
            List<SalidaInventarioDetalle> detalles = salidaInventarioService.obtenerDetallesPorSalida(idSalida);
            
            // *************************************************************
            // RECOMENDACIÓN: Aquí también deberías mapear a DTOs para evitar el mismo problema
             SalidaInventarioDTO salidaDto = salidaInventarioMapper.toDTO(salida);
            List<SalidaInventarioDetalleDTO> detallesDto = detalles.stream()
                .map(salidaInventarioMapper::toDetalleDTO)
                .collect(Collectors.toList());
            // *************************************************************

            Map<String, Object> data = new HashMap<>();
            data.put("salida", salidaDto);      // Usar DTO
            data.put("detalles", detallesDto);  // Usar DTO
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "Detalle obtenido correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener detalle: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Exportar historial de compras (opcional)
     */
    @GetMapping("/historial/{idCliente}/exportar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportarHistorial(
            @PathVariable Integer idCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Implementar exportación a Excel o PDF
            response.put("success", true);
            response.put("message", "Exportación en desarrollo");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al exportar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener resumen de compras del cliente
     */
    @GetMapping("/historial/{idCliente}/resumen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerResumenCompras(@PathVariable Integer idCliente) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el cliente existe
            if (!clienteService.buscarPorId(idCliente).isPresent()) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalCompras", salidaInventarioService.contarComprasPorCliente(idCliente));
            resumen.put("montoTotal", salidaInventarioService.calcularMontoTotalPorCliente(idCliente));
            resumen.put("ultimaCompra", salidaInventarioService.obtenerUltimaCompraPorCliente(idCliente));
            resumen.put("comprasPendientes", salidaInventarioService.contarComprasPendientesPorCliente(idCliente));
            
            response.put("success", true);
            response.put("data", resumen);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener resumen: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
