package com.mmoscovich.webcontab.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

/**
 * Helper para Criteria de JPA
 */
public class CriteriaUtils {
	
	/**
	 * Crea un predicate para un rango de valores (pueden ser fechas o numeros).
	 * <p>La logica es:
	 * <br>Si estan ambos valores, es un between.
	 * <br>Si hay uno solo es un <= o >=
	 * <br>Si no esta ninguno, no se genera el predicate
	 * @param <Y> tipo de dato
	 * @param builder
	 * @param field campo
	 * @param min valor minimo
	 * @param max valor maximo
	 * @return el predicate o un optional vacio si no debe crearse
	 */
	public static <Y extends Comparable<? super Y>> Optional<Predicate> between(CriteriaBuilder builder, Expression<? extends Y> field, Y min, Y max) {
		if(min != null && max != null) {
			return Optional.of(builder.between(field, min, max));
		} else if(min != null) {
			return Optional.of(builder.greaterThanOrEqualTo(field, min));
		} else if(max != null) {
			return Optional.of(builder.lessThanOrEqualTo(field, max));
		}
		return Optional.empty();
	}
	
	
	/**
	 * Obtiene el root de una {@link CriteriaQuery} del tipo indicado
	 * @param <T>
	 * @param c
	 * @param cls
	 * @return el root o null si no existe
	 */
	@SuppressWarnings("unchecked")
	public static <T> Root<T> getRoot(CriteriaQuery<?> c, Class<T> cls) {
		for(Root<?> r: c.getRoots()) {
			if (r.getJavaType().equals(cls)) return (Root<T>) r;
		}
		return null;
	}
	
	/**
	 * Obtiene un {@link Slice} de Spring Data a partir de una {@link CriteriaQuery} y los datos de paginacion
	 * @param <T>
	 * @param em entity manager
	 * @param criteria
	 * @param pagination datos de paginacion
	 * @return
	 */
	public static <T> Slice<T> getSlice(EntityManager em, final CriteriaQuery<T> criteria, Pageable pagination) {
		// Se obtiene una pagina de items
		List<T> content = em.createQuery(criteria)
			.setFirstResult(Long.valueOf(pagination.getOffset()).intValue())
			
			// Se pide uno mas que lo necesario para saber si hay next
			.setMaxResults(pagination.getPageSize() + 1)
			.getResultList();
		
		int resultSize = content.size();

		// Se obtiene la lista con el tamanio de pagina como maximo
		content = content.subList(0, Math.min(pagination.getPageSize(), resultSize));
		
		return new SliceImpl<T>(content, pagination, content.size() < resultSize);
		
	}
	
	/**
	 * Obtiene una {@link Page} de Spring Data a partir de una {@link CriteriaQuery} y los datos de paginacion
	 * @param <T>
	 * @param em entity manager
	 * @param criteria
	 * @param pagination datos de paginacion
	 * @return
	 */
	public static <T> Page<T> getPage(EntityManager em, final CriteriaQuery<T> criteria, Pageable pagination) {
		// Se obtiene una pagina de items
		List<T> content = em.createQuery(criteria)
			.setFirstResult(Long.valueOf(pagination.getOffset()).intValue())
			.setMaxResults(pagination.getPageSize())
			.getResultList();
		
		// Se obtiene la pagina con el total
        return new PageImpl<T>(content, pagination, count(em, criteria));
	}
	
	/**
	 * Crea un stream a partir de una criteria usando el fetch size especificado
	 * @param <T>
	 * @param em entity manager
	 * @param criteria
	 * @param fetchSize
	 * @return
	 */
	public static <T> Stream<T> getStream(EntityManager em, CriteriaQuery<T> criteria, int fetchSize) {
		return JpaUtils.getStreamFromQuery(em.createQuery(criteria), fetchSize);
	}
	
	/**
	 * Obtiene el count a partir de una {@link CriteriaQuery}
	 * @param <T>
	 * @param em entity manager
	 * @param criteria
	 * @return numero de filas devueltas
	 */
	public static <T> long count(EntityManager em, final CriteriaQuery<T> criteria) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Long> query = createCountQuery(builder, criteria);
	    return em.createQuery(query).getSingleResult();
	}
	
	/**
	 * Concatena una cantidad arbitraria de campos usando el delimitador indicado
	 * @param delimiter delimitador entre campos
	 * @param expressions expresiones a concatenar
	 * @return la expresion que concatena los campos usando el delimitador
	 */
	@SafeVarargs
	public static Expression<String> concat(final CriteriaBuilder cb, String delimiter, Expression<String> ... expressions) {
	    if(expressions.length == 1) return expressions[0];
	    
	    Expression<String> result = null;
	    
	    for (int i = 0; i < expressions.length; i++) {
	        final Expression<String> expression = expressions[i];
	        
	        // Si es la primera expresion se incluye. Si no, se concatena a lo acumulado
	        result = (result == null) ? expression : cb.concat(result, expression);

	        // Si no es la ultima expresion se agrega el delimitador
            if (i < expressions.length - 1) {
                result = cb.concat(result, delimiter);
	        }
	    }
	    return result;
	}

	/**
	 * Crea una {@link CriteriaQuery} para hacer el count a partir de una {@link CriteriaQuery} existente.
	 * @param <T>
	 * @param cb
	 * @param criteria
	 * @return
	 */
	private static <T> CriteriaQuery<Long> createCountQuery(final CriteriaBuilder cb, final CriteriaQuery<T> criteria) {
		Root<T> root = getRoot(criteria, criteria.getResultType());
		
	    final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
	    final Root<T> countRoot = countQuery.from(criteria.getResultType());

	    // Replica los join y los join fetch
	    doJoins(root.getJoins(), countRoot);
	    doJoinsOnFetches(root.getFetches(), countRoot);

	    // Hace select del count
	    countQuery.select(cb.count(countRoot));
	    
	    // Replica los Where
	    countQuery.where(criteria.getRestriction());

	    countRoot.alias(root.getAlias());
	    
	    // Replica el group by y having
	    countQuery.groupBy(criteria.getGroupList());
	    countQuery.having(criteria.getGroupRestriction());

	    return countQuery.distinct(criteria.isDistinct());
	}

	/**
	 * Replica los fetches de un {@link Root} en otro, recursivamente
	 * @param fetches fetches originales
	 * @param root root donde se deben incluir los fetches
	 */
	@SuppressWarnings("unchecked")
	private static void doJoinsOnFetches(Set<? extends Fetch<?, ?>> fetches, Root<?> root) {
	    doJoins((Set<? extends Join<?, ?>>) fetches, root);
	}

	/**
	 * Replica los joins de un {@link Root} en otro, recursivamente
	 * @param joins joins originales
	 * @param root root donde se deben incluir los joins
	 */
	private static void doJoins(Set<? extends Join<?, ?>> joins, From<?, ?> root) {
	    for (Join<?, ?> join : joins) {
	        Join<?, ?> joined = root.join(join.getAttribute().getName(), join.getJoinType());
	        joined.alias(join.getAlias());
	        doJoins(join.getJoins(), joined);
	    }
	}
}
