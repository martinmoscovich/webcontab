package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando ocurre algun error en la actualizacion del sistema
 *
 */
public class UpdaterException extends WebContabException {

	private static final long serialVersionUID = 1L;

	public UpdaterException(String message, Throwable e) {
		super(message, e);
	}
	
	public UpdaterException(String message) {
		super(message);
	}

	@Override
	public String getErrorCode() {
		return "updater_error";
	}

	@Override
	public int getStatusCode() {
		return Status.INTERNAL_SERVER_ERROR.getStatusCode();
	}

	

}
