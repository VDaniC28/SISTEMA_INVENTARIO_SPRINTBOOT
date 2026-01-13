package com.example.appsistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Talla;

@Repository
public interface TallaRepository extends JpaRepository<Talla, Integer> {
    Optional<Talla> findByValor(String valor);
    boolean existsByValor(String valor);
}