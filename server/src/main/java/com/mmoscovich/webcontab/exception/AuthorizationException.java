package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando hay un error de autorizacion
 *
 */
public class AuthorizationException extends WebContabException {
	private static final long serialVersionUID = 1L;

	public AuthorizationException(String message) {
		super(message);
	}

	@Override
	public String getErrorCode() {
		return "access_no_permitido";
	}

	@Override
	public int getStatusCode() {
		return Status.FORBIDDEN.getStatusCode();
	}

}
