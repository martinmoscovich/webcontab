package com.mmoscovich.webcontab.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Moneda;

/**
 * DAO de Moneda
 */
public interface MonedaRepository extends JpaRepository<Moneda, Long> {

	/** Actualiza la moneda default para que no lo sea */
	@Modifying
	@Query("UPDATE Moneda SET isDefault = false WHERE isDefault = true")
	void removeDefault();
}
