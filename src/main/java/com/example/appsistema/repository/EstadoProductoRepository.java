package com.example.appsistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.EstadoProducto;

@Repository
public interface EstadoProductoRepository extends JpaRepository<EstadoProducto, Integer> {
    Optional<EstadoProducto> findByDescripcionEstado(String descripcionEstado);
    List<EstadoProducto> findByAfectaStock(Boolean afectaStock);
    boolean existsByDescripcionEstado(String descripcionEstado);
}