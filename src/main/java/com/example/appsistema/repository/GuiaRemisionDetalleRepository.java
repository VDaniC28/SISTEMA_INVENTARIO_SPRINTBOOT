package com.example.appsistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.GuiaRemisionDetalle;

@Repository
public interface GuiaRemisionDetalleRepository extends JpaRepository<GuiaRemisionDetalle, Integer> {
    
    List<GuiaRemisionDetalle> findByGuiaRemisionIdGuia(Integer idGuia);
    
    @Query("SELECT MAX(d.numeroItem) FROM GuiaRemisionDetalle d WHERE d.guiaRemision.idGuia = :idGuia")
    Integer findMaxNumeroItem(@Param("idGuia") Integer idGuia);
}