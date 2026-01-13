package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Marca;

import com.example.appsistema.repository.MarcaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public class MarcaService {
    @Autowired
    private MarcaRepository marcaRepository;
    
    public Page<Marca> obtenerTodas(Pageable pageable) {
        // Llama a findAll(Pageable) que devuelve Page
        return marcaRepository.findAll(pageable);
    }
    public List<Marca> obtenerTodos2() {
        return marcaRepository.findAll();
    }
    public Optional<Marca> obtenerPorId(Integer id) {
        return marcaRepository.findById(id);
    }
    
    public Marca guardar(Marca marca) throws RuntimeException {
        if (marca.getIdMarca() == null && marcaRepository.existsByNombre(marca.getNombre())) {
            throw new RuntimeException("Ya existe una marca con ese nombre");
        }
        if (marca.getIdMarca() != null) {
            Optional<Marca> existente = marcaRepository.findByNombre(marca.getNombre());
            if (existente.isPresent() && !existente.get().getIdMarca().equals(marca.getIdMarca())) {
                throw new RuntimeException("Ya existe una marca con ese nombre");
            }
        }
        return marcaRepository.save(marca);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!marcaRepository.existsById(id)) {
            throw new RuntimeException("Marca no encontrada");
        }
        marcaRepository.deleteById(id);
    }
}
