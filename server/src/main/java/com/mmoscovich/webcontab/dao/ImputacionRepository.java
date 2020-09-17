package com.mmoscovich.webcontab.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;

/**
 * DAO de imputaciones
 */
public interface ImputacionRepository extends JpaRepository<Imputacion, Long> {
	
	/** 
	 * Busca las imputaciones de todos los asientos especificados. 
	 * Las ordena por asiento primero y por orden despues.
	 */
	@Query("FROM Imputacion i join fetch i.cuenta WHERE i.asiento IN :asientos order by i.asiento, i.orden")
	List<Imputacion> findByAsientos(Collection<Asiento> asientos);
	
	/** Elimina las imputaciones que tienen los ids especificados */
	@Modifying
	@Query("DELETE FROM Imputacion WHERE id in :ids")
	void deleteByIds(Collection<Long> ids);

	/** Elimina las imputaciones de un asiento */
	@Modifying
	@Query("DELETE FROM Imputacion WHERE asiento = :asiento")
	void deleteByAsiento(Asiento asiento);
	
	/** 
	 * Elimina <b>TODAS</b> las imputaciones de un ejercicio
	 */
	@Modifying
	@Query("DELETE FROM Imputacion WHERE asiento.id IN (SELECT id FROM Asiento WHERE ejercicio = :ejercicio)")
	void deleteByEjercicio(Ejercicio ejercicio);
	
	
	/** Determina si existen imputaciones para una cuenta determinada en <b>cualquier</b> ejercicio */
	@Query("SELECT count(i.id) > 0 " 
			 + "FROM Imputacion i "
			 + "WHERE "
			 + "i.cuenta = :cuenta")
	boolean existsByCuenta(Cuenta cuenta);
	
//	/** Determina si existen imputaciones para una cuenta determinada en un ejercicio especifico. */
//	@Query("SELECT count(i.id) > 0 " 
//			 + "FROM Imputacion i "
//			 + "inner join i.asiento a "
//			 + "WHERE "
//			 + "a.ejercicio = :ejercicio AND "
//			 + "i.cuenta = :cuenta")
//	boolean existsByEjercicioAndCuenta(Ejercicio ejercicio, Cuenta cuenta);
}
