package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.TipoItem;
import com.example.appsistema.repository.TipoItemRepository;

@Service
public class TipoItemService {
    @Autowired
    private TipoItemRepository tipoItemRepository;
    
    public Page<TipoItem> obtenerTodos(Pageable pageable) {
        return tipoItemRepository.findAll(pageable);
    }
    
    public Optional<TipoItem> obtenerPorId(Integer id) {
        return tipoItemRepository.findById(id);
    }
    // Agrega este nuevo método para obtener todos sin paginación
    public List<TipoItem> obtenerTodos2() {
        return tipoItemRepository.findAll();
    }
    public TipoItem guardar(TipoItem tipoItem) throws RuntimeException {
        if (tipoItem.getIdTipoItem() == null && tipoItemRepository.existsByDescripcion(tipoItem.getDescripcion())) {
            throw new RuntimeException("Ya existe un tipo de item con esa descripción");
        }
        if (tipoItem.getIdTipoItem() != null) {
            Optional<TipoItem> existente = tipoItemRepository.findByDescripcion(tipoItem.getDescripcion());
            if (existente.isPresent() && !existente.get().getIdTipoItem().equals(tipoItem.getIdTipoItem())) {
                throw new RuntimeException("Ya existe un tipo de item con esa descripción");
            }
        }
        return tipoItemRepository.save(tipoItem);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!tipoItemRepository.existsById(id)) {
            throw new RuntimeException("Tipo de item no encontrado");
        }
        tipoItemRepository.deleteById(id);
    }
}
