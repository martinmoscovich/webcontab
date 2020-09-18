import { queryString, toEntity, toList, toPage } from '@/core/ajax/helpers';
import { Cuenta, CuentaOCategoria, mapCuenta, mapCuentaOCategoria } from '@/model/Cuenta';
import { AxiosInstance } from 'axios';
import Page from '@/core/Page';

const BASE_URL = '/cuentas';

/**
 * API que maneja las cuentas
 */
export class CuentaApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Crea una nueva cuenta
   * @param cuenta
   */
  crear(cuenta: Cuenta): Promise<Cuenta> {
    return this.http.post(BASE_URL, cuenta).then(toEntity(mapCuenta));
  }

  /**
   * Actualiza una cuenta
   * @param cuenta
   */
  update(cuenta: Cuenta) {
    // Estos son los datos que se pueden actualizar
    const payload: Partial<Cuenta> = {
      descripcion: cuenta.descripcion,
      monedaId: cuenta.monedaId,
      activa: cuenta.activa,
      individual: cuenta.individual,
      ajustable: cuenta.ajustable,
      balanceaResultados: cuenta.balanceaResultados,
      balanceaAjustables: cuenta.balanceaAjustables
    };
    return this.http.put(`${BASE_URL}/${cuenta.id}`, payload).then(toEntity(mapCuenta));
  }

  /**
   * Elimina una cuenta
   * @param cuenta
   */
  borrar(cuenta: Cuenta): Promise<void> {
    return this.http.delete(`${BASE_URL}/${cuenta.id}`);
  }

  /**
   * Obtiene una cuenta por id
   * @param id id de la cuenta
   * @param opts  indica si se debe traer el path de la cuenta
   */
  getById(id: number, opts: { path?: boolean } = {}): Promise<Cuenta> {
    return this.http.get(`${BASE_URL}/${id}${queryString(opts)}`).then(toEntity(mapCuenta));
  }

  /**
   * Busca una cuenta por parte del codigo, descripcion o alias.
   * Se usa en el autocomplete
   * @param query
   */
  search(query: string, page?: number): Promise<Page<Cuenta>> {
    return this.http.get(`${BASE_URL}${queryString({ query, page })}`).then(toPage(mapCuenta));
  }

  /**
   * Busca una categoria o cuenta por parte del codigo, descripcion o alias.
   * Se usa en el autocomplete
   * @param query
   */
  searchWithCategories(query: string, page?: number): Promise<Page<CuentaOCategoria>> {
    return this.http
      .get(`${BASE_URL}${queryString({ query, categories: true, page })}`)
      .then(toPage(mapCuentaOCategoria));
  }

  /**
   * Busca multiples cuentas por id
   * @param ids ids de las cuentas a buscar
   */
  findByIds(ids: number[]): Promise<Cuenta[]> {
    return this.http.get(`${BASE_URL}${queryString({ ids: ids.join(',') })}`).then(toList(mapCuenta));
  }
}
