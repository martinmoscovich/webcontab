package com.mmoscovich.webcontab.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.util.CreateValidation;

import lombok.Data;

/**
 * DTO generico que representa un periodo (con fecha de inicio y fin)
 * @author Martin
 *
 */
@Data
public class Periodo {
	
	@NotNull(groups = CreateValidation.class)
	@QueryParam("desde") 
	private LocalDate desde;
	
	@NotNull(groups = CreateValidation.class)
	@QueryParam("hasta") 
	private LocalDate hasta;
	
	
	public void validar(boolean requerido) {
		if(desde != null && hasta != null) {
			if(desde.isAfter(hasta)) throw new InvalidRequestException("La fecha inicial no puede ser posterior a la final");
		} else {
			if(requerido) throw new InvalidRequestException("Desde ingresar tanto la fecha inicial como la final");
		}
	}
	
	public String toString() {
		if(this.getDesde() != null && this.getHasta() != null) return this.getDesde() + " - " + this.getHasta();
		if(this.getDesde() != null) return this.getDesde() + " - ?";
		if(this.getHasta() != null) return "? - " + this.getHasta();
		
		return "";
	}
}
