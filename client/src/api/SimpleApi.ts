import { toEntity, toList, queryString, searchOptionsToQuerystring, toPage } from '@/core/ajax/helpers';
import { IdModel } from '@/model/IdModel';
import { AxiosInstance } from 'axios';
import { SearchOptions, Sort } from '@/core/ajax/model';
import Page from '@/core/Page';

/**
 * API generica para hacer ABM simples.
 * Incluye logica para listar todos, obtener uno por id, crear, actualizar y borrar
 */
export class SimpleApi<T extends IdModel> {
  constructor(
    protected http: AxiosInstance,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private config: { baseUrl: string; searchUrl?: string; mapper?: (json: any) => T }
  ) {}

  /**
   * Lista de los items.
   * Permite opcionalmente enviar parametros para filtrar y ordenar
   * @param opts Parametros de ordenamiento y filtrado
   */
  list(opts?: { filter?: unknown; sort?: Sort }): Promise<T[]> {
    return this.http
      .get(this.config.baseUrl + queryString(searchOptionsToQuerystring(opts)))
      .then(toList(this.config.mapper));
  }

  /**
   * Obtiene una pagina de items.
   * @param opts Parametros de paginacion, ordenamiento y filtrado
   */
  searchPage(opts: SearchOptions<T>): Promise<Page<T>> {
    let url = this.config.baseUrl;
    if (this.config.searchUrl) url += this.config.searchUrl;
    return this.http.get(url + queryString(searchOptionsToQuerystring(opts))).then(toPage(this.config.mapper));
  }

  /** Obtiene un item por id */
  getById(id: number): Promise<T> {
    return this.http.get(`${this.config.baseUrl}/${id}`).then(toEntity(this.config.mapper));
  }

  /** Crea un nuevo item */
  create(entity: T): Promise<T> {
    return this.http.post(this.config.baseUrl, entity).then(toEntity(this.config.mapper));
  }

  /** Actualiza un item existente */
  update(entity: T) {
    return this.http.put(`${this.config.baseUrl}/${entity.id}`, entity).then(toEntity(this.config.mapper));
  }

  /** Crea o actualiza un item segun si ya tiene id o no */
  save(entity: T) {
    return entity.id ? this.update(entity) : this.create(entity);
  }

  /** Elimina un item */
  async delete(id: number) {
    await this.http.delete(`${this.config.baseUrl}/${id}`);
  }
}
