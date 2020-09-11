package com.mmoscovich.webcontab.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.mmoscovich.webcontab.dao.helper.CriteriaCondition;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.CuentaBase_;
import com.mmoscovich.webcontab.model.Cuenta_;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * Implementacion de {@link CuentaBaseExtraRepository}. 
 *
 */
public class CuentaBaseExtraRepositoryImpl implements CuentaBaseExtraRepository {

	@Inject
	private EntityManager em;
	

	@Override
	public Optional<Long> findDuplicado(CuentaBase catOCuenta, String newDescripcion, boolean checkCodigo) {
//		FROM CuentaBase WHERE organizacion = :org 
//		AND ((:codigo IS NULL OR codigo = :codigo) or (categoria.id = :categoriaId AND descripcion = :descripcion))
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<CuentaBase> cuenta = criteria.from(CuentaBase.class);
        
        // Se devuelve el id
        criteria.select(cuenta.get(CuentaBase_.ID));
        
        boolean isRoot = catOCuenta.getCategoria() == null || catOCuenta.getCategoria().getId() == null;
        
        // Condicion Categoria (debe tener el mismo padre)
        Predicate condCategoria = isRoot ? 
        		builder.isNull(cuenta.get(CuentaBase_.CATEGORIA)) : 
        		builder.equal(cuenta.get(CuentaBase_.CATEGORIA), catOCuenta.getCategoria());
        
        // Condicion Descripcion (incluye la de categoria)
        Predicate condDescripcion = builder.and(condCategoria, builder.equal(cuenta.get(CuentaBase_.DESCRIPCION), newDescripcion));

        Predicate filtro = condDescripcion;
        
        // Si se pidio, se agrega la condicion del codigo
        if(checkCodigo) filtro = builder.or(condDescripcion, builder.equal(cuenta.get(CuentaBase_.CODIGO), catOCuenta.getCodigo()));
        
        criteria.where(
    		builder.equal(cuenta.get(CuentaBase_.ORGANIZACION), catOCuenta.getOrganizacion()),
    		filtro
		);

        try {
        	return Optional.of(em.createQuery(criteria).getSingleResult());
        } catch(NoResultException e) {
        	return Optional.empty();
        }
	}
	
	@Override
	public List<Cuenta> getCuentasDescendientes(Organizacion org, List<Categoria> categorias) {
		// FROM Cuenta WHERE organizacion = :org [OR codigo LIKE :parent1% OR codigo LIKE :parent2%...])
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Cuenta> criteria = builder.createQuery(Cuenta.class);
        Root<Cuenta> cuenta = criteria.from(Cuenta.class);
        
        criteria.where(new CriteriaCondition()
        	// Deben ser las cuentas de una organizacion
        	.add(builder.equal(cuenta.get(Cuenta_.ORGANIZACION), org))

        	// Se transforman las categorias a predicates que piden las cuentas que pertenecen y se incluyen en el WHERE
        	.add(pertenecientesACategorias(builder, cuenta, categorias))
        	
        	.buildAnd(builder)
        );
        
        return em.createQuery(criteria).getResultList();
	}
	
	@Override
	public List<Cuenta> searchCuentasByText(Organizacion org, String query, List<Categoria> categorias) {
		// FROM Cuenta WHERE organizacion = :org AND (lower(descripcion) LIKE :%query% OR lower(descripcion) LIKE :%query% OR codigo LIKE :query% [OR codigo LIKE :parent1% OR codigo LIKE :parent2%...])
		
		return this.searchBaseByText(Cuenta.class, org, query, categorias);
	}
	
	@Override
	public List<CuentaBase> searchAllByText(Organizacion org, String query, List<Categoria> categorias) {
		// FROM CuentaBase WHERE organizacion = :org AND (lower(descripcion) LIKE :%query% OR lower(alias) LIKE :%query% OR codigo LIKE :query% [OR codigo LIKE :parent1% OR codigo LIKE :parent2%...])
		
		return this.searchBaseByText(CuentaBase.class, org, query, categorias);
	}
	
	/**
	 * Crea un predicate para buscar todas las cuenta o categorias descendientes de las especificadas
	 */
	private <T extends CuentaBase> Optional<Predicate> pertenecientesACategorias(CriteriaBuilder cb, Path<T> cuenta, List<Categoria> categorias) {
		// Se hace un filtrado para no incluir categorias hijas de otras que ya estan incluidas
		Collection<String> codigos = new ArrayList<>();
		
		for(Categoria cat : categorias) {
			if(!tienePrefijo(codigos, cat.getCodigo())) codigos.add(cat.getCodigo());
		}
		
		// Se crea la lista de predicates, uno por cada codigo
		Collection<Predicate> p = codigos.stream()
				.map(c -> cb.like(cuenta.get(Cuenta_.CODIGO), c + ".%"))
				.collect(Collectors.toList());
		
		if(p.isEmpty()) return Optional.empty();
		
		// Se devuelve un solo predicate de tipo OR
		return Optional.of(cb.or(p.toArray(new Predicate[0])));
	}
	
	/**
	 * Determina si la coleccion ya tiene un codigo que contiene al que se especifica.
	 * <p>Ej: Si el codigo es "1.2.34" y la coleccion ya tiene "1" o "1.2" devuelve true.
	 */
	private boolean tienePrefijo(Collection<String> codigos, String codigo) {
		for(String prefijo : codigos) {
			if(codigo.startsWith(prefijo + ".")) return true;
		}
		return false;
	}
	
	/**
	 * Hace la busqueda por texto.
	 */
	private <T extends CuentaBase> List<T> searchBaseByText(Class<T> cls, Organizacion org, String query, List<Categoria> categorias) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(cls);
        Root<T> cuenta = criteria.from(cls);
        
        Predicate searchFilter = new CriteriaCondition()
        	.add(builder.like(builder.lower(cuenta.get(Cuenta_.DESCRIPCION)), "%" + query + "%"))
        	.add(builder.like(builder.lower(cuenta.get(Cuenta_.ALIAS)), "%" + query + "%"))
        	.add(builder.like(cuenta.get(Cuenta_.CODIGO), query + "%"))

        	// Se transforman las categorias a predicates que piden las cuentas que pertenecen
        	.add(pertenecientesACategorias(builder, cuenta, categorias))
        	
        	.buildOr(builder);
        
        criteria.where(
        		// Deben ser las cuentas de una organizacion
        		builder.equal(cuenta.get(Cuenta_.ORGANIZACION), org),
        		searchFilter
        );
        
        return em.createQuery(criteria).getResultList();
	}
}
