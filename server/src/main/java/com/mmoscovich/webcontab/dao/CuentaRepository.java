package com.mmoscovich.webcontab.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * DAO de Cuentas
 *
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
	
	/** Obtiene una lista de cuentas de una organizacion por ids */
	@Query("FROM Cuenta WHERE organizacion = :org AND id IN :ids")
	List<Cuenta> findByIds(Organizacion org, List<Long> ids);
	
	/** 
	 * Obtiene todas las cuentas de la organizacion que balancean resultados (una por moneda).
	 * <br>Se usa para la refundicion de cuentas de resultados. 
	 */
	@Query("FROM Cuenta WHERE organizacion = :org AND balanceaResultados = true")
	List<Cuenta> findCuentasQueBalanceanResultados(Organizacion org);
	
	/** 
	 * Obtiene todas las cuentas de la organizacion que balancean las ajustables por inflacion (una por moneda).
	 * <br>Se usa para el ajuste por inflacion. 
	 */
	@Query("FROM Cuenta WHERE organizacion = :org AND balanceaAjustables = true")
	List<Cuenta> findCuentasQueBalanceanAjustables(Organizacion org);
	
	/**
	 * Borra todas las cuentas de una organizacion
	 */
	@Modifying
	@Query("DELETE FROM Cuenta WHERE organizacion = :org")
	void deleteByOrganizacion(Organizacion org);
	
	/**
	 * Desactiva el flag "ajustable" y el flag "balanceaAjustables" de <b>TODAS</b>
	 * las cuentas asociadas a una moneda determinada.
	 * <p>
	 * Util cuando una moneda deja de ser "ajustable".
	 * <br>Esto afecta a las cuentas de <b>TODAS</b> las organizaciones.
	 * </p>
	 * @param moneda
	 */
	@Modifying
	@Query("UPDATE Cuenta SET ajustable = false, balanceaAjustables = false WHERE moneda = :moneda")
	void desactivarCuentasAjustablesYBalanceadora(Moneda moneda);
}
