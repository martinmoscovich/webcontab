package com.mmoscovich.webcontab.exception;

import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * Excepcion lanzada cuando se quiere deshabilitar una categoria que tienen hijos o una cuenta que tiene imputaciones en el ejercicio
 *
 */
public class CuentaUtilizadaException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public CuentaUtilizadaException(Cuenta cuenta) {
		super("La cuenta " + cuenta.getDescripcion() + " tiene imputaciones");
	}
	
	public CuentaUtilizadaException(Cuenta cuenta, Ejercicio ejercicio) {
		super("La cuenta " + cuenta.getDescripcion() + " tiene imputaciones en el ejercicio [" + ejercicio.getInicio() + " - " + ejercicio.getFinalizacion() + "]");
	}
	
	public CuentaUtilizadaException(Categoria categoria) {
		super("La categoria " + categoria.getDescripcion() + " tiene subcategorias o subcuentas");
	}
	
}
