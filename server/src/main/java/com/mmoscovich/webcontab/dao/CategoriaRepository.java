package com.mmoscovich.webcontab.dao;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * DAO de categorias
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
	
	/**
	 * Busca todas las categorias de una organizacion (y las cachea)
	 */
	@QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value ="true") })
	List<Categoria> findByOrganizacion(Organizacion org);
	
	/**
	 * Obtiene las categorias de resultados de la organizacion (en general INGRESOS Y EGRESOS)
	 */
	@Query("FROM Categoria WHERE organizacion = :org AND resultado = true")
	List<Categoria> findCategoriasDeResultados(Organizacion org);
	
	/**
	 * Limpia la jerarquia para poder borrar las categorias sin problemas de contraints
	 */
	@Modifying
	@Query("UPDATE Categoria c SET c.categoria = NULL WHERE c.organizacion = :org")
	void removeHierarchyByOrganizacion(Organizacion org);
	
	/**
	 * Borra todas las categorias de una organizacion
	 */
	@Modifying
	@Query("DELETE FROM Categoria WHERE organizacion = :org")
	void deleteByOrganizacion(Organizacion org);
	
	// REPORTES 
	
	/**
	 * Obtiene un stream de <b>TODAS</b> las categorias y cuentas de la organizacion.
	 * <br>Se usa para exportar el plan de cuentas.
	 */
	@Query("FROM CuentaBase WHERE organizacion = :org ORDER BY orden")
	Stream<CuentaBase> getPlan(Organizacion org);
	
	/**
	 * Obtiene un stream de <b>TODAS</b> las categorias y cuentas de la organizacion a partir de la categoria especificada en el codigo.
	 * <br>Se usa para exportar el plan de cuentas.
	 */
	@Query("FROM CuentaBase WHERE organizacion = :org AND codigo like :codigo ORDER BY orden")
	Stream<CuentaBase> getPlan(Organizacion org, String codigo);
}
