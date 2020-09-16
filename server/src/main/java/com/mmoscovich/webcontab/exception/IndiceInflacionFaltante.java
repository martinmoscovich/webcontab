package com.mmoscovich.webcontab.exception;

import java.time.YearMonth;

/**
 * Excepcion lanzada cuando falta el indice de inflacion de una moneda y mes.
 *
 */
public class IndiceInflacionFaltante extends ConflictException {

	private static final long serialVersionUID = 1L;

	public IndiceInflacionFaltante(String descripcionCuenta, YearMonth mes) {
		super("No se definio indice para la moneda de la cuenta " + descripcionCuenta + " en el mes " + mes);
	}
	
	public IndiceInflacionFaltante(String message) {
		super(message);
	}
}
