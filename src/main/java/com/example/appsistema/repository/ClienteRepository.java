package com.example.appsistema.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
   
    
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombreCliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Cliente> searchClientes(@Param("search") String search, Pageable pageable);
    
    boolean existsByNumeroDocumento(String numeroDocumento);
}
