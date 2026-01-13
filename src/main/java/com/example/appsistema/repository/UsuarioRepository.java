package com.example.appsistema.repository;

import com.example.appsistema.model.Usuario; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Esta es la consulta completa para cargar roles Y empresas
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.empresas WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

}