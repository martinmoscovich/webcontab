import { Dictionary } from 'vue-router/types/router';

/** Determina si el parametro es null */
export function isNull<T>(value: T | null): value is null {
  return value === null;
}

/** Determina si el parametro es undefined */
export function isUndefined<T>(value: T | undefined): value is undefined {
  return value === undefined;
}

/** Determina si el parametro es null o undefined */
export function isNullOrUndefined<T>(value: T | null | undefined): value is null | undefined {
  return isNull(value) || isUndefined(value);
}

/** Determina si el parametro NO es null */
export function isNotNull<T>(value: T | null): value is T {
  return value !== null;
}

/** Determina si el parametro NO es undefined */
export function isNotUndefined<T>(value: T | undefined): value is T {
  return value !== undefined;
}

/** Determina si el parametro NO es null NI undefined */
export function isNotNullOrUndefined<T>(value: T | null | undefined): value is T {
  return isNotNull(value) && isNotUndefined(value);
}

/** Alias para `isNotNullOrUndefined()` */
export const isDefined = isNotNullOrUndefined;

/** Dado un objeto, devuelve si todos sus atributos son null o undefined */
export function allUndefined(obj: Dictionary<unknown>): boolean {
  return !Object.values(obj).some(isDefined);
}

/**
 * Ordena las keys del objeto segun el array.
 * Las keys que no estan en el segundo array quedan al final del nuevo objeto
 *
 * @param obj objeto a ordenar
 * @param keys lista de keys en el orden que deben ser ordenadas en el objeto
 */
export function sortKeys<T>(obj: T, keys: Array<keyof T>) {
  if (!obj) return obj;
  const result: T = {} as T;
  for (const key of keys) {
    result[key] = obj[key];
  }
  return Object.assign(result, obj);
}

/**
 * Comprueba si lo que se paso por parametro es un objeto (no un valor primitivo ni un array)
 * @param item
 * @returns {boolean}
 */
export function isObject(item: unknown): item is Dictionary<unknown> {
  return item && typeof item === 'object' && !Array.isArray(item);
}

/**
 * Convierte a numero solo si esta definido, si no devuelve undefined o null (segun el valor)
 */
export function toInt(value: string): number;
export function toInt(value: undefined): undefined;
export function toInt(value: null): null;
export function toInt(value: string | undefined | null) {
  return isDefined(value) ? parseInt(value, 10) : value;
}

/**
 * Convierte a decimal solo si esta definido, si no devuelve undefined o null (segun el valor)
 */
export function toFloat(value: string | undefined | null) {
  return isDefined(value) ? parseFloat(value) : value;
}

/**
 * Mergea dos o mas objetos de manera recursiva dentro del target (primer parametro).
 *
 * Para valores primitivos, los pisa, pero para objetos, los recorre chequeando sus atributos.
 *
 * Es una version "deep" de Object.assign().
 *
 * @param target objeto donde se quiere mergear
 * @param ...sources uno o mas objetos de donde saldran los datos. Los ultimos pisaran a los primeros.
 */
function deepMerge(target: unknown, ...sources: unknown[]): Dictionary<unknown> {
  if (!sources.length) {
    return isObject(target) ? target : {};
  }
  const source = sources.shift();

  if (isObject(target) && isObject(source)) {
    for (const key in source) {
      if (isObject(source[key])) {
        if (!target[key]) Object.assign(target, { [key]: {} });
        deepMerge(target[key], source[key]);
      } else {
        Object.assign(target, { [key]: source[key] });
      }
    }
  }

  return deepMerge(target, ...sources);
}

/**
 * Hace deep merge pero en caso de un atributo undefined, deja el que estaba en lugar de pisarlo con undefined.
 * @param target
 * @param ...sources
 */
export function deepMergeOnlyDefined(target: unknown, ...sources: unknown[]): Dictionary<unknown> {
  sources.forEach(obj => {
    if (isObject(obj)) {
      Object.keys(obj).forEach(key => obj[key] === undefined && delete obj[key]);
    }
  });
  return deepMerge(target, ...sources);
}
