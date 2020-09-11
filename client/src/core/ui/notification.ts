import { Location } from 'vue-router';

/**
 * Tipos de alertas disponibles
 */
export type AlertType = 'information' | 'success' | 'error' | 'warning';

interface MessageProps {
  /**
   *  Mensaje a mostrar
   */
  message: string;

  /**
   * Tipo de alerta
   */
  type: AlertType;
}

/**
 * Estilo de Toast.
 * El default es el Snackbar Material, pero puede ser el de Twitter cuando hay nuevos Tweets.
 * Tambien puede ser otro que se desee.
 */
export type ToastStyle = 'material' | 'twitter' | string;

/**
 * Interfaz que representa las propiedades de un Toast
 *
 * @export
 */
export interface ToastProps extends MessageProps {
  /**
   * Tiempo que debe durar el Toast en pantalla.
   * Permite configurar distintos tiempos por mensaje.
   */
  duration?: number;

  /**
   * Estilo de Toast.
   */
  style?: ToastStyle;
}

/**
 * Interfaz que representa las propiedades de un Snackbar.
 * Es como un Toast, pero permite acciones.
 *
 * @export
 */
export interface SnackbarProps extends ToastProps {
  /**
   * Texto de la accion
   */
  actionText?: string;

  /**
   * Ubicacion del VueRouter a la que navegar cuando se hace click en la accion.
   * Si esta definida, se ignoran href y onAction
   */
  to?: Location;

  /**
   * URL a la que navegar cuando se hace click en la accion.
   * Si esta definida, se ignora onAction
   */
  href?: string;

  /** Handler del click en la accion. */
  onAction?: () => void;
}

/**
 * Interfaz que representa las propiedades de un Dialog
 *
 * @export
 */
export interface DialogProps extends MessageProps {
  /**
   * Titulo del Dialog
   */
  title?: string;
}
