package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando hay un error inesperado en el servidor.
 *
 */
public class ServerException extends WebContabException {

	private static final long serialVersionUID = 1L;
	
	public ServerException(String message) {
		super(message);
	}
	
	public ServerException(String message, Throwable e) {
		super(message, e);
	}
	
	@Override
	public String getErrorCode() {
		return "server_error";
	}

	@Override
	public int getStatusCode() {
		return Status.INTERNAL_SERVER_ERROR.getStatusCode();
	}
}
