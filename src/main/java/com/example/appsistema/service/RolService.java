package com.example.appsistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.appsistema.repository.RolRepository;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    // Método para eliminar un rol por su ID
    public void eliminarRol(Integer idRol) {
        rolRepository.deleteById(idRol);
    }
}
