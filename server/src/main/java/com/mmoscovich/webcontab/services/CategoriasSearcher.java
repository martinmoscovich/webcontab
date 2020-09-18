package com.mmoscovich.webcontab.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mmoscovich.webcontab.model.Categoria;

/**
 * Clase que permite hacer busquedas de categorias por texto libre en memoria.
 * <p>Filtrar y ordena los resultados.</p>
 *
 */
public class CategoriasSearcher implements Predicate<Categoria>, Comparator<Categoria> {
	/** Texto a buscar */
	private String query;
	
	/** Indica si el texto es un codigo */
	private boolean isCodigo;
	
	/**
	 * Instancia la busqueda
	 * @param query texto a buscar
	 */
	public CategoriasSearcher(String query) {
		this.query = query.toLowerCase();
		this.isCodigo = StringUtils.isNumeric(this.query.replace(".", ""));
	}
	
	/**
	 * Dada una lista de categorias, las filtra y ordena por la query
	 */
	public List<Categoria> search(Collection<Categoria> categorias) {
		return categorias.stream()
				.filter(this)
				.sorted(this)
				.collect(Collectors.toList());
	}
	
	@Override
	public boolean test(Categoria cat) {
		// Se debe incluir la categoria si contiene el texto en la descripcion o en el codigo
		return cat.getDescripcion().toLowerCase().contains(query) || (isCodigo && cat.getCodigo().startsWith(query));
	}
	
	@Override
	public int compare(Categoria c1, Categoria c2) {
		// Si el texto es un codigo, se intenta ordenar primero por codigo
		if(isCodigo) {
			// Si una de las categorias tiene el texto en el codigo, esa tiene prioridad (por sobre la que lo tiene en la descripcion)
			// Si ambas tienen el texto en el codigo, va primero la que tiene codigo mas corto (es mejor coincidencia)
			
			if(c1.getCodigo().startsWith(query)) {
				if(c2.getCodigo().startsWith(query)) return c1.getCodigo().length() - c2.getCodigo().length();
				return -1;
			}
			
			if(c2.getCodigo().startsWith(query)) return 1;
		}
		
		// Si llega aca, ambas tienen coincidencia por descripcion
		// Va primero la que tiene descripcion mas corta (es mejor coincidencia)
		return c1.getDescripcion().length() - c2.getDescripcion().length();
	}
}