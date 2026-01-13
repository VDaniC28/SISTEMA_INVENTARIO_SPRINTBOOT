package com.example.appsistema.service;

import java.time.LocalDate;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appsistema.model.Empresa;
import com.example.appsistema.model.GuiaRemision;
import com.example.appsistema.model.GuiaRemisionDetalle;
import com.example.appsistema.model.OrdenesCompra;
import com.example.appsistema.model.OrdenesCompraDetalle;
import com.example.appsistema.model.SalidaInventario;
import com.example.appsistema.model.SalidaInventarioDetalle;
import com.example.appsistema.repository.EmpresaRepository;
import com.example.appsistema.repository.GuiaRemisionDetalleRepository;
import com.example.appsistema.repository.GuiaRemisionRepository;
import com.example.appsistema.repository.OrdenesCompraDetalleRepository;
import com.example.appsistema.repository.OrdenesCompraRepository;
import com.example.appsistema.repository.SalidaInventarioDetalleRepository;
import com.example.appsistema.repository.SalidaInventarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuiaRemisionService {
    
    @Autowired
    private GuiaRemisionRepository guiaRepository;
    
    @Autowired
    private GuiaRemisionDetalleRepository detalleRepository;
    
    @Autowired
    private OrdenesCompraRepository ordenCompraRepository;
    
    @Autowired
    private SalidaInventarioRepository salidaInventarioRepository;
    
    @Autowired
    private OrdenesCompraDetalleRepository ordenDetalleRepository;
    
    @Autowired
    private SalidaInventarioDetalleRepository salidaDetalleRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;

    // Obtener órdenes con paginación
    public Page<OrdenesCompra> getOrdenesConfirmadasSinGuia(int page, int size) {
        return ordenCompraRepository.findAllWithProveedorAndAlmacen(PageRequest.of(page, size));
    }

    // Obtener salidas con paginación
    public Page<SalidaInventario> getSalidasCompletadasSinGuia(int page, int size) {
        return salidaInventarioRepository.findAllWithCliente(PageRequest.of(page, size));
    }

    // Generar número de guía automático
    public String generarNumeroGuia(Integer idEmpresa) {
        String maxCorrelativo = guiaRepository.findMaxCorrelativo(idEmpresa);
        int siguiente = 1;
        
        if (maxCorrelativo != null && !maxCorrelativo.isEmpty()) {
            try {
                siguiente = Integer.parseInt(maxCorrelativo) + 1;
            } catch (NumberFormatException e) {
                siguiente = 1;
            }
        }
        
        return String.format("%07d", siguiente);
    }

    // Generar serie automática
    public String generarSerie() {
        return "E001";
    }

    // Crear guía desde orden de compra
    @Transactional
    public GuiaRemision crearGuiaDesdeOrdenCompra(Integer idOrden, GuiaRemision guiaData, Integer idEmpresa) {
        if (guiaRepository.existsByOrdenCompra(idOrden)) {
            throw new RuntimeException("Ya existe una guía de remisión para esta orden de compra");
        }

        OrdenesCompra orden = ordenCompraRepository.findById(idOrden)
            .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
            
        Empresa empresa = empresaRepository.findById(idEmpresa)
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        GuiaRemision guia = new GuiaRemision();
        String numeroGenerado = generarNumeroGuia(idEmpresa);
        
        guia.setNumeroGuia(numeroGenerado);
        guia.setSerieGuia(generarSerie());
        guia.setCorrelativoGuia(numeroGenerado);
        guia.setFechaEmision(LocalDate.now());
        guia.setFechaTraslado(guiaData.getFechaTraslado());
        guia.setTipoGuia(GuiaRemision.TipoGuia.Entrada);
        guia.setEstadoGuia(GuiaRemision.EstadoGuia.Emitida);
        guia.setEmpresa(empresa);
        guia.setOrdenCompra(orden);
        guia.setAlmacenDestino(orden.getAlmacen());
        guia.setTransportista(guiaData.getTransportista());
        guia.setPlacaVehiculo(guiaData.getPlacaVehiculo());
        guia.setLicenciaConducir(guiaData.getLicenciaConducir());
        guia.setMotivoTraslado(GuiaRemision.MotivoTraslado.Compra);
        guia.setPesoTotal(guiaData.getPesoTotal());
        guia.setNumeroPackages(guiaData.getNumeroPackages());
        guia.setObservaciones(guiaData.getObservaciones());

        guia = guiaRepository.save(guia);

        List<OrdenesCompraDetalle> detallesOrden = ordenDetalleRepository.findByOrdenCompraIdOrdenes(idOrden);
        int numeroItem = 1;

        for (OrdenesCompraDetalle detalle : detallesOrden) {
            GuiaRemisionDetalle guiaDetalle = new GuiaRemisionDetalle();
            guiaDetalle.setNumeroItem(numeroItem++);
            guiaDetalle.setGuiaRemision(guia);
            guiaDetalle.setProducto(detalle.getProducto());
            guiaDetalle.setSuministro(detalle.getSuministro());
            guiaDetalle.setCantidad(detalle.getCantidadSolicitada());
            guiaDetalle.setCantidadRecibida(0);
            guiaDetalle.setUnidadMedida("UND");
            guiaDetalle.setEstadoItem(GuiaRemisionDetalle.EstadoItem.Pendiente);
            
            detalleRepository.save(guiaDetalle);
        }

        return guia;
    }

    // Crear guía desde salida de inventario
    @Transactional
    public GuiaRemision crearGuiaDesdeSalidaInventario(Integer idSalida, GuiaRemision guiaData, Integer idEmpresa) {
        if (guiaRepository.existsBySalidaInventario(idSalida)) {
            throw new RuntimeException("Ya existe una guía de remisión para esta salida de inventario");
        }

        SalidaInventario salida = salidaInventarioRepository.findById(idSalida)
            .orElseThrow(() -> new RuntimeException("Salida de inventario no encontrada"));
            
        Empresa empresa = empresaRepository.findById(idEmpresa)
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        GuiaRemision guia = new GuiaRemision();
        String numeroGenerado = generarNumeroGuia(idEmpresa);
        
        guia.setNumeroGuia(numeroGenerado);
        guia.setSerieGuia(generarSerie());
        guia.setCorrelativoGuia(numeroGenerado);
        guia.setFechaEmision(LocalDate.now());
        guia.setFechaTraslado(guiaData.getFechaTraslado());
        guia.setTipoGuia(GuiaRemision.TipoGuia.Salida);
        guia.setEstadoGuia(GuiaRemision.EstadoGuia.Emitida);
        guia.setEmpresa(empresa);
        guia.setSalidaInventario(salida);
        guia.setAlmacenOrigen(salida.getAlmacen());
        guia.setTransportista(guiaData.getTransportista());
        guia.setPlacaVehiculo(guiaData.getPlacaVehiculo());
        guia.setLicenciaConducir(guiaData.getLicenciaConducir());
        guia.setMotivoTraslado(GuiaRemision.MotivoTraslado.Venta);
        guia.setPesoTotal(guiaData.getPesoTotal());
        guia.setNumeroPackages(guiaData.getNumeroPackages());
        guia.setObservaciones(guiaData.getObservaciones());

        guia = guiaRepository.save(guia);

        List<SalidaInventarioDetalle> detallesSalida = salidaDetalleRepository.findBySalidaInventario_IdSalida(idSalida);
        int numeroItem = 1;

        for (SalidaInventarioDetalle detalle : detallesSalida) {
            GuiaRemisionDetalle guiaDetalle = new GuiaRemisionDetalle();
            guiaDetalle.setNumeroItem(numeroItem++);
            guiaDetalle.setGuiaRemision(guia);
            guiaDetalle.setProducto(detalle.getProducto());
            guiaDetalle.setSuministro(detalle.getSuministro());
            guiaDetalle.setCantidad(detalle.getCantidad());
            guiaDetalle.setCantidadRecibida(0);
            guiaDetalle.setUnidadMedida("UND");
            guiaDetalle.setLote(detalle.getLote());
            guiaDetalle.setEstadoItem(GuiaRemisionDetalle.EstadoItem.Pendiente);
            
            detalleRepository.save(guiaDetalle);
        }

        return guia;
    }

    // Listar guías con paginación
    public Page<GuiaRemision> listarGuiasPorEmpresa(Integer idEmpresa, int page, int size) {
        return guiaRepository.findByEmpresaIdEmpresaOrderByFechaEmisionDesc(idEmpresa, PageRequest.of(page, size));
    }

    // Buscar guías con paginación
    public Page<GuiaRemision> buscarGuias(Integer idEmpresa, String search, int page, int size) {
        return guiaRepository.searchGuias(idEmpresa, search, PageRequest.of(page, size));
    }

    // Obtener detalles de guía
    public List<GuiaRemisionDetalle> obtenerDetallesGuia(Integer idGuia) {
        return detalleRepository.findByGuiaRemisionIdGuia(idGuia);
    }

    // Anular guía
    @Transactional
    public void anularGuia(Integer idGuia) {
        GuiaRemision guia = guiaRepository.findById(idGuia)
            .orElseThrow(() -> new RuntimeException("Guía no encontrada"));
        
        guia.setEstadoGuia(GuiaRemision.EstadoGuia.Anulada);
        guiaRepository.save(guia);
    }

    
    public GuiaRemision obtenerGuiaParaReporte(Integer idGuia) {
        
            // Usamos el nuevo método findByIdForPdf que usa JOIN FETCH 
        // y carga TODA la información en una SOLA consulta.
        GuiaRemision guia = guiaRepository.findByIdForPdf(idGuia); 

        if (guia == null) {
            throw new RuntimeException("Guía de Remisión con ID " + idGuia + " no encontrada.");
        }
        
        // Todo lo demás se elimina porque ya está precargado
        return guia;
    }
}
