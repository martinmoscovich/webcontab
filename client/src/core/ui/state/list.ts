import { InfiniteListLoadingStatus, LoadingStatus, ListRequestMode } from '../loading';
import Page from '../../Page';
import { insertOrUpdate, ListItemPredicate, byId, byCodigo, OverwritePredicate } from '@/utils/array';
import { hasId, hasCodigo } from '@/model/IdModel';

/**
 * Interfaz que representa el estado de una lista en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx).
 *
 * @export
 * @template T tipo de items que contiene la lista
 */
export interface ListState<T> {
  /**
   * Status de carga de la lista
   */
  status: LoadingStatus;

  /**
   * Numero total de items, si se conoce
   */
  total?: number;

  /**
   * Lista de items actualmente cargados
   */
  items: T[];
}

/**
 * Interfaz que representa el estado de una lista infinita paginada en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx).
 *
 * Extiende del estado de la lista comun.
 *
 * @export
 * @template T tipo de items que contiene la lista
 */
export interface InfiniteListState<T> extends ListState<T> {
  /**
   * Status de carga de la lista paginada
   */
  status: InfiniteListLoadingStatus;

  /**
   * Indica si hay mas paginas para cargar.
   */
  hasNext: boolean;

  /**
   * Indica la ultima pagina cargada
   */
  lastPage: number;

  /**
   * Indica fecha o valor de referencia de la primera query.
   *
   * Modo de uso: Al hacer la primera query se setea la fecha actual, y luego en queries posteriores se pasa ese valor.
   * De esta forma, solo se recibiran items anteriores a esa fecha.
   *
   * Esto es necesario porque cuando el orden es descendente, podrian aparecer nuevos items que "muevan" el offset de la pagina.
   */
  initialQueryTs?: number | Date;
}

/**
 * Interfaz que representa el estado de una lista de paginacion estandar en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx).
 *
 * @export
 * @template T tipo de items que contiene la lista
 */
export interface PaginatedListState<T> {
  /**
   * Status de carga de la lista paginada
   */
  status: LoadingStatus;

  /**
   * Numero total de items, si se conoce
   */
  total?: number;

  /**
   * Lista de items actualmente cargados
   */
  pages: Page<T>[];

  /**
   * Indica el numero de pagina actual
   */
  currentPageNumber: number;

  /**
   * Indica fecha o valor de referencia de la primera query.
   *
   * Modo de uso: Al hacer la primera query se setea la fecha actual, y luego en queries posteriores se pasa ese valor.
   * De esta forma, solo se recibiran items anteriores a esa fecha.
   *
   * Esto es necesario porque cuando el orden es descendente, podrian aparecer nuevos items que "muevan" el offset de la pagina.
   */
  initialQueryTs?: number | Date;
}

/**
 * Devuelve el estado inicial para una lista de items en la aplicacion.
 * Es un estado donde no hay items cargados ni se estan cargando.
 *
 * @export
 * @template T tipo de item
 * @returns {ListState<T>} estado inicial
 */
export function getInitialListState<T>(): ListState<T> {
  return { items: [], status: {} };
}

/**
 * Devuelve el estado inicial para una lista de items infinita (paginada) en la aplicacion.
 *
 * Es un estado donde no hay items cargados ni se estan cargando, pero se indica que hay siguiente
 * pagina de manera que se intente cargar primeros items.
 * Se indica que la ultima pagina es `-1` para que la siguiente sea la `0`.
 *
 * @export
 * @template T tipo de item
 * @returns {InfiniteListState<T>}  estado inicial
 */
export function getInitialInfiniteListState<T>(): InfiniteListState<T> {
  return {
    items: [],
    hasNext: true,
    lastPage: -1,
    initialQueryTs: undefined,
    status: { type: 'more' }
  };
}

/**
 * Devuelve el estado inicial para una lista de items paginada (estandar) en la aplicacion
 * (sin paginas, nada cargando ni en error y `currentPageNumber = 0`).
 *
 * @export
 * @template T tipo de item
 * @returns {InfiniteListState<T>}  estado inicial
 */
export function getInitialPaginatedListState<T>(): PaginatedListState<T> {
  return {
    pages: [],
    currentPageNumber: 0,
    initialQueryTs: undefined,
    status: {}
  };
}

/**
 * Se modifica el estado de la lista para indicar que se esta pidiendo resultados.
 * - Pone `loading = true` y `error = false`
 */
export function listRequest(state: ListState<unknown>) {
  state.status = { loading: true };
}

/**
 * Se modifica el estado de la lista para indicar que hubo un error.
 * - Pone `error = true` y `loading = false`
 */
export function listFail(state: ListState<unknown>) {
  state.status = { error: true };
}

/**
 * Se modifica el estado de la lista para indicar se cargo exitosamente.
 * - Pone `loading = false` y `error = false`.
 * - Setea el `total` (length de los resultados).
 * - Setea `items` con los resultados.
 */
export function listSuccess<T>(state: ListState<T>, list: T[] = []) {
  state.items = list;
  state.total = list.length;
  state.status = {};
}

/**
 * Actualiza el estado de la lista en un Store luego de obtener un conjunto de entidades.
 * La diferencia con los otros metodos es que "listSuccess" pisa la lista existente e "infiniteListSuccess"
 * appendea la nueva lista.
 * En cambio este metodo solo agrega los nuevos items, los existentes los actualiza, dejando los demas sin modificar.
 *
 *  - Resetea el status (loading y error en false)
 *  - Inserta o actualiza el item en la lista:
 *    - Si se especifico predicate, lo usa para encontrar el item.
 *    - En caso contrario, busca por `id` y `codigo` (en ese orden de prioridad)
 *  - Actualiza el total de la lista
 */
export function entitiesSuccessInList<T>(
  list: ListState<T>,
  items: T[],
  opts?: { predicate?: ListItemPredicate<T>; overwrite?: OverwritePredicate<T> }
) {
  list.status = {};

  for (const item of items) {
    let predicate = opts?.predicate;
    if (!predicate) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      if (hasId(item)) predicate = byId(item.id) as any;
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      else if (hasCodigo(item)) predicate = byCodigo(item.codigo) as any;
      else throw new Error('Falta parametro predicate');
    }

    // Si el item esta en la lista, se actualiza. Si no se agrega
    list.items = insertOrUpdate(list.items, item, {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      predicate: predicate!,
      overwrite: opts?.overwrite
    });
  }

  list.total = list.items.length;
}

/**
 * Actualiza el estado de la lista en un Store luego de obtener una **unica** entidad.
 *
 *  - Resetea el status (loading y error en false)
 *  - Inserta o actualiza el item en la lista:
 *    - Si se especifico predicate, lo usa para encontrar el item.
 *    - En caso contrario, busca por `id`, `code` y `codigo` (en ese orden de prioridad)
 *  - Actualiza el total de la lista
 */
export function entitySuccessInList<T>(
  list: ListState<T>,
  item: T,
  opts?: { predicate?: ListItemPredicate<T>; overwrite?: OverwritePredicate<T> }
) {
  entitiesSuccessInList(list, [item], opts);
}

/**
 * Se modifica el estado de la lista infinita para indicar que se esta pidiendo resultados.
 * - Pone `loading = true` y `error = false`
 * - Setea el tipo especificado
 * - Si el tipo es `reload`, setea la fecha de primera query
 */
export function infiniteListRequest(state: InfiniteListState<unknown>, type: ListRequestMode) {
  state.status = { loading: true, type };

  // Si el tipo es reload, se setea la fecha actual como fecha de primera query
  if (type === 'reload') state.initialQueryTs = new Date();
}

/**
 * Se modifica el estado de la lista infinita para indicar que hubo un error.
 * - Pone `error = true` y `loading = false`
 * - Setea el tipo especificado
 */
export function infiniteListFail(state: InfiniteListState<unknown>, type: ListRequestMode) {
  state.status = { error: true, type };
}

/**
 * Se modifica el estado de la lista infinita para indicar se cargo exitosamente.
 * - Pone `loading = false` y `error = false`
 * - Setea el tipo especificado
 * - Setea los datos de la pagina: `hasNext`, `lastPage` y `total` (si existe).
 * - Segun el tipo, incluye los items (`more` los carga al final, `refresh` al principio y `reload` los pisa).
 */
export function infiniteListSuccess<T>(state: InfiniteListState<T>, page: Page<T>, type: ListRequestMode) {
  state.hasNext = page.next;
  state.lastPage = page.number;
  if (page.total) state.total = page.total;
  state.status = { type };
  // Si se agregan mas, van al final, si es refresh, van al comienzo.
  switch (type) {
    case 'more':
      state.items = [...state.items, ...page.items];
      break;
    case 'refresh':
      state.items = [...page.items, ...state.items];
      break;
    case 'reload':
      state.items = page.items;
  }
}

/**
 * Se modifica el estado de la lista paginada para indicar que se esta pidiendo resultados.
 * - Pone `loading = true` y `error = false`
 */
export function paginatedListRequest(state: PaginatedListState<unknown>) {
  state.status = { loading: true };
}

/**
 * Se modifica el estado de la lista paginada para indicar que hubo un error.
 * - Pone `error = true` y `loading = false`
 */
export function paginatedListFail(state: PaginatedListState<unknown>) {
  state.status = { error: true };
}

/**
 * Se modifica el estado de la lista paginada para indicar se cargo exitosamente.
 * - Pone `loading = false` y `error = false`
 * - Agrega la pagina a la lista de paginas (esta a su vez contiene los items, el numero de pagina, si hay siguiente)
 * - Setea el `total` (si existe).
 * - Setea la pagina recibida como actual
 */
export function paginatedListSuccess<T>(state: PaginatedListState<T>, page: Page<T>) {
  state.pages = [...state.pages, page];
  if (page.total) state.total = page.total;
  state.currentPageNumber = page.number;
  state.status = {};
}
