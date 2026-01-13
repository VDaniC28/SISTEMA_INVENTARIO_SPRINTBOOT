package com.example.appsistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

                        //VISTAPROVEEDORES
    // Buscar proveedores activos
    List<Proveedor> findByEstadoTrue();
    
    // Buscar por número de documento
    Optional<Proveedor> findByNumeroDocumento(String numeroDocumento);
    
    // Buscar por email
    Optional<Proveedor> findByEmail(String email);
    
    // Buscar por nombre del proveedor (ignorando mayúsculas/minúsculas)
    List<Proveedor> findByNombreProveedorContainingIgnoreCase(String nombreProveedor);
    
    // Verificar si existe un proveedor con el mismo número de documento (excluyendo el ID actual para edición)
    @Query("SELECT COUNT(p) > 0 FROM Proveedor p WHERE p.numeroDocumento = :numeroDocumento AND p.idProveedor != :idProveedor")
    boolean existsByNumeroDocumentoAndNotId(@Param("numeroDocumento") String numeroDocumento, @Param("idProveedor") Integer idProveedor);
    
    // Verificar si existe un proveedor con el mismo email (excluyendo el ID actual para edición)
    @Query("SELECT COUNT(p) > 0 FROM Proveedor p WHERE p.email = :email AND p.idProveedor != :idProveedor")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("idProveedor") Integer idProveedor);
    
    // Buscar proveedores por tipo de documento
    List<Proveedor> findByTipoDocumentoAndEstadoTrue(Proveedor.TipoDocumento tipoDocumento);
    
    // Buscar proveedores por tipo de persona
    List<Proveedor> findByTipoPersonaAndEstadoTrue(Proveedor.TipoPersona tipoPersona);

}
