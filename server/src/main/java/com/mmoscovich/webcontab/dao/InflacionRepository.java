package com.mmoscovich.webcontab.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.InflacionMes;
import com.mmoscovich.webcontab.model.Moneda;

/**
 * Dao de indices de Inflacion
 */
public interface InflacionRepository extends JpaRepository<InflacionMes, Long> {

	/** Busca los indices para una moneda en un determinado periodo */
	@Query("FROM InflacionMes WHERE moneda = :moneda AND mes BETWEEN :desde AND :hasta ORDER BY mes")
	List<InflacionMes> findByMonedaAndPeriodo(Moneda moneda, LocalDate desde, LocalDate hasta);
	
	/** 
	 * Borra todos los indices de una determinada moneda.
	 * <p>Util cuando una moneda deja de ser "ajustable".</p> 
	 */
	@Modifying
	@Query("DELETE FROM InflacionMes WHERE moneda = :moneda")
	void deleteByMoneda(Moneda moneda);
}
