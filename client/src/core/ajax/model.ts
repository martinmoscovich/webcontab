export type SortDirection = 'asc' | 'desc';

/**
 * Parametros de ordenamiento
 */
export interface Sort {
  /** Campo segun el cual ordenar */
  field: string;

  /** Direccion del orden */
  direction?: SortDirection;
}

/**
 * Parametros de paginacion
 */
export interface PageRequest {
  /** Numero de pagina */
  page: number;

  /** Tamanio de pagina */
  size?: number;
}

/**
 * Parametros genericos de request, incluyendo orden, paginacion y si calcular el total
 */
export interface RequestOptions {
  /** Parametros de ordenamiento */
  sort?: Sort;

  /** Parametros de paginacion */
  pagination?: PageRequest;

  /** Indica si calcular el total */
  count?: boolean;
}

/**
 * Parametros de busqueda, incluyendo RequestOptions y tambien un filtro especifico para el caso
 */
export interface SearchOptions<T> extends RequestOptions {
  /** Filtro a aplicar para una busqueda */
  filter: T;
}
