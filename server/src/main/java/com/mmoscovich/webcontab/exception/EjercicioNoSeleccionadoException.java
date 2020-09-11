package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando se pide el ejercicio actual pero no se selecciono ninguno
 *
 */
public class EjercicioNoSeleccionadoException extends WebContabException {
	private static final long serialVersionUID = 1L;

	public EjercicioNoSeleccionadoException() {
		super("No se selecciono la organizacion");
	}

	@Override
	public String getErrorCode() {
		return "organizacion_no_seleccionada";
	}

	@Override
	public int getStatusCode() {
		return Status.UNAUTHORIZED.getStatusCode();
	}

}
