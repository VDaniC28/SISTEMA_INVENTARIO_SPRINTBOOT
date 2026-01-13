package com.example.appsistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    Optional<Color> findByNombreColor(String nombreColor);
    boolean existsByNombreColor(String nombreColor);
}