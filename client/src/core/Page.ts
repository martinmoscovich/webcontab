/* eslint-disable @typescript-eslint/no-explicit-any */
import { isNullOrUndefined } from '@/utils/general';

/**
 * Interfaz que representa una Pagina de entidades.
 * Se utiliza en las APIs paginadas
 *
 * @export
 * @template T tipo de item de la pagina
 */
export default interface Page<T> {
  /** items de la pagina */
  items: T[];

  /** indica si hay siguiente pagina */
  next: boolean;

  /** cantidad total de items, en caso de ser conocido dicho valor */
  total?: number;

  /** tamanio de la pagina (items.length) */
  size: number;

  /** numero de pagina */
  number: number;
}

/**
 * Devuelve una pagina vacia
 *
 * @export
 * @returns una pagina vacia
 */
export function emptyPage() {
  return { items: [], next: false, size: 0, number: 0 };
}

/**
 * Crea una pagina
 *
 * @export
 * @template T tipo de item
 * @param {T[]} items lista de items
 * @param {number} pageNumber numero de pagina
 * @param {boolean} [next=false] indica si hay pagina siguiente
 * @param {number} [total] cantidad total de items, si es conocida
 * @returns {Page<T>} la pagina
 */
export function createPage<T>(items: T[], pageNumber = 1, next = false, total?: number): Page<T> {
  return {
    items,
    number: pageNumber,
    next,
    total,
    size: items.length
  };
}

/**
 * Crea una pagina a partir de un JSON.
 *
 * Se puede pasar un mapper para los items y otro para la pagina.
 *
 * Si no se define un mapper de pagina, se considera que el JSON tiene la misma estructura.
 *
 * Si no se define un mapper de items, se considera que los mismos ya tienen la estructura deseada.
 *
 * Por lo tanto, si no se pasa ninguno de los mappers, esta funcion es basicamente un casteo.
 *
 * @export
 * @template T tipo de item
 * @template any
 * @param {Page<any>} jsonPage pagina en formato JSON
 * @param {(json: any) => T} [mapper] funcion que mapea cada item a una instancia deseada.
 * @param {(json: any) => Page<T>} [pageMapper] funcion que mapea los atributos de la pagina (default: `items`, `number`, `next`, `total`)
 * @returns {Page<T>} la pagina tipada
 */
export function pageFromJSON<T = unknown>(
  jsonPage: any,
  mapper?: (json: any) => T,
  pageMapper?: (json: any) => Page<T>
): Page<T> {
  // Se adapta la estructura del JSON a Page
  const adaptedPage: Page<T> = pageMapper ? pageMapper(jsonPage) : (jsonPage as Page<T>);

  if (isNullOrUndefined(adaptedPage.next)) adaptedPage.next = jsonPage.hasNext;

  // Se obtienen los items y si no existen, una lista vacia
  const items = adaptedPage.items || [];

  // Se genera la pagina
  return createPage(mapper ? items.map(mapper) : items, adaptedPage.number, adaptedPage.next, adaptedPage.total);
}
