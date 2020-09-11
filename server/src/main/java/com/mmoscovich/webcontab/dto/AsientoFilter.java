package com.mmoscovich.webcontab.dto;

import javax.ws.rs.QueryParam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filtro para busqueda de asientos.
 * <p>Se puede filtrar los asientos por fecha o numero.</p>
 * 
 * @author Martin Moscovich
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AsientoFilter extends Periodo {

	@QueryParam("min") 
	private Short min;
	
	@QueryParam("max") 
	private Short max;
	
	/** Indica si se filtro por fecha */
	public boolean esFiltroFechas() {
		return this.getDesde() != null || this.getHasta() != null;
	}
	
	/** Indica si se filtro por numeros */
	public boolean esFiltroNumeros() {
		return min != null || max != null;
	}
}
