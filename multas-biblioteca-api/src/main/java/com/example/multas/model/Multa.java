package com.example.multas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "multas")
public class Multa {

    private static final BigDecimal VALOR_POR_DIA =
            new BigDecimal("500");

    private static final BigDecimal TOPE_MAXIMO =
            new BigDecimal("15000");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de estudiante es obligatorio")
    @Column(nullable = false)
    private String estudianteId;

    @NotBlank(message = "El concepto es obligatorio")
    @Column(nullable = false)
    private String concepto;

    @Min(value = 1, message = "Los días de atraso deben ser al menos 1")
    @Column(nullable = false)
    private int diasAtraso;

    @Column(nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMulta estado = EstadoMulta.PENDIENTE;

    @Column(nullable = false)
    private LocalDate fechaGeneracion = LocalDate.now();

    private LocalDate fechaPago;

    private String metodoPago;

    public Multa() {
    }

    public static BigDecimal calcularMonto(int diasAtraso) {
        BigDecimal montoCalculado =
                VALOR_POR_DIA.multiply(BigDecimal.valueOf(diasAtraso));

        return montoCalculado.min(TOPE_MAXIMO);
    }

    public void marcarComoPagada(String metodoPago) {

        if (this.estado == EstadoMulta.PAGADA) {
            throw new MultaYaPagadaException(
                    "La multa " + this.id +
                    " ya fue pagada el " + this.fechaPago
            );
        }

        this.estado = EstadoMulta.PAGADA;
        this.fechaPago = LocalDate.now();
        this.metodoPago = metodoPago;
    }

    public Long getId() {
        return id;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public EstadoMulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMulta estado) {
        this.estado = estado;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}