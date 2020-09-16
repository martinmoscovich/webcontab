package com.mmoscovich.webcontab.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mmoscovich.webcontab.dto.informes.BalanceMensualCuenta;
import com.mmoscovich.webcontab.exception.IndiceInflacionFaltante;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.InflacionMes;

import lombok.Getter;

/**
 * Clase encargada de calcular los ajustes por inflacion de las distintas cuentas en base a los saldos mensuales. 
 */
public class InflacionCalculator {
	
	/** Mapa que contiene como clave el mes y la moneda y como valor el indice de inflacion correspondiente */
	private Map<String, BigDecimal> indices = new HashMap<>();
	
	/** Lista que se usa para conservar el orden de las cuentas */
	@Getter
	private List<Long> cuentasIds = new ArrayList<>();
	
	/** 
	 * Mapa que contiene como clave el id de la cuenta 
	 * y como valor acumula los saldos normales y ajustados para cada cuenta en todo el ejercicio (sumando cada mes) 
	 */
	private Map<Long, SaldoCuenta> saldos = new HashMap<>();
	
	/** Sin que representa el mes final en formato "yyyy-mm". Se usa como clave en el mapa */
	private String mesFinalizacion;
	
	/** Incializa un Calculador en base al ejercicio y la lista de indices de inflacion */
	public InflacionCalculator(Ejercicio ejercicio, List<InflacionMes> items) {
		// Se obtiene el string "yyyy-mm" del mes de finalizacion del ejercicio
		this.mesFinalizacion = YearMonth.from(ejercicio.getFinalizacion()).toString();
		
		// Por cada indice, se incluye en el mapa usando como clave "yyyy-mm|id_moneda".
		for(InflacionMes indice : items) {
			indices.put(indice.getYearMonth().toString() + "|" + indice.getMoneda().getId(), indice.getIndice());
		}
	}
	
	/**
	 * Devuelve la diferencia entre el saldo ajustado por inflacion y el normal para una cuenta.
	 * <p>Estos saldos son los acumulados para todo el ejercicio</p>
	 * @param cuentaId
	 * @return
	 */
	public BigDecimal getDiferenciaAjuste(Long cuentaId) {
		SaldoCuenta saldo = this.saldos.get(cuentaId);
		
		// Se devuelve la diferencia usando solo 2 decimales
		return saldo.saldoAjustado.subtract(saldo.saldoNormal).setScale(2, RoundingMode.HALF_DOWN);
	}
	
	/** 
	 * Acumula para una cuenta el valor del saldo normal y ajustado en un determinado mes.
	 * @param balance saldo de una cuenta en un determinado mes
	 * 
	 * @throws IndiceInflacionFaltante si no se cargo el indice de inflacion para ese mes y moneda
	 */
	public void add(BalanceMensualCuenta balance) throws IndiceInflacionFaltante {
		final Long cuentaId = balance.getId();
		
		// Se agrega la cuenta a la lista ordenada, si aun no se agrego
		if(!this.cuentasIds.contains(cuentaId)) cuentasIds.add(cuentaId);
		
		// Se obtiene o crea el acumulado para esa cuenta
		SaldoCuenta saldo = saldos.computeIfAbsent(cuentaId, id -> new SaldoCuenta());

		// Se obtiene el saldo del mes (se ponen 4 decimales para los calculos)
		BigDecimal saldoNormal = balance.getSaldo().setScale(4);
		
		// Se obtiene el saldo ajustado haciendo ("saldo del mes" / "indice del mes") * "indice del mes final del ejercicio"
		BigDecimal saldoAjustado = saldoNormal.divide(getIndiceOrThrow(balance), 4, RoundingMode.DOWN)
											  .multiply(getIndiceFinalOrThrow(balance))
											  .setScale(4, RoundingMode.DOWN);
		
		// Se acumulan a los saldos de meses anteriores para la misma cuenta
		saldo.saldoNormal = saldo.saldoNormal.add(saldoNormal).setScale(4, RoundingMode.DOWN);
		saldo.saldoAjustado = saldo.saldoAjustado.add(saldoAjustado).setScale(4, RoundingMode.DOWN);
	}
	
	/** 
	 * Obtiene el indice de inflacion para un item de balance.
	 * <br>Se obtiene a partir del mes y la cuenta (por lo tanto la moneda) del item. 
	 * 
	 * @param balance
	 * @return
	 * @throws IndiceInflacionFaltante si no se cargo el indice de inflacion para ese mes y la moneda de la cuenta
	 */
	private BigDecimal getIndiceOrThrow(BalanceMensualCuenta balance) throws IndiceInflacionFaltante {
		// Se busca con la key mes-moneda
		BigDecimal indice = indices.get(balance.getMes().toString() + "|" + balance.getMonedaId());
		if(indice == null) throw new IndiceInflacionFaltante(balance.getDescripcion(), balance.getMes());
		return indice.setScale(4, RoundingMode.DOWN);
	}
	
	/**
	 * Obtiene el indice de inflacion del mes final de ejercicio para una item de balance (usando su cuenta).
	 * @param balance
	 * @return
	 * @throws IndiceInflacionFaltante si no se cargo el indice de inflacion del mes final para la moneda de la cuenta
	 */
	private BigDecimal getIndiceFinalOrThrow(BalanceMensualCuenta balance) throws IndiceInflacionFaltante {
		// Se busca con la key mes-moneda, usando el mes de finalizacion
		BigDecimal indice = indices.get(this.mesFinalizacion + "|" + balance.getMonedaId());
		if(indice == null) throw new IndiceInflacionFaltante("No se definio el indice final del ejercicio para la moneda de la cuenta " + balance.getDescripcion());
		return indice;
	}

	/**
	 * Clase usada como estructura para guardar los dos saldos que nos interesan: el normal y el ajustado
	 */
	private static class SaldoCuenta {
		private BigDecimal saldoNormal = BigDecimal.ZERO;
		private BigDecimal saldoAjustado = BigDecimal.ZERO;
	}
}
