package com.example.appsistema.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.MaterialSuela;
import com.example.appsistema.repository.MaterialSuelaRepository;

@Service
public class MaterialSuelaService {
     @Autowired
    private MaterialSuelaRepository materialSuelaRepository;
    
    public Page<MaterialSuela> obtenerTodos(Pageable pageable) {
        return materialSuelaRepository.findAll(pageable);
    }
    
    public List<MaterialSuela> obtenerTodos2() {
        return materialSuelaRepository.findAll();
    } 
    public Optional<MaterialSuela> obtenerPorId(Integer id) {
        return materialSuelaRepository.findById(id);
    }
    
    public MaterialSuela guardar(MaterialSuela materialSuela) throws RuntimeException {
        if (materialSuela.getIdMaterialSuela() == null && materialSuelaRepository.existsByDescripcionSuela(materialSuela.getDescripcionSuela())) {
            throw new RuntimeException("Ya existe un material de suela con esa descripción");
        }
        if (materialSuela.getIdMaterialSuela() != null) {
            Optional<MaterialSuela> existente = materialSuelaRepository.findByDescripcionSuela(materialSuela.getDescripcionSuela());
            if (existente.isPresent() && !existente.get().getIdMaterialSuela().equals(materialSuela.getIdMaterialSuela())) {
                throw new RuntimeException("Ya existe un material de suela con esa descripción");
            }
        }
        return materialSuelaRepository.save(materialSuela);
    }
    
    public void eliminar(Integer id) throws RuntimeException {
        if (!materialSuelaRepository.existsById(id)) {
            throw new RuntimeException("Material de suela no encontrado");
        }
        materialSuelaRepository.deleteById(id);
    }
}
