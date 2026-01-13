package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.TipoPersona;
import com.example.appsistema.repository.TipoPersonaRepository;

@Service
public class TipoPersonaService {
    
    @Autowired
    private TipoPersonaRepository tipoPersonaRepository;
    
    public Page<TipoPersona> obtenerTodos(Pageable pageable) {
        return tipoPersonaRepository.findAll(pageable);
    }
    public List<TipoPersona> obtenerTodos2() {
        return tipoPersonaRepository.findAll();
    } 
    public Optional<TipoPersona> obtenerPorId(Integer id) {
        return tipoPersonaRepository.findById(id);
    }
    
    public TipoPersona guardar(TipoPersona tipoPersona) throws RuntimeException {
        if (tipoPersona.getIdTipoPersona() == null && tipoPersonaRepository.existsByNombreTipoPersona(tipoPersona.getNombreTipoPersona())) {
            throw new RuntimeException("Ya existe un tipo de persona con ese nombre");
        }
        if (tipoPersona.getIdTipoPersona() != null) {
            Optional<TipoPersona> existente = tipoPersonaRepository.findByNombreTipoPersona(tipoPersona.getNombreTipoPersona());
            if (existente.isPresent() && !existente.get().getIdTipoPersona().equals(tipoPersona.getIdTipoPersona())) {
                throw new RuntimeException("Ya existe un tipo de persona con ese nombre");
            }
        }
        return tipoPersonaRepository.save(tipoPersona);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!tipoPersonaRepository.existsById(id)) {
            throw new RuntimeException("Tipo de persona no encontrado");
        }
        tipoPersonaRepository.deleteById(id);
    }
}
