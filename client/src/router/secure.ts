import VueRouter, { Route, RouteRecord } from 'vue-router';
import { checkAccess } from './RouteSecurity';
import { sessionStore } from '@/store';
import { Rol } from '@/model/admin/Member';
import { routerService } from '@/service';

/**
 * Calcula la seguridad de una ruta (anidada). La logica es:
 *  - Se usan los roles de la ruta mas especifica que los incluya.
 *  - Si ninguna ruta incluye roles, con que una este segurizada alcanza.
 *  - Si ninguna esta segurizada, la ruta completa no lo esta tampoco.
 *
 * @param route ruta para la cual se quiere calcular la seguridad.
 */
export function calculateRouteSecurity(route: Route): RouteSecurity {
  // Empieza no segurizada
  let result: RouteSecurity = false;

  route.matched.forEach((route: RouteRecord) => {
    const routeSecurity: RouteSecurity = route.meta.auth;

    if (Array.isArray(routeSecurity)) {
      // Si es lista de roles la ruta, sobreescribe el resultado
      result = routeSecurity;
    } else if (Array.isArray(result)) {
      // Si no es lista de roles y ya se encontro una lista, se deja la anterior
      return;
    }

    // Si ninguna es lista de roles, con que una sea true, el resultado debe serlo
    result = routeSecurity || result;
  });

  return result;
}

/**
 * Seguridad de una ruta.
 *
 * Puede ser:
 *  - Un valor boolean indicando si se requiere autenticacion o no,
 *  - Una lista de roles permitidos (OR)
 */
export type RouteSecurity = boolean | Rol[];

export function secureRouter(router: VueRouter) {
  router.beforeEach((routeTo, _routeFrom, next) => {
    // Comprueba si esta ruta requiere auth y roles (incluyendo rutas anidadas)
    const security: RouteSecurity = calculateRouteSecurity(routeTo);

    const authStore = {
      get roles() {
        return sessionStore.sesion ? sessionStore.sesion.roles : [];
      },
      get authenticated() {
        return sessionStore.authenticated;
      }
    };

    checkAccess(authStore, security, {
      onGranted() {
        // Se deja continuar
        next();
      },
      onLoginRequired() {
        // Se llama a login
        console.warn(
          `Pagina "${routeTo.name || routeTo.path}" no disponible para usuario anonimo, se intenta autenticacion`
        );
        next(routerService.login({ redirect: true }));
      },
      onRoleRequired(roles: string[]) {
        // Se redirige al home
        console.warn(
          `Pagina "${routeTo.name ||
            routeTo.path}" requiere que el usuario tenga al menos uno de estos roles: ${roles}. Se redirige al home`
        );
        next(false);
      }
    });
  });
}
