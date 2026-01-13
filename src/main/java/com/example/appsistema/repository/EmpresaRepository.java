package com.example.appsistema.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.appsistema.model.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository <Empresa,Integer> {
  
    
} 
