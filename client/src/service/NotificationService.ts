import { ToastProgrammatic as Toast } from 'buefy';
import { SnackbarProgrammatic as Snackbar } from 'buefy';
import { ToastProps, AlertType, SnackbarProps } from '@/core/ui/notification';
import { WebContabError, isRemoteError } from '@/core/ajax/error';
import { isDefined } from '@/utils/general';
import router from '@/router';

function isSnackbar(toast: ToastProps | SnackbarProps): toast is SnackbarProps {
  return isDefined((toast as SnackbarProps).actionText);
}

function getActionHandler(toast: SnackbarProps): (() => void) | undefined {
  if (toast.to) {
    return function() {
      if (toast.to) router.push(toast.to);
    };
  }
  if (toast.href) {
    return function() {
      if (toast.href) window.location.href = toast.href;
    };
  }
  return toast.onAction;
}

/**
 * Servicio de notificaciones.
 * Muestra mensajes al usuario
 */
export class NotificationService {
  /**
   * Muestra un mensaje en la aplicacion, usando los parametros:
   * Mensaje, duracion y tipo
   */
  // show(toast: ToastProps): void;
  // show(toast: SnackbarProps): void;
  show(toast: ToastProps | SnackbarProps): void {
    if (isSnackbar(toast)) {
      Snackbar.open({
        message: toast.message,
        duration: toast.duration,
        type: this.getType(toast.type),
        position: 'is-top',
        actionText: toast.actionText ?? null,
        onAction: getActionHandler(toast)
      });
    } else {
      Toast.open({
        message: toast.message,
        duration: toast.duration ?? 3500,
        type: this.getType(toast.type)
      });
    }
  }

  /**
   * Muestra un mensaje de error a partir de una excepcion
   * @param error excepcion
   * @param props duracion
   */
  error(error: WebContabError, props?: Partial<SnackbarProps>): void;
  /**
   * Muestra un mensaje de error
   * @param error mensaje
   * @param props duracion
   */
  error(message: string, props?: Partial<SnackbarProps>): void;
  error(messageOrError: string | WebContabError, props?: Partial<SnackbarProps>): void {
    if (typeof messageOrError === 'string') {
      this.show({ type: 'error', message: messageOrError, ...props });
    } else {
      this.show({
        type: 'error',
        message: this.getMessageFromError(messageOrError),
        ...props
      });
    }
  }
  /** Muestra un mensaje de informacion */
  info(message: string) {
    this.show({ type: 'information', message });
  }
  /** Muestra un mensaje de warning */
  warn(message: string) {
    this.show({ type: 'warning', message });
  }
  /** Muestra un mensaje de success */
  success(message: string) {
    this.show({ type: 'success', message });
  }

  private getType(type: AlertType) {
    switch (type) {
      case 'information':
        return 'is-info';
      case 'success':
        return 'is-success';
      case 'warning':
        return 'is-warning';
      case 'error':
        return 'is-danger';
    }
  }

  /**
   * Extrae el mensaje de una excepcion
   * @param error
   */
  private getMessageFromError(error: Error) {
    if (isRemoteError(error)) {
      switch (error.status) {
        case 404:
          return error.description.substring(0, error.description.indexOf('con id'));
        case 500:
          return 'Error en el servidor';
      }
    }
    return error.message;
  }
}
