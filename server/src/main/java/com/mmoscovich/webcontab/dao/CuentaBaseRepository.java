package com.mmoscovich.webcontab.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * DAO de CuentaBase (incluye tanto categorias como cuentas)
 *
 */
public interface CuentaBaseRepository extends JpaRepository<CuentaBase, Long>, CuentaBaseExtraRepository {
	
	/** Obtiene las categorias o cuentas que pertenecen a la especificada */
	List<CuentaBase> findByCategoriaOrderByOrden(Categoria categoria);
	
	/** Busca una categoria o cuenta de una organizacion por su codigo legacy */
	Optional<CuentaBase> findByOrganizacionAndLegacyCodigo(Organizacion org, String legacyCodigo);
}
