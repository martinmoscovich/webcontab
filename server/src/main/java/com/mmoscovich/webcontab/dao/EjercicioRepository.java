package com.mmoscovich.webcontab.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * DAO de Ejercicios
 */
public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

	/** Busca los ejercicios de una organizacion */
	List<Ejercicio> findByOrganizacion(Organizacion organizacion);
	
	/** 
	 * Obtiene el ejercicio de la organizacion que termina ultimo, si existe.
	 * Se filtra dos veces por la fecha, para obtener la fecha max y luego para traer ejercicio solo de ese ejercicio,
	 * ya que si un ejercicio de otra org tambien finaliza en esa fecha, podria traerlo 
	 */
	@Query("FROM Ejercicio WHERE organizacion = :org AND finalizacion = (SELECT MAX(e.finalizacion) FROM Ejercicio e WHERE e.organizacion = :org)")
	Optional<Ejercicio> findEjercicioQueFinalizaUltimo(Organizacion org);

	/** Busca los ejercicios de la organizacion que se solapan con las fechas especificadas */
	@Query("FROM Ejercicio WHERE organizacion = :org AND inicio <= :fin2 AND :inicio2 <= finalizacion")
	List<Ejercicio> findEjerciciosQueSolapan(Organizacion org, LocalDate inicio2, LocalDate fin2);
}
