import { RouteConfig } from 'vue-router';

/**
 * Seguridad de una ruta.
 *
 * Puede ser:
 *  - Un valor boolean indicando si se requiere autenticacion o no,
 *  - Una lista de roles permitidos (OR)
 */
export type RouteSecurity = boolean | string[];

/**
 * RouteConfig con seguridad.
 * Extiende el original pero especifica la config de seguridad en el atributo "meta".
 */
export interface SecureRouteConfig extends RouteConfig {
  meta?: {
    auth: RouteSecurity;
    [x: string]: unknown;
  };
}

interface CheckAccessCallbacks {
  /** Llamado cuando se permite el acceso */
  onGranted?(): void;

  /** Llamado cuando no se permite el acceso por no estar autenticado */
  onLoginRequired?(): void;

  /**
   * Llamado cuando no se permite el acceso porque no tiene ninguno de los roles requeridos
   * @param roles lista de roles de los cuales al menos uno es necesario
   */
  onRoleRequired?(roles: string[]): void;
}

interface AuthInfo {
  authenticated: boolean;
  roles: string[];
}

/**
 * Comprueba el acceso a una ruta.
 *
 * Devuelve true si se permite el acceso y false en caso contrario
 *
 * @param authInfo informacion del usuario actual
 * @param security seguridad de la ruta
 * @param callbacks callbacks que se llaman para realizar acciones custom en cada caso (opcional).
 */
export function checkAccess(authInfo: AuthInfo, security: RouteSecurity, callbacks: CheckAccessCallbacks = {}) {
  // Si no requiere auth, se permite el acceso
  if (!security) {
    callbacks.onGranted?.();
    return true;
  }

  if (!authInfo.authenticated) {
    // Si require auth y el usuario no esta logueado, no se permite el acceso
    callbacks.onLoginRequired?.();
    return false;
  }

  if (Array.isArray(security)) {
    // Si la seguridad de la ruta es la lista de roles, el usuario debe tener al menos uno para acceder
    if (authInfo.roles.some(r => security.includes(r))) {
      callbacks.onGranted?.();
      return true;
    } else {
      // Si no tiene ningun rol de los requeridos, no se permite el acceso
      callbacks.onRoleRequired?.(security);
      return false;
    }
  } else {
    // Si la seguridad de la ruta no es una lista de roles, simplemente alcanza con que este autenticado
    callbacks.onGranted?.();
    return true;
  }
}
