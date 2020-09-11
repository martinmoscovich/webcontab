package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando hay un error de autenticacion
 *
 */
public class AuthenticationException extends WebContabException {
	private static final long serialVersionUID = 1L;

	public AuthenticationException(String message) {
		super(message);
	}

	@Override
	public String getErrorCode() {
		return "usuario_no_autenticado";
	}

	@Override
	public int getStatusCode() {
		return Status.UNAUTHORIZED.getStatusCode();
	}

}
