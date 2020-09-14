package com.mmoscovich.webcontab.exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * Excepcion lanzada cuando una fecha no es valida para un ejercicio.
 * <p>Esto puede ocurrir porque no esta dentro del ejercicio 
 * o bien porque es anterior a la fecha confirmada del ejercicio.</p>
 *
 */
public class EjercicioFechaInvalidaException extends ConflictException {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	private static final long serialVersionUID = 1L;

	public EjercicioFechaInvalidaException(String message) {
		super(message);
	}


	public static EjercicioFechaInvalidaException fueraDelEjercicio(Ejercicio ejercicio, LocalDate fecha) {
		return new EjercicioFechaInvalidaException("La fecha " + fecha + " no esta dentro del ejercicio (" + ejercicio.getInicio().format(formatter) + " - " + ejercicio.getFinalizacion().format(formatter) + ")");
	}
	
	public static EjercicioFechaInvalidaException fechaConfirmada (Ejercicio ejercicio, LocalDate fecha) {
		return new EjercicioFechaInvalidaException("La fecha debe ser posterior a la fecha confirmada del ejercicio (" + ejercicio.getFechaConfirmada().format(formatter) + ")");
	}
}
