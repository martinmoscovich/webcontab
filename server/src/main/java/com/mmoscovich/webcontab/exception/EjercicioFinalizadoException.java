package com.mmoscovich.webcontab.exception;

import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * Excepcion lanzada cuando se quiere modificar un ejercicio finalizado.
 *
 */
public class EjercicioFinalizadoException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public EjercicioFinalizadoException(Ejercicio ejercicio) {
		super("El ejercicio esta cerrado. No se pueden hacer modificaciones");
	}
	
}
