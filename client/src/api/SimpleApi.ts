import { toEntity, toList, queryString, searchOptionsToQuerystring, toPage } from '@/core/ajax/helpers';
import { IdModel, hasId } from '@/model/IdModel';
import { AxiosInstance } from 'axios';
import { SearchOptions, Sort } from '@/core/ajax/model';
import Page from '@/core/Page';
import { OptionalId, WithoutId } from '@/core/TypeHelpers';

interface ApiConfig<T extends IdModel> {
  /** URL Base de la API (se concatena al configurado para el HttpClient) */
  baseUrl: string;

  /**
   * URL especifica para la busqueda con filtros, orden y paginacion.
   * Si es la misma que para list() o no se usa, no es necesario definirla.
   */
  searchUrl?: string;

  /**
   * Funcion que mapea el json del servidor al tipo utilizado en el cliente.
   *
   * Se usa para mapear **un solo item**.
   * Cuando se mapean listas o paginas, se llama una vez para cada item.
   *
   * Es opcional. Si no se define, se utiliza el JSON como viene.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  mapper?: (json: any) => T;

  /**
   * Funcion que mapea una entidad del cliente al JSON que espera el servidor.
   *
   * Es opcional. Si no se define, se envia como esta.
   */
  upstreamMaper?: (entity: OptionalId<T>) => unknown;
}

/**
 * API generica para hacer ABM simples.
 * Incluye logica para listar todos, obtener uno por id, crear, actualizar y borrar
 */
export class SimpleApi<T extends IdModel> {
  constructor(protected http: AxiosInstance, private config: ApiConfig<T>) {}

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
  create(entity: WithoutId<T>): Promise<T> {
    return this.http.post(this.config.baseUrl, this.mapToJson(entity)).then(toEntity(this.config.mapper));
  }

  /** Actualiza un item existente */
  update(entity: T) {
    return this.http
      .put(`${this.config.baseUrl}/${entity.id}`, this.mapToJson(entity))
      .then(toEntity(this.config.mapper));
  }

  /** Crea o actualiza un item segun si ya tiene id o no */
  save(entity: OptionalId<T>) {
    return hasId(entity) ? this.update(entity as T) : this.create(entity);
  }

  /** Elimina un item */
  async delete(id: number) {
    await this.http.delete(`${this.config.baseUrl}/${id}`);
  }

  /** Convierte el item al formato JSON requerido por el server, caso de haber un mapper definido */
  private mapToJson(entity: OptionalId<T>): unknown {
    return this.config.upstreamMaper?.(entity) ?? entity;
  }
}
