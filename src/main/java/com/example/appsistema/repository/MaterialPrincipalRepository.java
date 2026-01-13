package com.example.appsistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.MaterialPrincipal;

@Repository
public interface MaterialPrincipalRepository extends JpaRepository<MaterialPrincipal, Integer> {
    Optional<MaterialPrincipal> findByNombreMaterial(String nombreMaterial);
    boolean existsByNombreMaterial(String nombreMaterial);
}
