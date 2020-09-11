package com.mmoscovich.webcontab.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.PersistentEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * Clase de metodo utiles relacionados con colecciones.
 */
@UtilityClass
public class CollectionUtils {
	
	/**
	 * Dada una lista de {@link PersistentEntity}, devuelve una lista de ids, uno por cada entidad
	 * @param entities lista de entidades
	 * @return lista de ids de las entidades
	 */
	public Set<Long> toIdList(Collection<? extends PersistentEntity> entities) {
		return mapAsSet(entities, PersistentEntity::getId);
	}
	
	/**
	 * Busca el primer elemento de la coleccion para el cual la funcion devuelve true.
	 * @param <T>
	 * @param col coleccion
	 * @param item item que se usara para comparar
	 * @param compareFn funcion que compara el item especificado con cada uno de la lista
	 * @return opcional con el item si existe o vacio si no
	 */
	public <T> Optional<T> find(Collection<T> col, T item, BiFunction<T, T, Boolean> compareFn) {
		return col.stream().filter(i -> compareFn.apply(i, item)).findFirst();
	}
	
	/**
	 * Busca el item de la coleccion que tiene el mismo id que el especificado
	 * @param <T>
	 * @param col
	 * @param entity entidad cuyo id se usa para buscar el item en la coleccion
	 * @return el item encontrado o null.
	 */
	public <T extends PersistentEntity> T find(Collection<T> col, final T entity) {
		return col.stream().filter(e -> e.getId() != null && e.getId().equals(entity.getId())).findFirst().orElse(null);
	}
	
	/**
	 * Convierte un String de ids separados por comas en una lista de Ids de tipo {@link Long}.
	 * @param ids
	 * @return
	 * @throws InvalidRequestException si el string no se puede parsear
	 */
	public List<Long> parseLongList(String ids) throws InvalidRequestException {
		if(ids == null) return null;
		try {
			return Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
		} catch(NumberFormatException e) {
			throw new InvalidRequestException("Los valores no se pudieron parsear", e);
		}
	}
	
	/**
	 * Filtra una lista en base a un predicate.
	 * @param <T> tipo de item
	 * @param items lista de items
	 * @param predicate funcion que filtra los items
	 * @return la lista filtrada
	 */
	public <T> List<T> filter(Collection<T> items, Predicate<? super T> predicate) {
		return items.stream().filter(predicate).collect(Collectors.toList());
	}
	
	/**
	 * Mapea una coleccion a una lista.
	 * @param <T> tipo de item
	 * @param items lista de items
	 * @param mapper funcion que convierte cada item de un tipo a otro.
	 * @return la lista mapeada
	 */
	public <T, R> List<R> map(Collection<T> items, Function<? super T, ? extends R> mapper) {
		return items.stream().map(mapper).collect(Collectors.toList());
	}
	
	/**
	 * Mapea una coleccion a un Set.
	 * @param <T> tipo de item
	 * @param items lista de items
	 * @param mapper funcion que convierte cada item de un tipo a otro.
	 * @return la lista mapeada
	 */
	public <T, R> Set<R> mapAsSet(Collection<T> items, Function<? super T, ? extends R> mapper) {
		return items.stream().map(mapper).collect(Collectors.toSet());
	}
	
	/**
	 * Compara los items de dos colecciones en base a una funcion especificada por parametro 
	 * y devuelve un objeto que posee 4 colecciones:
	 * <ul>
	 * 	<li>items que solo estan en la primera</li>
	 * 	<li>items que solo estan en la segunda</li>
	 * 	<li>items que estan en ambas (interseccion)</li>
	 * 	<li>todos los items (union)</li>
	 * <ul>
	 * 
	 * @param col1
	 * @param col2
	 * @param compareFn funcion que dado
	 * @return
	 */
	public <T, O> CollectionComparison<T, O> compare(Collection<T> col1, Collection<O> col2, BiFunction<T, O, Boolean> compareFn) {
		CollectionComparison<T, O> result = new CollectionComparison<>();
		
		// Creo una copia de la segunda coleccion para poder modificarla
		Collection<O> onlyInSecond = new HashSet<>(col2);
		
		// Itero cada item de la primera coleccion
		for(T item1 : col1) {
			// Itero cada item de la segunda coleccion
			for(O item2 : col2) {
				
				if(compareFn.apply(item1, item2)) {
					// Si el item esta en ambos, se agrega a la coleccion de ambos
					// y se quita de la exclusiva para la segunda
					result.addToBoth(item1, item2);
					onlyInSecond.remove(item2);
				} else {
					// Si el item esta solo en la primera, se agrega a dicha coleccion
					result.addToFirst(item1);
				}
				
			}
		}
		
		result.setOnlyInSecond(onlyInSecond);
		
		return result;
	}
	
	/**
	 * Clase que representa el resultado de comparar 2 colecciones.
	 * <p>Contiene colecciones con los items que:
	 * <ul>
	 * 	<li>solo estan en la primera</li>
	 * 	<li>solo estan en la segunda</li>
	 * 	<li>estan en ambas (interseccion). Guarda 2 lista "interseccion": una con el item de la primera y otro con ambos.</li>
	 * 	<li>estan en cualquiera de las dos colecciones (union).</li>
	 * </ul>
	 * </p>
	 * 
	 * @author Martin Moscovich
	 *
	 * @param <T> tipo de item de la primera coleccion
	 * @param <O> tipo de item de la segunda coleccion
	 */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	public static class  CollectionComparison<T, O> {
		
		/**
		 * Clase que representa un item que esta en ambas listas.
		 * <p>Provee las dos versiones por si se necesitan (por ej para merge).</p>
		 * 
		 * @author Martin Moscovich
		 *
		 * @param <T> tipo de item de la primera lista
		 * @param <O> tipo de item de la segunda lista
		 */
		@Getter
		@AllArgsConstructor
		public static class BiEntry<T, O> {
			private T first;
			private O second;
		}

		/**
		 * Items que solo estan en la <b>primera</b> lista
		 */
		private Collection<T> onlyInFirst = new HashSet<>();
		
		/**
		 * Items que solo estan en la <b>segunda</b> lista
		 */
		private Collection<O> onlyInSecond = new HashSet<>();
		
		/**
		 * Items que estan en <b>ambas</b> listas. 
		 * <br>Contiene la version del item de la primera lista.
		 */
		private Collection<T> intersection = new HashSet<>();
		
		/**
		 * Items que estan en <b>ambas</b> listas. 
		 * <br>Contiene ambas versiones del item (por si hace falta hacer merge).
		 */
		private Collection<BiEntry<T, O>> intersectionWithBoth = new HashSet<>();
		
		/**
		 * @return Todos los items de ambas listas. 
		 * En caso de los que estan en ambas, se usa la version de la primera lista. 
		 */
		public Collection<Object> getUnion() {
			Collection<Object> u = new HashSet<>();
			u.addAll(this.onlyInFirst);
			u.addAll(this.onlyInSecond);
			u.addAll(this.intersection);
			return u;
		}
		
		void addToFirst(T item) {
			this.onlyInFirst.add(item);
		}
		
		void addToSecond(O item) {
			this.onlyInSecond.add(item);
		}
		
		void addToBoth(T item1, O item2) {
			this.intersection.add(item1);
			this.intersectionWithBoth.add(new BiEntry<T, O>(item1, item2));
			
			this.onlyInFirst.remove(item1);
			this.onlyInSecond.remove(item2);
		}
	}


}
