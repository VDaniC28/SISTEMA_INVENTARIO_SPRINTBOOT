package com.example.appsistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.TipoPersona;

@Repository
public interface TipoPersonaRepository extends JpaRepository<TipoPersona, Integer> {
    Optional<TipoPersona> findByNombreTipoPersona(String nombreTipoPersona);
    boolean existsByNombreTipoPersona(String nombreTipoPersona);
}