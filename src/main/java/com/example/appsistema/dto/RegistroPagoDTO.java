package com.example.appsistema.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPagoDTO {

    private Integer idCliente;
    private Integer idSalida;
    private BigDecimal montoAbono;
    private Integer idUsuario;
    
}
