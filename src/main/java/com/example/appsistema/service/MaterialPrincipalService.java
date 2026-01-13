package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.MaterialPrincipal;

import com.example.appsistema.repository.MaterialPrincipalRepository;

@Service
public class MaterialPrincipalService {
    
    @Autowired
    private MaterialPrincipalRepository materialPrincipalRepository;
    
    public Page<MaterialPrincipal> obtenerTodos(Pageable pageable) {
        return materialPrincipalRepository.findAll(pageable);
    }
    public List<MaterialPrincipal> obtenerTodos2() {
        return materialPrincipalRepository.findAll();
    } 
    public Optional<MaterialPrincipal> obtenerPorId(Integer id) {
        return materialPrincipalRepository.findById(id);
    }
    
    public MaterialPrincipal guardar(MaterialPrincipal material) throws RuntimeException {
        if (material.getIdMaterialPrincipal() == null && materialPrincipalRepository.existsByNombreMaterial(material.getNombreMaterial())) {
            throw new RuntimeException("Ya existe un material con ese nombre");
        }
        if (material.getIdMaterialPrincipal() != null) {
            Optional<MaterialPrincipal> existente = materialPrincipalRepository.findByNombreMaterial(material.getNombreMaterial());
            if (existente.isPresent() && !existente.get().getIdMaterialPrincipal().equals(material.getIdMaterialPrincipal())) {
                throw new RuntimeException("Ya existe un material con ese nombre");
            }
        }
        return materialPrincipalRepository.save(material);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!materialPrincipalRepository.existsById(id)) {
            throw new RuntimeException("Material principal no encontrado");
        }
        materialPrincipalRepository.deleteById(id);
    }
}
