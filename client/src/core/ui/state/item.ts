import { LoadingStatus } from '../loading';

export { entitySuccessInList } from './list';

/**
 * Interfaz que representa el estado de una entidad en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx).
 *
 * @export
 * @template T tipo del item a guardar
 */
export interface EntityState<T> {
  item?: T;
  status: LoadingStatus;
}

/**
 * Se modifica el estado de la entidad para indicar que se esta pidiendo resultados.
 * - Pone `loading = true` y `error = false`
 * - Borra el item actual, si existia.
 */
export function entityRequest(state: EntityState<never>) {
  state.item = undefined;
  state.status = { loading: true };
}

/**
 * Se modifica el estado de la entidad para indicar que hubo un error.
 * - Pone `error = true` y `loading = false`
 */
export function entityFail(state: EntityState<never>) {
  state.status = { error: true };
}

/**
 * Se modifica el estado de la entidad para indicar se cargo exitosamente.
 * - Pone `loading = false` y `error = false`.
 * - Setea el `item` con el resultado.
 */
export function entitySuccess<T>(state: EntityState<T>, item: T) {
  state.item = item;
  state.status = {};
}
