import { toEntity, toList } from '@/core/ajax/helpers';
import { IdModel } from '@/model/IdModel';
import { AxiosInstance } from 'axios';

/**
 * API generica para hacer ABM simples.
 * Incluye logica para listar todos, obtener uno por id, crear, actualizar y borrar
 */
export class SimpleApi<T extends IdModel> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  constructor(protected http: AxiosInstance, private config: { baseUrl: string; mapper?: (json: any) => T }) {}

  /** Lista todos los items */
  list(): Promise<T[]> {
    return this.http.get(`${this.config.baseUrl}`).then(toList(this.config.mapper));
  }

  /** Obtiene un item por id */
  getById(id: number): Promise<T> {
    return this.http.get(`${this.config.baseUrl}/${id}`).then(toEntity(this.config.mapper));
  }

  /** Crea un nuevo item */
  crear(entity: T): Promise<T> {
    return this.http.post(this.config.baseUrl, entity).then(toEntity(this.config.mapper));
  }

  /** Actualiza un item existente */
  update(entity: T) {
    return this.http.put(`${this.config.baseUrl}/${entity.id}`, entity).then(toEntity(this.config.mapper));
  }

  /** Elimina un item */
  async borrar(id: number) {
    await this.http.delete(`${this.config.baseUrl}/${id}`);
  }
}
