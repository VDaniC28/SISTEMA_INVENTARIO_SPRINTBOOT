package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Color;

import com.example.appsistema.repository.ColorRepository;

@Service
public class ColorService {
    
    @Autowired
    private ColorRepository colorRepository;
    
    public Page<Color> obtenerTodos(Pageable pageable) {
        return colorRepository.findAll(pageable);
    }
    
    public List<Color> obtenerTodos2() {
        return colorRepository.findAll();
    } 
    public Optional<Color> obtenerPorId(Integer id) {
        return colorRepository.findById(id);
    }
    
    public Color guardar(Color color) throws RuntimeException {
        if (color.getIdColor() == null && colorRepository.existsByNombreColor(color.getNombreColor())) {
            throw new RuntimeException("Ya existe un color con ese nombre");
        }
        if (color.getIdColor() != null) {
            Optional<Color> existente = colorRepository.findByNombreColor(color.getNombreColor());
            if (existente.isPresent() && !existente.get().getIdColor().equals(color.getIdColor())) {
                throw new RuntimeException("Ya existe un color con ese nombre");
            }
        }
        return colorRepository.save(color);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!colorRepository.existsById(id)) {
            throw new RuntimeException("Color no encontrado");
        }
        colorRepository.deleteById(id);
    }
}
