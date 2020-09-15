package com.mmoscovich.webcontab.dto;

import java.time.YearMonth;

import com.mmoscovich.webcontab.exception.InvalidRequestException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO generico que representa un periodo mensual (similar a {@link Periodo}, pero no incluye precision a nivel dias).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoMensual {
	
	private YearMonth desde;
	private YearMonth hasta;
	
	/**
	 * Crea un {@link PeriodoMensual} a partir de un {@link Periodo}, perdiendo la precision de los dias
	 * @param periodo
	 */
	public PeriodoMensual(Periodo periodo) {
		if(periodo.getDesde() != null) this.desde = YearMonth.from(periodo.getDesde());
		if(periodo.getHasta() != null) this.hasta = YearMonth.from(periodo.getHasta());
	}
	
	/**
	 * Valida el periodo.
	 * <p>Chequea que, de estar definidos, "desde" sea anterior o igual a "hasta"</p>
	 * @param requerido si es true, tienen que estar definidas ambas cotas 
	 */
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
