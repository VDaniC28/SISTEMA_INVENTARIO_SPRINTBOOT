package com.example.appsistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.TipoItem;

@Repository
public interface TipoItemRepository extends JpaRepository<TipoItem, Integer> {
    Optional<TipoItem> findByDescripcion(TipoItem.TipoDescripcion descripcion);
    boolean existsByDescripcion(TipoItem.TipoDescripcion descripcion);
}