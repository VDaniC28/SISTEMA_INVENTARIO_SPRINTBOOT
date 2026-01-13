package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Genero;
import com.example.appsistema.repository.GeneroRepository;

@Service
public class GeneroService {
     @Autowired
    private GeneroRepository generoRepository;
    
    public Page<Genero> obtenerTodos(Pageable pageable) {
        return generoRepository.findAll(pageable);
    }
    
    public List<Genero> obtenerTodos2() {
        return generoRepository.findAll();
    } 
    public Optional<Genero> obtenerPorId(Integer id) {
        return generoRepository.findById(id);
    }
    
    public Genero guardar(Genero genero) throws RuntimeException {
        if (genero.getIdGenero() == null && generoRepository.existsByNombreGenero(genero.getNombreGenero())) {
            throw new RuntimeException("Ya existe un género con ese nombre");
        }
        if (genero.getIdGenero() != null) {
            Optional<Genero> existente = generoRepository.findByNombreGenero(genero.getNombreGenero());
            if (existente.isPresent() && !existente.get().getIdGenero().equals(genero.getIdGenero())) {
                throw new RuntimeException("Ya existe un género con ese nombre");
            }
        }
        return generoRepository.save(genero);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!generoRepository.existsById(id)) {
            throw new RuntimeException("Género no encontrado");
        }
        generoRepository.deleteById(id);
    }
}
