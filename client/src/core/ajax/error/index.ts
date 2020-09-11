import { isUndefined } from '@/utils/general';

/**
 * Error base
 */
export class WebContabError extends Error {
  constructor(public code: string, public description: string) {
    super(description);
  }
}

/**
 * Error de conexion
 */
export class ConnectionError extends WebContabError {
  constructor() {
    super('connection_error', 'Error de conexion al servidor');
  }
}

/**
 * Error ocurrido en el server
 */
export class RemoteError extends WebContabError {
  constructor(public status: number, code: string, description: string) {
    super(code, description);
  }
}

/**
 * Error ocurrido en el cliente
 */
export class ApplicationError extends WebContabError {
  constructor(description: string) {
    super('application_error', description);
  }
}

/**
 * Determina si el error es de tipo RemoteError (error del servidor)
 * @param e error
 */
export function isRemoteError(e: unknown): e is RemoteError;
/**
 * Determina si el error es de tipo RemoteError (error del servidor) y tiene el status indicado
 * @param e error
 * @param status status deseado (ej: 404)
 */
export function isRemoteError(e: unknown, status: number): e is RemoteError;
/**
 * Determina si el error es de tipo RemoteError (error del servidor) y tiene el status y el code indicados
 * @param e error
 * @param status status deseado (ej: 404)
 * @param code code deseado (ej: entidad_not_found)
 */
export function isRemoteError(e: unknown, status: number, code: string): e is RemoteError;
export function isRemoteError(e: unknown, status?: number, code?: string): e is RemoteError {
  // Si no es RemoteError, devolver false
  if (!(e instanceof RemoteError)) return false;

  // Si no se especifico status, con que sea RemoteError alcanza
  if (isUndefined(status)) return true;

  // Si se definio status pero no coincide, es false
  if (e.status !== status) return false;

  // Si no se especifico code, con que sea RemoteError y tenga ese status alcanza
  if (isUndefined(code)) return true;

  // Debe coincidir el code
  return e.code === code;
}

/** Determina si el error es Remote Error dentro de los status indicados */
export function isAnyRemoteError(e: unknown, status: number[]) {
  return e instanceof RemoteError && status.includes(e.status);
}

/** Determina si el error especificado es request invalido (un RemoteError con status 400) */
export function isInvalidRequest(e: unknown): e is RemoteError {
  return isRemoteError(e, 400);
}

/** Determina si el error especificado es de autenticacion (un RemoteError con status 401) */
export function isNotAuthenticated(e: unknown): e is RemoteError {
  return isRemoteError(e, 401);
}

/** Determina si el error especificado es de autorizacion (un RemoteError con status 403) */
export function isForbidden(e: unknown): e is RemoteError {
  return isRemoteError(e, 403);
}

/** Determina si el error especificado es un Not Found (un RemoteError con status 404) */
export function isNotFound(e: unknown): e is RemoteError {
  return isRemoteError(e, 404);
}

/** Determina si el error especificado es un conflicto de negocio (un RemoteError con status 409) */
export function isConflict(e: unknown): e is RemoteError {
  return isRemoteError(e, 409);
}
