package com.mmoscovich.webcontab.util;

import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.jpa.QueryHints;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.mmoscovich.webcontab.model.PersistentEntity;

/**
 * Utilidades para trabajar con JPA.
 * <p>Permite detectar Proxies, saber si una entidad esta cargada o pedir el ID sin que eso dispare una Query.</p>
 * 
 * @author Martin Moscovich
 */
public class JpaUtils {

	/**
	 * Determina si la entidad es un proxy de JPA
	 * 
	 * @param entity entidad a evaluar
	 * @return <code>true</code> si es un proxy, <code>false</code> en caso contrario
	 */
	public static boolean isProxy(Object entity) {
		 return (entity instanceof HibernateProxy || entity instanceof PersistentCollection);
	}
	
	/**
	 * Determina si la entidad fue cargada.
	 * <br>Esto puede significar que es un POJO normal o bien un proxy que fue inicializado
	 * 
	 * @param entity entidad a evaluar
	 * @return <code>true</code> si fue cargada, <code>false</code> en caso contrario.
	 */
	public static boolean isLoadedEntity(Object entity) {
		if(entity instanceof HibernateProxy) return !((HibernateProxy)entity).getHibernateLazyInitializer().isUninitialized(); 
		if(entity instanceof PersistentCollection) return ((PersistentCollection)entity).wasInitialized();
		
		return true;
	}
	
	private static LazyInitializer getProxy(Object entity) {
		 if(!isProxy(entity)) return null;
		 return ((HibernateProxy) entity).getHibernateLazyInitializer();
	}
	
	/**
	 * Devuelve el identificador de un {@link PersistentEntity}.
	 * <p>Si la entidad es directamente el POJO, llama a {@link PersistentEntity#getId()}.
	 * <br>Si es un proxy, usa un metodo de Hibernate para extraer el identificador del mismo.</p>
	 * 
	 * @param entity entidad de la que se desea el id
	 * @return el identificador
	 */
	public static Long getId(PersistentEntity entity) {
		 LazyInitializer li = getProxy(entity);
		 if(li == null) return entity.getId();
		 
		 return (Long)li.getIdentifier();
	}
	
	/**
	 * Determina si una clase es una entidad
	 * @param clazz
	 * @return
	 */
	public static boolean isEntity(EntityManager em, Class<?> clazz) {
	    boolean foundEntity = false;
	    Set<EntityType<?>> entities = em.getMetamodel().getEntities();
	    for(EntityType<?> entityType :entities) {
	        Class<?> entityClass = entityType.getJavaType();
	        if(entityClass.equals(clazz)) {
	            foundEntity = true;
	        }
	    }
	    return foundEntity;
	}
	
	/**
	 * Crea un stream a partir de una query usando el fetch size especificado
	 * @param <T>
	 * @param criteria
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Stream<T> getStreamFromQuery(Query query, int fetchSize) {
		return query
        		.setHint(QueryHints.HINT_FETCH_SIZE, fetchSize)
        		.setHint(QueryHints.HINT_CACHEABLE, false)
        		.setHint(QueryHints.HINT_READONLY, true)
        		.getResultStream();
	}
}
