/**
 * Interfaz que representa el status de carga de una entidad o una lista de items.
 *
 * @export
 */
export interface LoadingStatus {
  /**
   * Indica si el item o la lista se esta cargando
   */
  loading?: boolean;

  /**
   * Indica si la ultima carga resulto en error o no.
   */
  error?: boolean;
}

/**
 * Tipo de request de lista.
 * - `more`: Siguiente pagina, los resultados iran al final de la lista
 * - `refresh`: Solo nuevos items y se agregan al principio de la lista
 * - `reload`: Todos los items y se pisa la lista actual
 */
export type ListRequestMode = 'more' | 'refresh' | 'reload';

/**
 * Interfaz que representa el status de carga de una lista infinita paginada, o sea
 * aquellas que se puede scrollear hasta el final o hacer refresh.
 * Extiende el status para una lista comun pero agrega atributos especificos.
 *
 * Normalmente estan ordenadas de manera descendente.
 *
 *
 * @export
 */
export interface InfiniteListLoadingStatus extends LoadingStatus {
  /**
   * Indica si se estan cargando items mas antiguos ("more") o items mas nuevos ("refresh") o se recarga la lista ("reload").
   * Si no estan cargando puede estar indefinido.
   */
  type?: ListRequestMode;
}
