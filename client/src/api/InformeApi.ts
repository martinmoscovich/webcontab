import {
  toPage,
  queryString,
  toEntity,
  searchOptionsToQuerystring,
  toQuerystringDictionary
} from '@/core/ajax/helpers';
import Page from '@/core/Page';
import { BalanceItem, BalanceTotal } from '@/model/Balance';
import { AxiosInstance } from 'axios';
import { SearchOptions } from '@/core/ajax/model';
import { Dictionary } from 'vue-router/types/router';
import { Periodo } from '@/model/Periodo';
import { AsientosSearchOptions, AsientosSearchFilter } from '@/api/AsientoApi';
import { ImputacionesCuenta, mapImputacionesCuentaFromServer } from '@/model/ImputacionesCuenta';

const BASE_URL = '/informes';

/** Filtro para balances, permite filtrar cuentas y asientos */
export interface BalanceFilter extends Partial<Periodo> {
  /** Si se define, se busca solo el balance de las cuentas descendientes de esta categoria */
  categoria?: number;

  /** Si es true, se incluyen las cuentas cuyos balances son 0 */
  cero?: boolean;
}

/**
 * Query params posibles al pedir balance.
 * Incluye paginacion y filtro
 */
export type BalanceSearchOptions = SearchOptions<BalanceFilter>;

/**
 * API para obtener datos y URLs de los informes (diario, mayor y balance)
 */
export class InformeApi {
  constructor(private http: AxiosInstance) {}

  // /** Genera los query params a partir de un filtro de balance */
  // filterToQuerystring(filter: BalanceFilter): Dictionary<string> {
  //   if (!filter) return {};

  //   const qs: Dictionary<string> = {};
  //   if (filter.desde) qs.desde = formatDateForServer(filter.desde);
  //   if (filter.hasta) qs.hasta = formatDateForServer(filter.hasta);
  //   if (filter.categoria) qs.categoria = filter.categoria.toString();
  //   if (filter.cero) qs.cero = filter.cero.toString();

  //   return qs;
  // }

  /**
   * Obtiene una pagina del balance en base a los filtros
   * @param opts filtros y paginacion
   */
  getBalance(opts: BalanceSearchOptions): Promise<Page<BalanceItem>> {
    const qs: Dictionary<string> = searchOptionsToQuerystring(opts);
    // const qs: Dictionary<string | number> = {
    //   ...paginationAndSortToQuerystring(opts.pagination, opts.sort),
    //   ...toQuerystringDictionary(opts.filter)
    //   // ...this.filterToQuerystring(opts.filter)
    // };
    // if (opts.count) qs.count = "true";

    return this.http.get(`${BASE_URL}/balance${queryString(qs)}`).then(toPage());
  }

  /**
   * Obtiene los saldos totales del balance para cada moneda.
   * @param filter filtro que limita las cuentas y asientos incluidos
   * @returns Un mapa cuya clave es el id de la moneda y el valor el saldo en dicha moneda
   */
  getBalanceTotales(filter: BalanceFilter): Promise<BalanceTotal> {
    // const qs: Dictionary<string | number> = {
    //   ...this.filterToQuerystring(filter)
    // };
    const qs = toQuerystringDictionary(filter);

    return this.http.get(`${BASE_URL}/balance/totales${queryString(qs)}`).then(toEntity());
  }

  /**
   * Obtiene una pagina del mayor de una cuenta.
   * @param cuentaId id de la cuenta a buscar
   * @param opts datos de paginacion y filtro de los asientos a considerar
   * @returns una pagina del mayor y el saldo anterior en caso de existir
   */
  getMayor(cuentaId: number, opts: AsientosSearchOptions): Promise<ImputacionesCuenta> {
    const qs: Dictionary<string | number> = searchOptionsToQuerystring(opts);
    qs.cuenta = cuentaId;
    if (!qs.size) qs.size = 15;

    return this.http.get(`${BASE_URL}/mayor${queryString(qs)}`).then(toEntity(mapImputacionesCuentaFromServer));
  }

  /**
   * Genera la URL para pedir la generacion de un reporte diario en Excel
   * @param filter filtro de los asientos a incluir
   */
  getExportarDiarioUrl(filter: AsientosSearchFilter): string {
    // const qs: Dictionary<string | number> = asientoApi.filterToQuerystring(
    //   filter
    // );

    const qs: Dictionary<string> = toQuerystringDictionary(filter);

    return `${this.http.defaults.baseURL}${BASE_URL}/diario${queryString(qs)}`;
  }

  /**
   * Genera la URL para pedir la generacion de un reporte mayor en Excel
   * @param selector indica que cuentas incluir, ya se explicitamente o mediante sus categorias
   * @param filter filtro de los asientos a incluir
   */
  getExportarMayorUrl(selector: { categorias?: number[]; cuentas?: number[] }, filter: AsientosSearchFilter): string {
    // const qs: Dictionary<string | number> = asientoApi.filterToQuerystring(
    //   filter
    // );
    const qs: Dictionary<string> = toQuerystringDictionary(filter);
    if (selector.cuentas && selector.cuentas.length > 0) {
      qs.cuentas = selector.cuentas.join(',');
    }
    if (selector.categorias && selector.categorias.length > 0) {
      qs.categorias = selector.categorias.join(',');
    }

    return `${this.http.defaults.baseURL}${BASE_URL}/mayor/xls${queryString(qs)}`;
  }

  /**
   * Genera la URL para pedir la generacion de un reporte balance en Excel
   * @param filter filtro de las cuentas y asientos a incluir
   */
  getExportarBalanceUrl(filter: BalanceFilter): string {
    // const qs: Dictionary<string | number> = {
    //   ...this.filterToQuerystring(filter)
    // };
    const qs: Dictionary<string> = toQuerystringDictionary(filter);

    return `${this.http.defaults.baseURL}${BASE_URL}/balance/xls${queryString(qs)}`;
  }
}
