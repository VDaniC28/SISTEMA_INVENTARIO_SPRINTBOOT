package com.example.appsistema.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appsistema.model.Proveedor;
import com.example.appsistema.service.ProveedorService;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {
    @Autowired
    private ProveedorService proveedorService;

    // Mostrar la vista principal de proveedores
    @GetMapping
    public String mostrarProveedores(Model model) {
        List<Proveedor> proveedores = proveedorService.obtenerTodosLosProveedores();
        model.addAttribute("proveedores", proveedores);
        return "admin/vistaProveedor";
    }

    // API para obtener todos los proveedores (AJAX)
    @GetMapping("/api/todos")
    @ResponseBody
    public List<Proveedor> obtenerTodosLosProveedores() {
        return proveedorService.obtenerTodosLosProveedores();
    }

    // API para obtener un proveedor por ID
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Proveedor> obtenerProveedorPorId(@PathVariable Integer id) {
        Optional<Proveedor> proveedor = proveedorService.obtenerProveedorPorId(id);
        if (proveedor.isPresent()) {
            return ResponseEntity.ok(proveedor.get());
        }
        return ResponseEntity.notFound().build();
    }

    // API para guardar un nuevo proveedor
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarProveedor(@RequestBody Proveedor proveedor) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar datos
            String validacion = proveedorService.validarProveedor(proveedor);
            if (validacion != null) {
                response.put("success", false);
                response.put("message", validacion);
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar si ya existe el número de documento
            if (proveedorService.existeNumeroDocumento(proveedor.getNumeroDocumento())) {
                response.put("success", false);
                response.put("message", "Ya existe un proveedor con este número de documento");
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar si ya existe el email
            if (proveedorService.existeEmail(proveedor.getEmail())) {
                response.put("success", false);
                response.put("message", "Ya existe un proveedor con este email");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar proveedor
            Proveedor proveedorGuardado = proveedorService.guardarProveedor(proveedor);
            response.put("success", true);
            response.put("message", "Proveedor registrado exitosamente");
            response.put("proveedor", proveedorGuardado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API para actualizar un proveedor
    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarProveedor(@PathVariable Integer id, @RequestBody Proveedor proveedor) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar si el proveedor existe
            Optional<Proveedor> proveedorExistente = proveedorService.obtenerProveedorPorId(id);
            if (!proveedorExistente.isPresent()) {
                response.put("success", false);
                response.put("message", "Proveedor no encontrado");
                return ResponseEntity.notFound().build();
            }

            // Establecer el ID para la actualización
            proveedor.setIdProveedor(id);

            // Validar datos
            String validacion = proveedorService.validarProveedor(proveedor);
            if (validacion != null) {
                response.put("success", false);
                response.put("message", validacion);
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar duplicados excluyendo el ID actual
            if (proveedorService.existeNumeroDocumentoParaEdicion(proveedor.getNumeroDocumento(), id)) {
                response.put("success", false);
                response.put("message", "Ya existe otro proveedor con este número de documento");
                return ResponseEntity.badRequest().body(response);
            }

            if (proveedorService.existeEmailParaEdicion(proveedor.getEmail(), id)) {
                response.put("success", false);
                response.put("message", "Ya existe otro proveedor con este email");
                return ResponseEntity.badRequest().body(response);
            }

            // Mantener la fecha de registro original
            proveedor.setFechaRegistro(proveedorExistente.get().getFechaRegistro());

            // Actualizar proveedor
            Proveedor proveedorActualizado = proveedorService.actualizarProveedor(proveedor);
            response.put("success", true);
            response.put("message", "Proveedor actualizado exitosamente");
            response.put("proveedor", proveedorActualizado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API para eliminar un proveedor (cambiar estado)
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProveedor(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean eliminado = proveedorService.eliminarProveedor(id);
            if (eliminado) {
                response.put("success", true);
                response.put("message", "Proveedor eliminado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Proveedor no encontrado");
            }
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API para activar un proveedor
    @PutMapping("/api/activar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activarProveedor(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean activado = proveedorService.activarProveedor(id);
            if (activado) {
                response.put("success", true);
                response.put("message", "Proveedor activado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Proveedor no encontrado");
            }
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al activar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API para buscar proveedores por nombre
    @GetMapping("/api/buscar")
    @ResponseBody
    public List<Proveedor> buscarProveedoresPorNombre(@RequestParam String nombre) {
        return proveedorService.buscarPorNombre(nombre);
    }
}
