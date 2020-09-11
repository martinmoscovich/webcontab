package com.mmoscovich.webcontab.dao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * DAO de Asientos 
 */
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

	/**
	 * Borra multiples asientos de un ejercicio por id 
	 * @param ejercicio ejercicio al que corresponden
	 * @param ids lista de ids a borrar
	 */
	@Modifying
	@Query("DELETE FROM Asiento WHERE ejercicio = :ejercicio AND id in :ids")
	void deleteByIds(Ejercicio ejercicio, Collection<Long> ids);
	
	/**
	 * Borra todos los asientos de un ejercicio
	 */
	@Modifying
	@Query("DELETE FROM Asiento WHERE ejercicio = :ejercicio")
	void deleteByEjercicio(Ejercicio ejercicio);

	/**
	 * Busca un asiento por id y trae sus imputaciones
	 */
	@Query("FROM Asiento a left join fetch a.imputaciones WHERE a.id = :id")
	Optional<Asiento> findByIdWithImputaciones(Long id);
	
	/**
	 * Busca un asiento por ejercicio y numero
	 */
	@Query("FROM Asiento WHERE ejercicio = :ejercicio AND numero = :numero")
	Optional<Asiento> findByNumero(Ejercicio ejercicio, Integer numero);
	
	/**
	 * Devuelve una pagina de asientos de un ejercicio en un determinado periodo
	 */
	@Query("from Asiento a WHERE a.ejercicio = :ejercicio AND a.fecha BETWEEN :desde AND :hasta ORDER BY a.fecha, a.numero")
	Page<Asiento> findByPeriodo(Ejercicio ejercicio, LocalDate desde, LocalDate hasta, Pageable page);
	
	/**
	 * Devuelve una pagina de asientos de un ejercicio dentro un determinado rango de numeros
	 */
	@Query("from Asiento a WHERE a.ejercicio = :ejercicio AND a.numero BETWEEN :min AND :max ORDER BY a.fecha, a.numero")
	Page<Asiento> findByNumeros(Ejercicio ejercicio, Short min, Short max, Pageable page);
	
	/**
	 * Obtiene el ultimo numero de asiento para un determinado ejercicio.
	 * <br>Si no hay asientos, devuelve 0.
	 */
	@Query("SELECT coalesce(max(a.numero), 0) from Asiento a WHERE a.ejercicio = :ejercicio")
	Short getUltimoNumeroAsiento(Ejercicio ejercicio);
	
	/**
	 * Obtiene la ultima fecha de asiento cargada en el ejercicio.
	 * <p>O sea, la fecha mas tardia para un asiento.
	 * <br>No necesariamente es el ultimo asiento cargado, ya que se pueden cargar desordenados.</p>
	 */
	@Query("SELECT max(fecha) from Asiento WHERE ejercicio = :ejercicio")
	Optional<LocalDate> getUltimaFechaAsiento(Ejercicio ejercicio);
	
	// REPORTES 
	
	// DIARIO
	/**
	 * Obtiene un stream de <b>TODOS</b> los asientos de un ejercicio en un determinado periodo y sus imputaciones. 
	 * <br>Se usa para exportar el Diario.
	 */
	@Query("from Asiento a join fetch a.imputaciones i join fetch i.cuenta WHERE a.ejercicio = :ejercicio AND a.fecha BETWEEN :desde AND :hasta ORDER BY a.fecha, a.numero")
	Stream<Asiento> findByPeriodoReport(Ejercicio ejercicio, LocalDate desde, LocalDate hasta);

	/**
	 * Obtiene un stream de <b>TODOS</b> los asientos de un ejercicio con numeracion dentro de un determinado rango y sus imputaciones.
	 * <br>Se usa para exportar el Diario. 
	 */
	@Query("from Asiento a join fetch a.imputaciones i join fetch i.cuenta WHERE a.ejercicio = :ejercicio AND a.numero BETWEEN :min AND :max ORDER BY a.fecha, a.numero")
	Stream<Asiento> findByNumerosReport(Ejercicio ejercicio, Short min, Short max);
	
//
//	@Query("SELECT count(i.id) > 0 " 
//		 + "FROM Imputacion i "
//		 + "inner join i.asiento a "
//		 + "inner join i.cuenta c "
//		 + "WHERE "
//		 + "a.ejercicio = :ejercicio AND "
//		 + "(c.categoria = :categoria OR c.codigo LIKE :codigo)")
//	boolean existsByAsientoEjercicioAndCategoria(Ejercicio ejercicio, Categoria categoria, String codigo);

}
