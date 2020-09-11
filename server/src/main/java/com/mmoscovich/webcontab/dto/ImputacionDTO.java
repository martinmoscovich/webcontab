package com.mmoscovich.webcontab.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO de una imputacion
 */
@Data
public class ImputacionDTO {
	private Long id;
	
	/** Datos basicos del asiento */
	private AsientoMinimo asiento;
	private IdModel<Long> cuenta;
	
	@Length(max = 50)
	private String detalle;
	private BigDecimal importe;
	
	// TODO Ver como se usa
	/**
	 * DTO de datos basicos de un asiento
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	public static class AsientoMinimo extends IdFechaModel<Long, LocalDate> {
		private Short numero;

		public AsientoMinimo(Long id, LocalDate fecha, Short numero) {
			super(id, fecha);
			this.numero = numero;
		}
	}

}
