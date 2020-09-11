import { queryString, toEntity, toList } from '@/core/ajax/helpers';
import { Cuenta, CuentaOCategoria, mapCuenta, mapCuentaOCategoria } from '@/model/Cuenta';
import { AxiosInstance } from 'axios';

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
      balanceaResultados: cuenta.balanceaResultados
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
  search(query: string): Promise<Cuenta[]> {
    return this.http.get(`${BASE_URL}${queryString({ query })}`).then(toList(mapCuenta));
  }

  /**
   * Busca una categoria o cuenta por parte del codigo, descripcion o alias.
   * Se usa en el autocomplete
   * @param query
   */
  searchWithCategories(query: string): Promise<CuentaOCategoria[]> {
    return this.http.get(`${BASE_URL}${queryString({ query, categories: true })}`).then(toList(mapCuentaOCategoria));
  }

  /**
   * Busca multiples cuentas por id
   * @param ids ids de las cuentas a buscar
   */
  findByIds(ids: number[]): Promise<Cuenta[]> {
    return this.http.get(`${BASE_URL}${queryString({ ids: ids.join(',') })}`).then(toList(mapCuenta));
  }
}
