package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
  * Excepcion lanzada cuando hay un conflicto de negocio 
 *
 */
public class ConflictException extends WebContabException {

	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}

	@Override
	public String getErrorCode() {
		return "conflict";
	}

	@Override
	public int getStatusCode() {
		return Status.CONFLICT.getStatusCode();
	}
	
}
