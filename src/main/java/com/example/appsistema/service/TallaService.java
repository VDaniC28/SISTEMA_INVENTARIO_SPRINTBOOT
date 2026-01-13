package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Talla;
import com.example.appsistema.repository.TallaRepository;

@Service
public class TallaService {
    @Autowired
    private TallaRepository tallaRepository;
    
    public Page<Talla> obtenerTodas(Pageable pageable) {
        return tallaRepository.findAll(pageable);
    }
    
    public List<Talla> obtenerTodos2() {
        return tallaRepository.findAll();
    } 
    public Optional<Talla> obtenerPorId(Integer id) {
        return tallaRepository.findById(id);
    }
    
    public Talla guardar(Talla talla) throws RuntimeException {
        if (talla.getIdTalla() == null && tallaRepository.existsByValor(talla.getValor())) {
            throw new RuntimeException("Ya existe una talla con ese valor");
        }
        if (talla.getIdTalla() != null) {
            Optional<Talla> existente = tallaRepository.findByValor(talla.getValor());
            if (existente.isPresent() && !existente.get().getIdTalla().equals(talla.getIdTalla())) {
                throw new RuntimeException("Ya existe una talla con ese valor");
            }
        }
        return tallaRepository.save(talla);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!tallaRepository.existsById(id)) {
            throw new RuntimeException("Talla no encontrada");
        }
        tallaRepository.deleteById(id);
    }
}
