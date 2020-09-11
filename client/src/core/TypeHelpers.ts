/**
 * Tipo que devuelve una lista de nombres de propiedades de T que son del tipo O.
 *
 * Ej Persona {id: number, nombre: string, edad: number} - KeysOfType<Persona, number> = 'id'|'edad'
 */
export type KeysOfType<T, O> = {
  [P in keyof T]: T[P] extends O ? P : never;
}[keyof T];

/**
 * Variante de Pick que en lugar de filtrar por nombre de atributo, filtra por el tipo del atributo.
 *
 * Dado un tipo, solo preserva los atributos que NO son del tipo (number, string, etc) pasado como segundo parametro.
 */
export type PickByNotType<T, O> = Pick<T, KeysNotOfType<T, O>>;

/**
 * Tipo que devuelve una lista de nombres de propiedades de T que NO son del tipo O.
 *
 * Ej Persona {id: number, nombre: string, edad: number} - KeysOfType<Persona, number> = 'nombre'
 */
export type KeysNotOfType<T, O> = {
  [P in keyof T]: T[P] extends O ? never : P;
}[keyof T];

/**
 * Tipo que modifica uno existente para permitir que sus propiedades sean null
 */
export type Nullable<T> = {
  [P in keyof T]: T[P] | null;
};
