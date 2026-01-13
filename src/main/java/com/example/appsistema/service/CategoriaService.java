package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Categoria;

import com.example.appsistema.repository.CategoriaRepository;

@Service
public class CategoriaService {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public Page<Categoria> obtenerTodas(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }
    
    public List<Categoria> obtenerTodos2() {
        return categoriaRepository.findAll();
    }
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }
    
    public Categoria guardar(Categoria categoria) throws RuntimeException {
        if (categoria.getIdCategoria() == null && categoriaRepository.existsByNombreCategoria(categoria.getNombreCategoria())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        if (categoria.getIdCategoria() != null) {
            Optional<Categoria> existente = categoriaRepository.findByNombreCategoria(categoria.getNombreCategoria());
            if (existente.isPresent() && !existente.get().getIdCategoria().equals(categoria.getIdCategoria())) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
        }
        return categoriaRepository.save(categoria);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoriaRepository.deleteById(id);
    }
}
