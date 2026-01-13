package com.example.appsistema.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.appsistema.model.Proveedor;
import com.example.appsistema.repository.ProveedorRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorService {
    @Autowired
    private ProveedorRepository proveedorRepository;

    // Obtener todos los proveedores
    public List<Proveedor> obtenerTodosLosProveedores() {
        return proveedorRepository.findAll();
    }

    public List<Proveedor> obtenerTodos2() {
        return proveedorRepository.findAll();
    } 

    // Obtener solo proveedores activos
    public List<Proveedor> obtenerProveedoresActivos() {
        return proveedorRepository.findByEstadoTrue();
    }

    // Obtener proveedor por ID
    public Optional<Proveedor> obtenerProveedorPorId(Integer id) {
        return proveedorRepository.findById(id);
    }

    // Guardar o actualizar proveedor
    public Proveedor guardarProveedor(Proveedor proveedor) {
        // Si es un nuevo proveedor, establecer fecha de registro
        if (proveedor.getIdProveedor() == null) {
            proveedor.setFechaRegistro(LocalDateTime.now());
        }
        return proveedorRepository.save(proveedor);
    }

    // Actualizar proveedor
    public Proveedor actualizarProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    // Eliminar proveedor (cambiar estado a false)
    public boolean eliminarProveedor(Integer id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            proveedor.setEstado(false);
            proveedorRepository.save(proveedor);
            return true;
        }
        return false;
    }

    // Eliminar permanentemente (solo si es necesario)
    public boolean eliminarProveedorPermanente(Integer id) {
        if (proveedorRepository.existsById(id)) {
            proveedorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Validar si existe un proveedor con el mismo número de documento
    public boolean existeNumeroDocumento(String numeroDocumento) {
        return proveedorRepository.findByNumeroDocumento(numeroDocumento).isPresent();
    }

    // Validar si existe un proveedor con el mismo email
    public boolean existeEmail(String email) {
        return proveedorRepository.findByEmail(email).isPresent();
    }

    // Validar número de documento para edición (excluyendo el ID actual)
    public boolean existeNumeroDocumentoParaEdicion(String numeroDocumento, Integer idProveedor) {
        return proveedorRepository.existsByNumeroDocumentoAndNotId(numeroDocumento, idProveedor);
    }

    // Validar email para edición (excluyendo el ID actual)
    public boolean existeEmailParaEdicion(String email, Integer idProveedor) {
        return proveedorRepository.existsByEmailAndNotId(email, idProveedor);
    }

    // Buscar proveedores por nombre
    public List<Proveedor> buscarPorNombre(String nombre) {
        return proveedorRepository.findByNombreProveedorContainingIgnoreCase(nombre);
    }

    // Activar proveedor
    public boolean activarProveedor(Integer id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            proveedor.setEstado(true);
            proveedorRepository.save(proveedor);
            return true;
        }
        return false;
    }

    // Validar datos del proveedor
    public String validarProveedor(Proveedor proveedor) {
        // Validar nombre
        if (proveedor.getNombreProveedor() == null || proveedor.getNombreProveedor().trim().isEmpty()) {
            return "El nombre del proveedor es obligatorio";
        }
        if (proveedor.getNombreProveedor().length() > 150) {
            return "El nombre del proveedor no puede exceder 150 caracteres";
        }

        // Validar número de documento según tipo
        if (proveedor.getTipoDocumento() == Proveedor.TipoDocumento.DNI) {
            if (proveedor.getNumeroDocumento().length() != 8) {
                return "El DNI debe tener exactamente 8 dígitos";
            }
        } else if (proveedor.getTipoDocumento() == Proveedor.TipoDocumento.RUC) {
            if (proveedor.getNumeroDocumento().length() != 11) {
                return "El RUC debe tener exactamente 11 dígitos";
            }
        }

        // Validar email
        if (proveedor.getEmail() == null || proveedor.getEmail().trim().isEmpty()) {
            return "El email es obligatorio";
        }
        if (!proveedor.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return "El formato del email no es válido";
        }
        if (proveedor.getEmail().length() > 300) {
            return "El email no puede exceder 300 caracteres";
        }

        // Validar teléfono si se proporciona
        if (proveedor.getTelefono() != null && !proveedor.getTelefono().trim().isEmpty()) {
            if (proveedor.getTelefono().length() > 13) {
                return "El teléfono no puede exceder 13 caracteres";
            }
        }

        // Validar dirección si se proporciona
        if (proveedor.getDireccion() != null && proveedor.getDireccion().length() > 200) {
            return "La dirección no puede exceder 200 caracteres";
        }

        return null; // Sin errores
    }
}
