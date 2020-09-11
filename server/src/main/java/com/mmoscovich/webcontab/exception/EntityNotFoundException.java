package com.mmoscovich.webcontab.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Excepcion lanzada cuando no se encuentra una entidad pedida
 *
 */
public class EntityNotFoundException extends WebContabException {

	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String message) {
		super(message);
	}
	
	public EntityNotFoundException(Class<?> cls, Long id) {
		super("No existe " + cls.getSimpleName() + " con id " + id);
	}
	
	public EntityNotFoundException(Class<?> cls, Object id) {
		super("No existe " + cls.getSimpleName() + " con id " + id);
	}

	@Override
	public String getErrorCode() {
		return "not_found";
	}

	@Override
	public int getStatusCode() {
		return Status.NOT_FOUND.getStatusCode();
	}
	
}
