package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando se pide la organizacion actual pero no se selecciono ninguna. 
 */
public class OrganizacionNoSeleccionadaException extends WebContabException {
	private static final long serialVersionUID = 1L;

	public OrganizacionNoSeleccionadaException() {
		super("No se selecciono el ejercicio");
	}

	@Override
	public String getErrorCode() {
		return "ejercicio_no_seleccionado";
	}

	@Override
	public int getStatusCode() {
		return Status.UNAUTHORIZED.getStatusCode();
	}

}
