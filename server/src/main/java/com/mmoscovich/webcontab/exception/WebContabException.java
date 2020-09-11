package com.mmoscovich.webcontab.exception;

/**
 * Excepcion base de la aplicacion.
 *
 */
public abstract class WebContabException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public abstract String getErrorCode();
	public abstract int getStatusCode();

	public WebContabException(String message) {
		super(message);
	}

	public WebContabException(String message, Throwable e) {
		super(message, e);
	}
}
