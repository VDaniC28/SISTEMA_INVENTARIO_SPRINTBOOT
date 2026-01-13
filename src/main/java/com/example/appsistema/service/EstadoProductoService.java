package com.example.appsistema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.EstadoProducto;
import com.example.appsistema.repository.EstadoProductoRepository;

@Service
public class EstadoProductoService {
    
    @Autowired
    private EstadoProductoRepository estadoProductoRepository;
    
    public Page<EstadoProducto> obtenerTodos(Pageable pageable) {
        return estadoProductoRepository.findAll(pageable);
    }
    public List<EstadoProducto> obtenerTodos2() {
        return estadoProductoRepository.findAll();
    } 
    public Optional<EstadoProducto> obtenerPorId(Integer id) {
        return estadoProductoRepository.findById(id);
    }
    
    public List<EstadoProducto> obtenerPorAfectaStock(Boolean afectaStock) {
        return estadoProductoRepository.findByAfectaStock(afectaStock);
    }
    
    public EstadoProducto guardar(EstadoProducto estadoProducto) throws RuntimeException {
        if (estadoProducto.getIdEstadoProducto() == null && estadoProductoRepository.existsByDescripcionEstado(estadoProducto.getDescripcionEstado())) {
            throw new RuntimeException("Ya existe un estado de producto con esa descripción");
        }
        if (estadoProducto.getIdEstadoProducto() != null) {
            Optional<EstadoProducto> existente = estadoProductoRepository.findByDescripcionEstado(estadoProducto.getDescripcionEstado());
            if (existente.isPresent() && !existente.get().getIdEstadoProducto().equals(estadoProducto.getIdEstadoProducto())) {
                throw new RuntimeException("Ya existe un estado de producto con esa descripción");
            }
        }
        return estadoProductoRepository.save(estadoProducto);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!estadoProductoRepository.existsById(id)) {
            throw new RuntimeException("Estado de producto no encontrado");
        }
        estadoProductoRepository.deleteById(id);
    }
}
