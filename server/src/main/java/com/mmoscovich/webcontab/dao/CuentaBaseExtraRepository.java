package com.mmoscovich.webcontab.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * Interfaz que "aumenta" el repositorio de CuentaBase con queries mas complejas.
 *
 */
public interface CuentaBaseExtraRepository {

	/**
	 * Determina si existe una cuenta o categoria con la descripcion especificada y el mismo padre u, opcionalmente, con el mismo codigo.
	 * @param catOCuenta cuenta cuyo duplicado se busca
	 * @param newDescripcion descripcion a buscar.
	 * @param checkCodigo si es true, tambien busca si ya existe el codigo de la cuenta o categoria.
	 * @return un id si existe o un opcional vacio si no.
	 */
	Optional<Long> findDuplicado(CuentaBase catOCuenta, String newDescripcion, boolean checkCodigo);
	
	/**
	 * Busca una pagina de cuentas dentro de la organizacion que cumplan <b>al menos una</b> de las siguientes condiciones:<br>
	 * <ul>
	 * <li>Incluyan el texto en la descripcion</li> 
	 * <li>Incluyan el texto en el alias</li>
	 * <li>Su codigo <b>empiece</b> con el texto</li>
	 * <li>Son descendientes de las categorias indicadas.</li>
	 * </ul>   
	 */
	Slice<Cuenta> searchCuentasByText(Organizacion org, String query, List<Categoria> categorias, Pageable pageParams);
	
	/**
	 * Busca una pagina de categorias o cuentas dentro de la organizacion que cumplan <b>al menos una</b> de las siguientes condiciones:<br>
	 * <ul>
	 * <li>Incluyan el texto en la descripcion</li> 
	 * <li>Incluyan el texto en el alias</li>
	 * <li>Su codigo <b>empiece</b> con el texto</li>
	 * <li>Son descendientes de las categorias indicadas.</li>
	 * </ul>   
	 */
	Slice<CuentaBase> searchAllByText(Organizacion org, String query, List<Categoria> categorias, Pageable pageParams);
	
	/** 
	 * Obtiene todas las cuentas de la organizacion descendientes de las categorias especificadas.
	 */
	List<Cuenta> getCuentasDescendientes(Organizacion org, List<Categoria> categorias);
}
