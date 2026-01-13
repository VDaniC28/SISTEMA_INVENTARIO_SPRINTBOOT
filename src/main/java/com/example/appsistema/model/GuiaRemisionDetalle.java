package com.example.appsistema.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guias_remision_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionDetalle {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalleGuia;
    
    @Column(nullable = false)
    private Integer numeroItem;
    
    @ManyToOne
    @JoinColumn(name = "idGuia", nullable = false)
    private GuiaRemision guiaRemision;
    
    @ManyToOne
    @JoinColumn(name = "idProducto")
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "idSuministro")
    private Suministro suministro;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    private Integer cantidadRecibida = 0;
    
    @Column(length = 20)
    private String unidadMedida = "UND";
    
    @Column(precision = 8, scale = 3)
    private BigDecimal pesoUnitario;
    
    @Column(length = 50)
    private String lote;
    
    @Enumerated(EnumType.STRING)
    private EstadoItem estadoItem = EstadoItem.Pendiente;
    
    @Column(length = 255)
    private String observacionesItem;
    
    public enum EstadoItem {
        Pendiente, Recibido, Faltante, Dañado
    }
    
    public String getNombreItem() {
        if (producto != null) return producto.getNombreProducto();
        if (suministro != null) return suministro.getNombreSuministro();
        return "";
    }
}
