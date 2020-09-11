package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando el request no es valido, en general por errores de validacion.
 *
 */
public class InvalidRequestException extends WebContabException {
	private static final long serialVersionUID = 1L;

	public InvalidRequestException(String message) {
		super(message);
	}
	
	public InvalidRequestException(String message, Throwable e) {
		super(message,  e);
	}

	@Override
	public String getErrorCode() {
		return "invalid_request";
	}

	@Override
	public int getStatusCode() {
		return Status.BAD_REQUEST.getStatusCode();
	}
	
}
