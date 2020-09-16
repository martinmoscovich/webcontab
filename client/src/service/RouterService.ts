import { Dictionary, Location, Route } from 'vue-router/types/router';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import router from '@/router';
import { IdModel } from '@/model/IdModel';
import { parseQueryString } from '@/utils/browser';
import { parseRequestOptionsFromQS, paginationToQuerystring } from '@/core/ajax/helpers';
import { SearchOptions } from '@/core/ajax/model';

const HOME_ROUTE: Location = { name: 'Home' };
const LOGIN_ROUTE: Location = { name: 'Login' };
const VER_ASIENTO_ROUTE: Location = { name: 'DetalleAsiento' };
const NUEVO_ASIENTO_ROUTE: Location = { name: 'NuevoAsiento' };
const LISTA_PROVINCIAS_ROUTE: Location = { name: 'ListaProvincias' };
const INFLACION_ROUTE: Location = { name: 'Inflacion' };
const LISTA_USUARIOS_ROUTE: Location = {
  name: 'ListaUsuarios'
};
const VER_USUARIO_ROUTE: Location = {
  name: 'DetalleUsuario'
};
const NUEVO_USUARIO_ROUTE: Location = {
  name: 'NuevoUsuario'
};
const LISTA_MONEDAS_ROUTE: Location = {
  name: 'ListaMonedas'
};
const VER_MONEDA_ROUTE: Location = {
  name: 'DetalleMoneda'
};
const NUEVA_MONEDA_ROUTE: Location = {
  name: 'NuevaMoneda'
};
const LISTA_ORGANIZACIONES_ROUTE: Location = {
  name: 'ListaOrganizaciones'
};
const VER_ORGANIZACION_ROUTE: Location = {
  name: 'DetalleOrganizacion'
};
const NUEVA_ORGANIZACION_ROUTE: Location = {
  name: 'NuevaOrganizacion'
};
const LISTA_EJERCICIOS_ROUTE: Location = {
  name: 'ListaEjercicios'
};

/**
 * Servicio de ruteo de la aplicacion.
 * Permite obtener las rutas para usar en los links o pedir la navegacion.
 */
export class RouterService {
  /** Indica si la ruta actual es la indicada */
  isCurrent(current: Route, route: Location) {
    return current.name === route.name;
  }

  goHome() {
    router.push(HOME_ROUTE);
  }
  login(opts?: { redirect?: boolean }) {
    let redirect: string | undefined = window.location.pathname + window.location.search;

    // Si no se pidio hacer redirect o es la raiz, se ignora
    if (!opts?.redirect || redirect === '/') redirect = undefined;

    return {
      ...LOGIN_ROUTE,
      query: { redirect }
    };
  }
  goToLogin(opts?: { redirect?: boolean }) {
    router.push(this.login(opts));
  }
  goToCuenta(cuenta: IdModel, query?: Dictionary<string | (string | null)[]>) {
    router.push({
      name: 'CuentaView',
      params: { id: cuenta.id.toString() },
      query
    });
  }

  categoria(categoria?: IdModel | null) {
    const params = categoria ? { id: categoria.id.toString() } : undefined;
    return {
      name: 'CategoriaView',
      params
    };
  }

  goToCategoria(categoria?: IdModel | null) {
    router.push(this.categoria(categoria));
  }

  asiento(asiento: IdModel) {
    return {
      ...VER_ASIENTO_ROUTE,
      params: {
        id: asiento.id.toString()
      }
    };
  }
  goToAsiento(asiento: IdModel) {
    router.push(this.asiento(asiento));
  }

  nuevoAsiento() {
    return NUEVO_ASIENTO_ROUTE;
  }
  goToNuevoAsiento() {
    router.push(this.nuevoAsiento());
  }

  informe(name: 'DIARIO' | 'MAYOR' | 'BALANCE') {
    return { path: '/informes/' + name.toLowerCase() };
  }

  goToInforme(name: 'DIARIO' | 'MAYOR' | 'BALANCE') {
    router.push(this.informe(name));
  }

  organizaciones() {
    return LISTA_ORGANIZACIONES_ROUTE;
  }
  goToOrganizaciones() {
    router.push(this.organizaciones());
  }
  goToOrganizacion(org: IdModel) {
    router.push({
      ...VER_ORGANIZACION_ROUTE,
      params: { id: org.id.toString() }
    });
  }
  nuevaOrganizacion() {
    return NUEVA_ORGANIZACION_ROUTE;
  }
  goToNuevaOrganizacion() {
    router.push(this.nuevaOrganizacion());
  }

  ejercicios() {
    return LISTA_EJERCICIOS_ROUTE;
  }
  goToEjercicios() {
    router.push(this.ejercicios());
  }

  goToImputacion(imputacion: ImputacionDTO) {
    router.push({
      name: 'DetalleAsiento',
      params: {
        id: imputacion.asiento.id.toString()
      },
      query: {
        imputacion: imputacion.id.toString()
      }
    });
  }

  provincias() {
    return LISTA_PROVINCIAS_ROUTE;
  }
  goToProvincias() {
    router.push(this.provincias());
  }
  monedas() {
    return LISTA_MONEDAS_ROUTE;
  }
  moneda() {
    return VER_MONEDA_ROUTE;
  }
  monedaNueva() {
    return NUEVA_MONEDA_ROUTE;
  }
  goToMoneda(moneda: IdModel) {
    router.push({
      ...this.moneda(),
      params: { id: moneda.id.toString() }
    });
  }
  goToNuevaMoneda() {
    router.push(this.monedaNueva());
  }
  goToMonedas() {
    router.push(this.monedas());
  }
  inflacion() {
    return INFLACION_ROUTE;
  }
  usuarios() {
    return LISTA_USUARIOS_ROUTE;
  }
  usuario() {
    return VER_USUARIO_ROUTE;
  }
  usuarioNuevo() {
    return NUEVO_USUARIO_ROUTE;
  }
  goToUsuario(user: IdModel) {
    router.push({
      ...this.usuario(),
      params: { id: user.id.toString() }
    });
  }
  goToNuevoUsuario() {
    router.push(this.usuarioNuevo());
  }
  goToUsuarios() {
    router.push(this.usuarios());
  }
  adminAjustes() {
    return { name: 'AdminAjustes' };
  }

  perfil() {
    return { name: 'Perfil' };
  }

  /**
   * Parsea una ruta y obtiene el SearchOptions asociado (con param de paginacion, sort, filtro, etc)
   * @param route
   * @param opts
   */
  queryFromUrl<T>(
    route: Route,
    opts: {
      parse: (qs: Dictionary<string>) => SearchOptions<T>;
      defaultPageSize?: number;
    }
  ): SearchOptions<T> {
    const qs = parseQueryString(route.query);
    const query: SearchOptions<T> = {
      ...opts.parse(qs),
      ...parseRequestOptionsFromQS(qs)
    };

    if (opts.defaultPageSize) {
      if (!query.pagination) query.pagination = { page: 1 };
      if (!query.pagination.size) query.pagination.size = opts.defaultPageSize;
    }

    return query;
  }

  /**
   * Navega a la ruta especificada, creando un query string en base al filtro indicado
   * @param route
   * @param filter
   */
  updateFilter(route: Route, filter: Dictionary<string>) {
    router.push({
      name: route.name ?? undefined,
      query: {
        size: route.query.size,
        ...filter
      }
    });
  }

  /**
   * Navega a la ruta especificada, actualizando los parametros de paginacion
   * @param route ruta
   * @param page numero de pagina
   * @param size tamanio de pagina
   */
  updatePagination(route: Route, page: number, size?: number) {
    if (!size) {
      size = route.query.size ? parseInt(route.query.size as string, 10) : undefined;
    }

    router.push({
      name: route.name ?? undefined,
      query: {
        ...route.query,
        ...paginationToQuerystring({
          page,
          size
        })
      }
    });
  }
}
