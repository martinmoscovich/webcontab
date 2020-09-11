package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando se pide una accion al Updater pero este se encuentra ejecutando una accion previa. 
 */
public class UpdaterBusyException extends UpdaterException {

	private static final long serialVersionUID = 1L;

	public UpdaterBusyException(String message) {
		super(message);
	}
	
	@Override
	public String getErrorCode() {
		return "updater_busy";
	}

	@Override
	public int getStatusCode() {
		return Status.CONFLICT.getStatusCode();
	}
	
}
