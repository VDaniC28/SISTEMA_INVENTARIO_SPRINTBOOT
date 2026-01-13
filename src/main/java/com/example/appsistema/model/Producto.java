package com.example.appsistema.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.NotBlank;

import com.example.appsistema.component.AuditEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Table(name="productos")
@EntityListeners(AuditEntityListener.class) // ← AGREGAR ESTA LÍNEA
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(name = "serialProducto", length = 150, nullable = false)
    @NotBlank(message = "El serial del producto es obligatorio")
    private String serialProducto;

    @Column(name = "nombreProducto", length = 150, nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    @Column(name = "descripcionProducto", length = 150, nullable = false)
    @NotBlank(message = "La descripción del producto es obligatoria")
    private String descripcionProducto;

    // Referencias a catálogos
    @ManyToOne
    @JoinColumn(name = "idTipoItem")
    private TipoItem tipoItem;

    @ManyToOne
    @JoinColumn(name = "idMarca")
    private Marca marca;

    @ManyToOne
    @JoinColumn(name = "idCategoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "idProveedor")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "idColor")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "idGenero")
    private Genero genero;

    @ManyToOne
    @JoinColumn(name = "idMaterialSuela")
    private MaterialSuela materialSuela;

    @ManyToOne
    @JoinColumn(name = "idTalla")
    private Talla talla;

    @ManyToOne
    @JoinColumn(name = "idMaterialPrincipal")
    private MaterialPrincipal materialPrincipal;

    @ManyToOne
    @JoinColumn(name = "idTipoPersona")
    private TipoPersona tipoPersona;

    @ManyToOne
    @JoinColumn(name = "idEstadoProducto")
    private EstadoProducto estadoProducto;

    @Column(name = "precioVenta", precision = 10, scale = 4, nullable = false)
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @Column(name = "stockMinimo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 0;

    @Column(name = "stockMaximo")
    @Min(value = 1, message = "El stock máximo debe ser mayor a 0")
    private Integer stockMaximo = 1000;

    @Column(name = "imagenURL")
    private String imagenURL;

    @Column(name = "fechaRegistro")
    private LocalDate fechaRegistro;

    // Constructores
    public Producto() {
        this.fechaRegistro = LocalDate.now();
    }

    public Producto(String serialProducto, String nombreProducto, String descripcionProducto, BigDecimal precioVenta) {
        this();
        this.serialProducto = serialProducto;
        this.nombreProducto = nombreProducto;
        this.descripcionProducto = descripcionProducto;
        this.precioVenta = precioVenta;
    }

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getSerialProducto() {
        return serialProducto;
    }

    public void setSerialProducto(String serialProducto) {
        this.serialProducto = serialProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public TipoItem getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItem tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public MaterialSuela getMaterialSuela() {
        return materialSuela;
    }

    public void setMaterialSuela(MaterialSuela materialSuela) {
        this.materialSuela = materialSuela;
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public MaterialPrincipal getMaterialPrincipal() {
        return materialPrincipal;
    }

    public void setMaterialPrincipal(MaterialPrincipal materialPrincipal) {
        this.materialPrincipal = materialPrincipal;
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(TipoPersona tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    public EstadoProducto getEstadoProducto() {
        return estadoProducto;
    }

    public void setEstadoProducto(EstadoProducto estadoProducto) {
        this.estadoProducto = estadoProducto;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Integer getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(Integer stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Método para obtener el stock actual desde inventario
    public Integer getStockActual() {
        // Este método se implementará cuando se integre con el módulo de inventario
        return 0;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", serialProducto='" + serialProducto + '\'' +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", precioVenta=" + precioVenta +
                '}';
    }
}
