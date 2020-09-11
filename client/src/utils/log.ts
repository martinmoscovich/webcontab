import { WebContabError } from '@/core/ajax/error';

/**
 * Loguea un error en la consola
 * @param accion accion que se queria realizar
 * @param error error que ocurrio
 */
export function logError(accion: string, error: WebContabError) {
  console.error('Error al %s: %o', accion, error);
}
