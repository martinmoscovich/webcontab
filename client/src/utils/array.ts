import { isDefined } from './general';

/** Tipo usado en funciones Predicate para filtrar */
export type ListItemPredicate<T> = (value: T, index: number, obj: T[]) => boolean;

/** Tipo usado en funciones que reciben 2 objetos y devuelven uno mergeado */
export type OverwritePredicate<T> = (existingItem: T, newItem: T) => T;

/**
 * Funcion para utilizar con el metodo Array.find() que permite buscar un item por id.
 *
 * Los elementos de la lista deben tener el atributo "id", que puede ser de cualquier tipo.
 * @param id id del elemento a buscar. Debe ser del mismo tipo que el de los elementos.
 *
 * @example return personas.find(byId(4)) -> Devuelve la persona con id 4 o undefined.
 */
export function byId<T>(id: T): ListItemPredicate<{ id: T }> {
  return (item: { id: T }) => item.id === id;
}

/**
 * Funcion para utilizar con el metodo Array.find() que permite buscar un item por "code" (en ingles).
 *
 * Los elementos de la lista deben tener el atributo "code" (en ingles) de tipo String.
 * @param code codigo del elemento a buscar
 *
 * @example return personas.find(byCode('ASD')) -> Devuelve la persona con code 'ASD' o undefined.
 */
export function byCode(code: string): ListItemPredicate<{ code: string }> {
  return (item: { code: string }) => item.code === code;
}

/**
 * Funcion para utilizar con el metodo Array.find() que permite buscar un item por codigo.
 *
 * Los elementos de la lista deben tener el atributo codigo de tipo String.
 * @param code codigo del elemento a buscar
 *
 * @example return personas.find(byCodigo('ASD')) -> Devuelve la persona con codigo 'ASD' o undefined.
 */
export function byCodigo(codigo: string): ListItemPredicate<{ codigo: string }> {
  return (item: { codigo: string }) => item.codigo === codigo;
}

/**
 * Construye una funcion de comparacion en base a un criterio de ordenamiento
 * @param sorting
 */
export function buildCompareFn<T>(sorting: { field: keyof T; descending?: boolean }) {
  return function compare(a: T, b: T) {
    const multiplier = sorting.descending ? -1 : 1;
    const x = a[sorting.field] ?? Number.MIN_VALUE;
    const y = b[sorting.field] ?? Number.MIN_VALUE;
    return multiplier * (x < y ? -1 : x > y ? 1 : 0);
  };
}

// /**
//  * Funcion para usar con array.sort() que ordena alfabeticamente
//  * @param a
//  * @param b
//  */
// export function <T> alphabeticalOrder(field: string) {
//   return (a: T, b: T) => {
//     if (a[field] < b[field]) return -1;
//     if (a[field] > b[field]) return 1;
//     return 0;
// }
// }

/**
 * Inserta o actualiza un item de la lista, buscandolo por el predicado que se pasa por parametro y devuelve una nueva lista.
 *
 * Para que funcione bien, se debe sobreescribir la lista anterior con esta.
 *
 * @param list
 * @param item
 * @param predicate funcion que determina si el item es el buscado
 * @param overwrite funcion que determina si el item nuevo debe sobreescribir el existente.
 */
export function insertOrUpdate<T>(
  list: T[],
  item: T,
  opts: { predicate: ListItemPredicate<T>; overwrite?: OverwritePredicate<T> }
): T[] {
  const existing = list.findIndex(opts.predicate);
  if (existing > -1) {
    if (opts.overwrite) item = opts.overwrite(list[existing], item);
    list = Object.assign([], list, { [existing]: item });
  } else {
    list = [...list, item];
  }
  return list;
}

/**
 * Inserta o actualiza un item de la lista, buscandolo por id y devuelve una nueva lista.
 *
 * Para que funcione bien, se debe sobreescribir la lista anterior con esta
 * @param list
 * @param item
 */
export function insertOrUpdateById<T extends { id: number | string }>(list: T[], item: T): T[] {
  return insertOrUpdate(list, item, { predicate: byId(item.id) });
}

/**
 * Inserta o actualiza un item de la lista, buscandolo por `code` y devuelve una nueva lista.
 *
 * Para que funcione bien, se debe sobreescribir la lista anterior con esta
 * @param list
 * @param item
 */
export function insertOrUpdateByCode<T extends { code: string }>(list: T[], item: T): T[] {
  return insertOrUpdate(list, item, { predicate: byCode(item.code) });
}

/**
 * Inserta o actualiza un item de la lista, buscandolo por `codigo` y devuelve una nueva lista.
 *
 * Para que funcione bien, se debe sobreescribir la lista anterior con esta
 * @param list
 * @param item
 */
export function insertOrUpdateByCodigo<T extends { codigo: string }>(list: T[], item: T): T[] {
  return insertOrUpdate(list, item, { predicate: byCodigo(item.codigo) });
}

/**
 * Funcion para usar con reduce() que devuelve la suma de los elementos
 *
 * @param acum
 * @param value
 */
export function reduceSum(acum: number, value: number) {
  return acum + value;
}

/**
 * Suma los valores de un atributo de un array de objetos
 * @param items array de objetos
 * @param field atributo que se debe sumar
 */
export function sumItems<T>(items: T[], field: keyof T): number {
  if (!items) return 0;
  return items.map(i => ((i[field] as unknown) as number) || 0).reduce(reduceSum, 0);
}

/**
 * Dado un array, recorre sus elementos y obtiene el menor valor del atributo indicado
 */
export function min<T, K extends keyof T>(items: T[], field: K): T[K] | null {
  if (!items) return null;
  return items
    .map(i => i[field])
    .reduce((result: T[K] | null, value: T[K]) => (result === null || value < result ? value : result), null);
}

/**
 * Dado un array, recorre sus elementos y obtiene el mayor valor del atributo indicado
 */
export function max<T, K extends keyof T>(items: T[], field: K): T[K] | null {
  if (!items) return null;
  return items
    .map(i => i[field])
    .reduce((result: T[K] | null, value: T[K]) => (result === null || value > result ? value : result), null);
}

/**
 * Dado un array, retorna otro sin valores repetidos.
 * @param array
 */
export function distinct<T>(array: T[]) {
  return array.filter((v, i, a) => a.indexOf(v) === i);
}

/**
 * Dado un array, retorna solo los items definidos (que no son null ni undefined)
 * @param array
 */
export function getDefinedItems<T>(array: T[]): T[] {
  return array.filter(isDefined);
}
