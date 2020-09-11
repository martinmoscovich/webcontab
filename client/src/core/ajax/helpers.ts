/* eslint-disable @typescript-eslint/no-explicit-any */
import { AxiosResponse } from 'axios';
import Page, { pageFromJSON } from '../Page';
import { Sort, PageRequest, SearchOptions, RequestOptions, SortDirection } from './model';
import { Dictionary } from 'vue-router/types/router';
import { formatDateForServer } from '@/utils/date';

/**
 * Crea un handler para respuestas Axios que mapea el payload usando el mapper especificado
 *
 * @export
 * @template T tipo de objeto a devolver
 * @template any
 * @param {(json: any) => T} [mapper] funcion que mapea el payload a la instancia deseada
 * @returns {(response: AxiosResponse<T>) => T} handler de axios que usa el mapper
 */
export function toEntity<T = any>(mapper?: (json: any) => T): (response: AxiosResponse<T>) => T {
  // Si el mapper existe, se usa, si no se devuelve el payload intacto
  return response => (mapper ? mapper(response.data) : response.data);
}

/**
 * Crea un handler que mapea el payload de la respuesta o bien una lista any[] a una lista de items tipados.
 *
 * Para cada item de la respuesta se llama el mapper, permitiendo crear las instancias.
 *
 * @export
 * @template T tipo de objeto a devolver en la lista
 * @param {(json: any) => T} mapper funcion que mapea cada item del payload a la instancia deseada
 * @returns {((response: any[] | AxiosResponse<T>) => T[])} handler de axios que mapea a una lista tipada
 */
export function toList<T = any>(mapper?: (json: any) => T): (response: any[] | AxiosResponse<T[]>) => T[] {
  return response => {
    if (Array.isArray(response)) {
      // Si la response ya es el array, se mapea
      return mapper ? response.map(mapper) : response;
    } else {
      // Si no, se castea el payload a array para mapearlo.
      return mapper ? (response.data as any[]).map(mapper) : (response.data as any[]);
    }
  };
}

/**
 * Crea un handler que mapea una pagina o el payload del response Axios a una pagina de items tipados.
 *
 * @export
 * @template T tipo de objeto a devolver en la pagina
 * @template any
 * @param {(json: any) => T} [itemMapper] funcion que mapea cada item de la pagina a la instancia deseada
 * @param {(json: any) => Page<T>} [pageMapper] funcion que mapea los atributos de la pagina (default: `items`, `number`, `next`, `total`)
 * @returns {((json: Page<any> | AxiosResponse<Page<T>>) => Page<T>)} handler de axion que mapea a una pagina tipada
 */
export function toPage<T = any>(
  itemMapper?: (json: any) => T,
  pageMapper?: (json: any) => Page<T>
): (json: Page<any> | AxiosResponse<Page<T>>) => Page<T> {
  return response => {
    // Se obtiene la pagina de jsons, ya sea directamente del parametro o de la response Axios
    const json = ((response as any).status ? (response as any).data : response) as Page<any>;

    return pageFromJSON(json, itemMapper, pageMapper);
  };
}

/**
 * Convierte un objecto JS (mapa) en query string para hacer un request, incluyendo el `?` inicial.
 *
 * Encodea los parametros automaticamente y **excluye** los `undefined` o `null` (no viajan en el request).
 *
 * @param map
 */
export function queryString(map: Record<string, any> | undefined): string {
  if (!map) return '';
  const qs: string = Object.entries(map)
    .filter(([key, value]) => key !== undefined && value !== undefined && value !== null)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
  if (qs === '') return '';
  return '?' + qs;
}

/**
 * Dado un objeto, crea otro con las propiedades definidas (sin null y undefined) y el valor:
 * - Si es fecha, con el formato de fecha para el server
 * - Si es boolean, solo lo incluye si es true
 * - Cualquier otro, hace toString().
 * @param object
 */
export function toQuerystringDictionary(object: Dictionary<any> | undefined | null) {
  if (!object) return {};

  const qs: Dictionary<string> = {};
  for (const prop in object) {
    const value = object[prop];
    if (value !== null && value !== undefined) {
      if (value instanceof Date) {
        qs[prop] = formatDateForServer(value);
      } else if (value instanceof Boolean) {
        if (value) qs[prop] = 'true';
      } else {
        qs[prop] = value.toString();
      }
    }
  }
  return qs;
}

/**
 * Genera el mapa para QS a partir de un objeto Sort
 * @param sort
 */
function sortToQuerystring(sort?: Sort) {
  const qs: Dictionary<string> = {};
  if (sort?.field) {
    qs.sort = sort.field;
    if (sort.direction === 'desc') qs.sort += '-desc';
  }
  return qs;
}

/**
 * Genera el mapa para QS a partir de un objeto Pagination
 * @param sort
 */
export function paginationToQuerystring(pagination?: PageRequest) {
  return toQuerystringDictionary(pagination);
}

/**
 * Genera el mapa para QS a partir de un RequestOptions
 * @param requestOptions
 */
function requestOptionsToQuerystring(requestOptions: RequestOptions) {
  const qs: Dictionary<string> = {
    ...paginationToQuerystring(requestOptions.pagination),
    ...sortToQuerystring(requestOptions.sort)
  };
  if (requestOptions.count) qs.count = 'true';
  return qs;
}

/**
 * Genera el mapa para QS a partir de un SearchOptions
 * @param requestOptions
 */
export function searchOptionsToQuerystring(opts: SearchOptions<any>) {
  return {
    ...requestOptionsToQuerystring(opts),
    ...toQuerystringDictionary(opts.filter)
  };
}

/**
 * Convierte un mapa QS en un Sort
 * @param qs
 */
export function parseSortFromQS(qs: Dictionary<string>): Sort | undefined {
  if (!qs.sort) return undefined;
  const parts = qs.sort.split('-');
  return {
    field: parts[0],
    direction: parts.length > 1 ? (parts[1] as SortDirection) : 'asc'
  };
}

/**
 * Convierte un mapa QS en un PageRequest
 * @param qs
 */
export function parsePaginationFromQS(qs: Dictionary<string>): PageRequest | undefined {
  if (!qs.page && !qs.size) return undefined;
  return {
    page: qs.page ? parseInt(qs.page, 10) : 1,
    size: qs.size ? parseInt(qs.size, 10) : undefined
  };
}

/**
 * Convierte un mapa QS en un RequestOptions
 * @param qs
 */
export function parseRequestOptionsFromQS(qs: Dictionary<string>): RequestOptions {
  return {
    pagination: parsePaginationFromQS(qs),
    sort: parseSortFromQS(qs)
  };
}
