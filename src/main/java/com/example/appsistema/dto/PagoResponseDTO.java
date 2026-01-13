package com.example.appsistema.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {
    private Integer idPago;
    private BigDecimal montoAbono;
    private LocalDateTime fechaAbono;
    private String nombreUsuario;
    private BigDecimal nuevoSaldo;
}
