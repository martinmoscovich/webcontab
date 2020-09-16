package com.mmoscovich.webcontab.dto.informes;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO que contiene el balance <b>MENSUAL</b> de una cuenta.
 * <p>Incluye datos basicos de la cuenta y su saldo para ese mes.
 * <br>Se utiliza para calculo de inflacion
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceMensualCuenta extends BalanceCuenta {
	private YearMonth mes;

	public BalanceMensualCuenta(Long id, String codigo, String descripcion, Long monedaId, BigDecimal saldo, YearMonth mes) {
		super(id, codigo, descripcion, monedaId, saldo);
		this.mes = mes;
	}
}
