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
import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.mmoscovich.webcontab.dao.helper.CriteriaCondition;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.CuentaBase_;
import com.mmoscovich.webcontab.model.Cuenta_;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.util.CriteriaUtils;

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
	public Slice<Cuenta> searchCuentasByText(Organizacion org, String query, List<Categoria> categorias, Pageable pageParams) {
		// FROM Cuenta WHERE organizacion = :org AND (lower(descripcion) LIKE :%query% OR lower(descripcion) LIKE :%query% OR codigo LIKE :query% [OR codigo LIKE :parent1% OR codigo LIKE :parent2%...])
		
		return this.searchBaseByText(Cuenta.class, org, query, categorias, pageParams);
	}
	
	@Override
	public Slice<CuentaBase> searchAllByText(Organizacion org, String query, List<Categoria> categorias, Pageable pageParams) {
		// FROM CuentaBase WHERE organizacion = :org AND (lower(descripcion) LIKE :%query% OR lower(alias) LIKE :%query% OR codigo LIKE :query% [OR codigo LIKE :parent1% OR codigo LIKE :parent2%...])
		
		return this.searchBaseByText(CuentaBase.class, org, query, categorias, pageParams);
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
	private <T extends CuentaBase> Slice<T> searchBaseByText(Class<T> cls, Organizacion org, String text, List<Categoria> categorias, Pageable pageReq) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(cls);
        Root<T> cuenta = criteria.from(cls);
        
        // Expresion que equivale a "descripcion || alias", en minuscula
        //Se usa para buscar el texto en ambos campos al mismo tiempo
        // Si el alias es null, se usa un string vacio
        Expression<String> descripcionAlias = builder.lower(CriteriaUtils.concat(builder, "|", cuenta.get(Cuenta_.DESCRIPCION), builder.coalesce(cuenta.get(Cuenta_.ALIAS), "")));
        Expression<Integer> descripcionAliasIndexOf = builder.locate(descripcionAlias, text);

        Predicate descripcionAliasCondition = builder.notEqual(descripcionAliasIndexOf, 0); 
//        Predicate descripcionAliasCondition = builder.like(descripcionAlias, "%" + text + "%");
        
		CriteriaCondition cond = new CriteriaCondition().add(descripcionAliasCondition);
//      .add(builder.like(builder.lower(cuenta.get(Cuenta_.DESCRIPCION)), "%" + text + "%"))
//      .add(builder.like(builder.lower(cuenta.get(Cuenta_.ALIAS)), "%" + text + "%"));
        
		// Solo buscar en codigo si el texto puede ser un codigo (numeros y puntos)
        if(StringUtils.isNumeric(text.replace(".", ""))) {
        	cond.add(builder.like(cuenta.get(Cuenta_.CODIGO), text + "%"));
        }

    	// Se transforman las categorias a predicates que piden las cuentas que pertenecen
        cond.add(pertenecientesACategorias(builder, cuenta, categorias));
        	
        criteria.where(
    		// Deben ser las cuentas de una organizacion
    		builder.equal(cuenta.get(Cuenta_.ORGANIZACION), org),
    		cond.buildOr(builder)
        );
        
        // Se ordenan los resultados
        criteria.orderBy( 
    		// Primero las que tienen el texto en la descripcion o alias
        	// Luego las que son hijas, en el orden en que vinieron las categorias (que ya estaban ordenadas)
    		builder.asc(this.getOrderPorCategorias(builder, cuenta, descripcionAliasCondition, categorias)),
    		
    		// En caso de ser iguales, van primero las categorias (imputable = 0)
    		builder.asc(cuenta.type()),
    		
    		// En caso de ser iguales:
    		// - Si es por descripcion/alias, se pone primero los que tienen la coindicencia primero
    		// - Si es por categoria padre, se pone primero los de niveles superiores
    		builder.asc(this.getOrderPorLongitud(builder, cuenta, descripcionAliasIndexOf)),
    		
    		// Finalmente, si todo lo demas es igual, se ordenan por numero de categoria/cuenta
    		builder.asc(cuenta.get(Cuenta_.ORDEN))
		);
        
        // Se limita la longitud
        return CriteriaUtils.getSlice(em, criteria, pageReq);
	}
	
	/**
	 * Devuelve una expresion para ordenar las cuentas, colocando primero las que coinciden en texto o alias y
	 * luego las que son hijas de las categorias que coinciden.
	 * <p>
	 * La query recibe una lista de categorias ordenada por prioridad para buscar sus subcuentas.
	 * <br>Por lo tanto se debe ordenar de tal manera que las subcuentas queden en el orden en el que vinieron sus padres.
	 * </p> 
	 * @param builder
	 * @param cuenta
	 * @param categorias
	 * @return
	 */
	private Expression<Object> getOrderPorCategorias(CriteriaBuilder builder, Root<?> cuenta, Predicate descripcionAliasCondition, List<Categoria> categorias) {
		final Case<Object> select= builder.selectCase();
			
		// El primer orden es que coincidan en descripcion o alias directamente	
		select.when(descripcionAliasCondition, 0);
        
	     // Se hace un filtrado para no incluir categorias hijas de otras que ya estan incluidas
	     Collection<String> codigos = new ArrayList<>();
		
	     // Se recorre la lista de categorias ordenadas
		 for(Categoria cat : categorias) {
			if(!tienePrefijo(codigos, cat.getCodigo())) {
				codigos.add(cat.getCodigo());
				// Expresion que obtiene el prefijo del codigo
				Expression<String> prefijo = builder.substring(cuenta.get(Cuenta_.CODIGO), 0, cat.getCodigo().length());
				
				// Cada cuenta tiene un valor equivalente a la posicion de su categoria padre en la lista
				select.when(builder.equal(prefijo, cat.getCodigo()), codigos.size());
			}
		 }
		 
		 // Cualquier otra debe tener un valor mayor para ir al final
		 return select.otherwise(codigos.size() + 1);
	}
	
	/**
	 * Devuelve una expresion para ordenar las cuentas en base a la descripcion/alias o el codigo.
	 * <p>
	 * Si matchea por descripcion o alias se usa la posicion del texto en la descripcion/alias (si aparece primero va antes)
	 * <br>Si matchea por hijo, se ordena por la longitud del codigo (mientras menos niveles, mejor).
	 * </p> 
	 * @param builder
	 * @param cuenta
	 * @param categorias
	 * @return
	 */
	private Expression<Object> getOrderPorLongitud(CriteriaBuilder builder, Root<?> cuenta, Expression<Integer> descripcionAliasIndexOf) {
		return builder.selectCase()
//				.when(descripcionAliasLike, builder.length(cuenta.get(Cuenta_.DESCRIPCION)))
				.when(builder.notEqual(descripcionAliasIndexOf, 0), descripcionAliasIndexOf)
				.otherwise(builder.length(cuenta.get(Cuenta_.ORDEN)));
	}
}
