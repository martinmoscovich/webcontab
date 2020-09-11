package com.mmoscovich.webcontab.dto;

import javax.ws.rs.QueryParam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filtro para el informe Balance (saldos por cuenta)
 * 
 * @author Martin Moscovich
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceFilter extends Periodo {

	/** Indica si incluir cuentas con saldo cero */
	@QueryParam("cero")
	private boolean includeCuentasEnCero;
	
	/** Indica si solo se debe incluir descendiente de una categoria determinada */
	@QueryParam("categoria") 
	private Long categoriaId;
}
