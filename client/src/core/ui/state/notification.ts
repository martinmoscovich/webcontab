import { ToastProps, DialogProps } from '../notification';
import Showable from '../Showable';

/**
 * Interfaz que representa el estado actual de los toast (alertas no bloqueantes) en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx)
 *
 * @export
 */
export interface ToastState extends ToastProps, Showable {}

/**
 * Interfaz que representa el estado actual del Dialog (modal bloqueante) en la aplicacion.
 * Sirve para aplicaciones cuya UI es funcion del estado central actual (vuex, redux, ngrx)
 *
 * @export
 */
export interface DialogState extends DialogProps, Showable {}

/**
 * Devuelve el estado inicial para los Toast en una aplicacion.
 * Este estado hace que no se muestre ninguno.
 *
 * @export
 * @returns {ToastState} estado inicial
 */
export function getInitialToastState(): ToastState {
  return { show: false, message: '', type: 'information' };
}

/**
 * Devuelve el estado inicial para el Dialog en una aplicacion.
 * Este estado hace que el Modal esta oculto.
 *
 * @export
 * @returns {DialogState} estado inicial
 */
export function getInitialDialogState(): DialogState {
  // Se castea a any porque ToastState tiene duration y DialogState no
  return { ...getInitialToastState(), title: '' };
}
