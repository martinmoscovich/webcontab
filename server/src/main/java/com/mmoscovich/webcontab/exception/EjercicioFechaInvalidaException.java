package com.mmoscovich.webcontab.exception;

import java.time.LocalDate;

import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * Excepcion lanzada cuando una fecha no esta dentro del ejercicio
 *
 */
public class EjercicioFechaInvalidaException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public EjercicioFechaInvalidaException(Ejercicio ejercicio, LocalDate fecha) {
		super("La fecha " + fecha + " no esta dentro del ejercicio (" + ejercicio.getInicio() + " - " + ejercicio.getFinalizacion() + ")");
	}
	
}
