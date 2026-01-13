package com.example.appsistema.model;

import java.time.LocalDate;

import com.example.appsistema.component.AuditEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="clientes")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente")
    private Integer idCliente;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoCliente", nullable = false)
    private TipoCliente tipoCliente;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoDocumento", nullable = false)
    private TipoDocumento tipoDocumento;
    
    @Column(name = "numeroDocumento", nullable = false, length = 20)
    private String numeroDocumento;
    
    @Column(name = "nombreCliente", nullable = false, length = 150)
    private String nombreCliente;
    
    @Column(name = "nombreComercial", length = 150)
    private String nombreComercial;
    
    @Column(name = "telefono", length = 13)
    private String telefono;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "direccion", length = 200)
    private String direccion;
    
    @Column(name = "fechaRegistro")
    private LocalDate fechaRegistro;
    
    @Column(name = "estadoValor", length = 100)
    private String estadoValor;
    
    // Enums
    public enum TipoCliente {
        Persona, Empresa
    }
    
    public enum TipoDocumento {
        DNI, RUC, Pasaporte
    }
    
    // ======================================================
    // 🏆 Método Unificado para Mostrar el Nombre del Cliente
    // ======================================================
    public String getNombreDisplay() {
        // 1. Si es tipo EMPRESA y tiene nombre comercial, usar ese (prioridad)
        if (TipoCliente.Empresa.equals(this.tipoCliente) && this.nombreComercial != null && !this.nombreComercial.trim().isEmpty()) {
            return this.nombreComercial;
        }
        
        // 2. Usar nombreCliente. Esto cubre:
        //    a) Tipo Cliente 'Persona' (donde nombreCliente es el campo principal).
        //    b) Tipo Cliente 'Empresa' que no tiene un nombreComercial registrado (aunque nombreCliente es NOT NULL en la DB).
        if (this.nombreCliente != null) {
            return this.nombreCliente;
        }
        
        // 3. Fallback (Nunca debería pasar si la DB aplica el NOT NULL correctamente)
        return "Cliente sin nombre";
    }

    
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
        validarYFormatearEstado();
    }

    // AGREGAR ESTO: Asegura que en cada actualización el estado sea válido
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        validarYFormatearEstado();
    }

    private void validarYFormatearEstado() {
        if (this.estadoValor == null || this.estadoValor.trim().isEmpty()) {
            this.estadoValor = "BUEN PAGADOR"; // Valor por defecto seguro
        } else {
            // Limpia espacios y convierte a MAYÚSCULAS para que coincida con el CHECK de la DB
            this.estadoValor = this.estadoValor.trim().toUpperCase();
            
            // Validación de seguridad final
            if (!this.estadoValor.equals("BUEN PAGADOR") && !this.estadoValor.equals("MAL PAGADOR")) {
                this.estadoValor = "BUEN PAGADOR"; 
            }
        }
    }
}
