package com.mmoscovich.webcontab.dto.informes;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO que contiene el balance de una cuenta.
 * <p>Incluye datos basicos de la cuenta y su saldo.
 * <br>Se utiliza en el balance (se devuelve una lista de estos items).
 * </p>
 */
@Data
public class BalanceCuenta {
	private Long id;
	private BigDecimal saldo;
	
	private String codigo;
	private String descripcion;
	private Long monedaId;
	
	public BalanceCuenta(Long id, String codigo, String descripcion, Long monedaId, BigDecimal saldo) {
		this.id = id;
		this.saldo = saldo;
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.monedaId = monedaId;
	}
}
