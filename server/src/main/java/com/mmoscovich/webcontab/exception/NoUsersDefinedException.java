package com.mmoscovich.webcontab.exception;

/**
 * Excepcion lanzada la primera vez que se ejecuta el sistema y no hay usuarios en la base de datos.
 *
 */
public class NoUsersDefinedException extends ConflictException {

	private static final long serialVersionUID = 7900275337175797560L;

	public NoUsersDefinedException() {
		super("No hay usuarios");
	}

	@Override
	public String getErrorCode() {
		return "no_users";
	}
}
