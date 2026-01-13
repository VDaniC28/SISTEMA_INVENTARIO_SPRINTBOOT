package com.example.appsistema.service;

import com.example.appsistema.model.GuiaRemision;
import com.example.appsistema.model.Empresa;
import com.example.appsistema.model.Proveedor;
import com.example.appsistema.model.Cliente;
import com.example.appsistema.model.Almacen;
// Asumo que GuiaRemisionDetalle tiene referencias a Producto y Suministro
// y que todas las clases de modelo requeridas están disponibles.

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal; // Para manejar el pesoTotal

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    // Se mantiene ResourceLoader para cargar la plantilla HTML
    @Autowired
    private ResourceLoader resourceLoader;
    
    // **NOTA DE CORRECCIÓN:** Se eliminan las inyecciones de JpaRepository (EmpresaRepository, etc.)
    // El modelo GuiaRemision ya provee los objetos relacionados (guia.getEmpresa(), guia.getOrdenCompra(), etc.)

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera un PDF detallado para la Guía de Remisión.
     * @param guia La entidad GuiaRemision completa con sus relaciones cargadas.
     * @return ByteArrayInputStream del PDF generado.
     */
    public ByteArrayInputStream generarPdfGuia(GuiaRemision guia) throws Exception {
        
        // 1. Obtener la plantilla HTML (asumiendo ruta: classpath:pdf/guia_remision_template.html)
        String htmlTemplate = loadHtmlTemplate("templates/pdf/guia_remision_template.html");
        
        // 2. Llenar la plantilla con datos
        String htmlContent = fillTemplate(htmlTemplate, guia);
        
        // 3. Generar PDF con Flying Saucer
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        
        // Configurar la base URL para la carga de recursos estáticos (CSS/Imágenes si existieran)
        String baseUrl = resourceLoader.getResource("classpath:/static/").getURL().toString();
        renderer.setDocumentFromString(htmlContent, baseUrl); 
        
        renderer.layout();
        renderer.createPDF(os);
        
        return new ByteArrayInputStream(os.toByteArray());
    }

    private String loadHtmlTemplate(String templatePath) throws IOException {
        try (var is = resourceLoader.getResource("classpath:" + templatePath).getInputStream()) {
             return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Plantilla PDF no encontrada en 'classpath:" + templatePath + "'", e);
        }
    }

    private String fillTemplate(String template, GuiaRemision guia) {
        
        Empresa empresa = guia.getEmpresa();
        
        // Inicializar variables de origen/destino
        // Se asume que el almacén de origen siempre es la empresa/un almacén propio
        String origenInfo = guia.getAlmacenOrigen() != null ? 
                            (guia.getAlmacenOrigen().getNombreAlmacen() + " (Almacén Propio)") : 
                            (empresa != null ? empresa.getNombre() + " (Sede Principal)" : "N/A");
        
        String destinoInfo = "N/A";
        String rucDestino = "N/A";
        String direccionDestino = "N/A";
        
        // Determinar Origen y Destino basado en TipoGuia
        switch (guia.getTipoGuia()) {
            case Salida: // Venta/Devolución a Cliente
                if (guia.getSalidaInventario() != null && guia.getSalidaInventario().getCliente() != null) {
                    Cliente cliente = guia.getSalidaInventario().getCliente();
                    destinoInfo = cliente.getNombreCliente() != null ? cliente.getNombreCliente() : (cliente.getNombreComercial() != null ? cliente.getNombreComercial() : "Cliente Vario");
                    rucDestino = cliente.getNumeroDocumento();
                    direccionDestino = cliente.getDireccion();
                }
                break;
            case Entrada: // Compra de Proveedor
                if (guia.getOrdenCompra() != null && guia.getOrdenCompra().getProveedor() != null) {
                    Proveedor proveedor = guia.getOrdenCompra().getProveedor();
                    // En una entrada, el proveedor es el punto de partida (Origen)
                    origenInfo = proveedor.getNombreProveedor() + " (Proveedor - RUC: " + proveedor.getNumeroDocumento() + ")"; 
                    
                    Almacen almacenDestino = guia.getAlmacenDestino();
                    destinoInfo = almacenDestino != null ? almacenDestino.getNombreAlmacen() + " (Almacén Propio)" : "Almacén Principal";
                    rucDestino = empresa != null ? empresa.getRuc() : "N/A"; // El RUC del receptor es el de la empresa
                    direccionDestino = almacenDestino != null ? almacenDestino.getUbicacion() : (empresa != null ? empresa.getDireccion() : "N/A");
                }
                break;
            case Transferencia:
                // El origen ya se define arriba (Almacen Origen)
                Almacen almacenDestino = guia.getAlmacenDestino();
                destinoInfo = almacenDestino != null ? almacenDestino.getNombreAlmacen() + " (Almacén Propio)" : "Almacén Destino No Especificado";
                rucDestino = empresa != null ? empresa.getRuc() : "N/A"; // RUC de la propia empresa (Transferencia interna)
                direccionDestino = almacenDestino != null ? almacenDestino.getUbicacion() : (empresa != null ? empresa.getDireccion() : "N/A");
                break;
        }

        // *****************************************************************
        // 1. Reemplazo de marcadores para la cabecera
        // *****************************************************************
        String filledTemplate = template
            // Datos de la Empresa
            .replace("${EMPRESA_NOMBRE}", empresa != null ? empresa.getNombre() : "Empresa Desconocida")
            .replace("${EMPRESA_RUC}", empresa != null ? empresa.getRuc() : "N/A")
            .replace("${EMPRESA_DIRECCION}", empresa != null ? empresa.getDireccion() : "N/A")
            
            // Datos de la Guía
            .replace("${GUIA_NUMERO_COMPLETO}", guia.getSerieGuia() + "-" + guia.getCorrelativoGuia())
            .replace("${GUIA_FECHA_EMISION}", guia.getFechaEmision() != null ? guia.getFechaEmision().format(DATE_FORMAT) : "N/A")
            .replace("${GUIA_TIPO}", guia.getTipoGuia().toString())
            .replace("${GUIA_MOTIVO}", guia.getMotivoTraslado().toString())
            
            // Datos de Origen / Destino
            .replace("${ORIGEN_INFO}", origenInfo)
            .replace("${DESTINO_INFO}", destinoInfo)
            .replace("${DESTINO_RUC}", rucDestino)
            .replace("${DESTINO_DIRECCION}", direccionDestino)
            
            // Datos de Transporte
            .replace("${TRANSPORTISTA}", guia.getTransportista() != null ? guia.getTransportista() : "Propio")
            .replace("${VEHICULO_PLACA}", guia.getPlacaVehiculo() != null ? guia.getPlacaVehiculo() : "N/A")
            .replace("${LICENCIA_CONDUCIR}", guia.getLicenciaConducir() != null ? guia.getLicenciaConducir() : "N/A")
            .replace("${PESO_TOTAL}", String.format("%.2f kg", guia.getPesoTotal() != null ? guia.getPesoTotal() : BigDecimal.ZERO))
            .replace("${NUMERO_BULTOS}", guia.getNumeroPackages() != null ? guia.getNumeroPackages().toString() : "1")
            .replace("${FECHA_TRASLADO}", guia.getFechaTraslado() != null ? guia.getFechaTraslado().format(DATE_FORMAT) : "N/A")
            .replace("${OBSERVACIONES}", guia.getObservaciones() != null ? guia.getObservaciones() : "Ninguna")
            .replace("${GUIA_ESTADO}", guia.getEstadoGuia().toString());

        // *****************************************************************
        // 2. Generación de las filas de detalle
        // *****************************************************************
        String detalleRows = guia.getDetalles().stream()
            .map(detalle -> {
                String descripcionItem = "";
                String codigoItem = "";
                String loteItem = detalle.getLote() != null ? detalle.getLote() : "-";
                
                // **NOTA:** Asumo que GuiaRemisionDetalle tiene las referencias a Producto/Suministro
                if (detalle.getProducto() != null) {
                    descripcionItem = detalle.getProducto().getNombreProducto();
                    codigoItem = detalle.getProducto().getSerialProducto();
                } else if (detalle.getSuministro() != null) {
                    descripcionItem = detalle.getSuministro().getNombreSuministro();
                    codigoItem = detalle.getSuministro().getCodigoSuministro();
                }

                return String.format(
                    "<tr>" +
                    "<td class='center-align'>%d</td>" +
                    "<td class='center-align'>%s</td>" +
                    "<td>%s</td>" +
                    "<td class='right-align'>%d</td>" +
                    "<td class='center-align'>%s</td>" +
                    "<td class='center-align'>%s</td>" +
                    "</tr>",
                    detalle.getNumeroItem(),
                    codigoItem.isEmpty() ? "N/A" : codigoItem,
                    descripcionItem.isEmpty() ? "Ítem Desconocido" : descripcionItem,
                    detalle.getCantidad(),
                    detalle.getUnidadMedida(),
                    loteItem
                );
            })
            .collect(Collectors.joining(""));

        // 3. Reemplazar el marcador de la tabla de detalles y fecha de generación
        return filledTemplate
                .replace("${DETALLE_FILAS}", detalleRows)
                .replace("${FECHA_ACCION_ACTUAL}", java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}