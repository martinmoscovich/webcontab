import { SimpleApi } from '@/api/SimpleApi';
import {
  entitySuccessInList,
  getInitialListState,
  listFail,
  listRequest,
  ListState,
  listSuccess
} from '@/core/ui/state/list';
import { IdModel } from '@/model/IdModel';
import { notificationService } from '@/service';
import { byId } from '@/utils/array';
import { logError } from '@/utils/log';
import { Action, Mutation, RegisterOptions, VuexModule } from 'vuex-class-modules';
import { capitalize } from '@/utils/text';
import { WebContabError } from '@/core/ajax/error';

/**
 * Store base para ABM simples.
 */
export abstract class BaseSimpleStore<T extends IdModel> extends VuexModule {
  /** Lista de items cacheados */
  lista: ListState<T> = getInitialListState();

  /** Id del ultimo item creado o actualizado */
  protected lastSavedId: number | null = null;

  /** API para comunicarse con el server */
  protected api: SimpleApi<T>;

  /** Nombre del tipo de entidad en singular (para log) */
  protected entidad: string;

  /** Nombre del tipo de entidad en plural (para log) */
  protected entidades: string;

  constructor(config: { api: SimpleApi<T>; entidad: string; entidades: string }, options: RegisterOptions) {
    super(options);
    this.api = config.api;
    this.entidad = config.entidad;
    this.entidades = config.entidades;
  }

  /** Busca un item por id en la cache */
  get find() {
    return (id: number) => this.lista.items.find(byId(id));
  }

  /** Devuelve el ultimo item creado o actualizado. O null si no existe */
  get lastSaved() {
    if (!this.lastSavedId) return null;
    return this.find(this.lastSavedId);
  }

  /** Vuelve el store a su estado inicial */
  @Mutation
  reset() {
    this.lista = getInitialListState();
    this.lastSavedId = null;
  }

  /**
   * Obtiene la lista de items de la cache o del servidor.
   *
   * @param refresh si es true, siempre va al servidor.
   */
  @Action
  async list(opts?: { refresh: boolean }) {
    try {
      this.listRequest();

      // Si no hay items o se pidio refresh, va al server. Si no, local
      const items = this.lista.items.length === 0 || opts?.refresh ? await this.api.list() : this.lista.items;
      this.listSuccess(items);
    } catch (e) {
      this.listError(e);
    }
  }

  /** Busca un item por id en la cache o en el server local */
  @Action
  async findById(payload: { id: number; refresh: boolean }) {
    try {
      this.findByIdRequest();

      // Si no se pidio refresh, se busca en local
      let item = !payload.refresh ? this.lista.items.find(byId(payload.id)) : null;

      // Si no existe en local o se pidio refresh, se llama al server
      if (!item) item = await this.api.getById(payload.id);
      this.findByIdSuccess(item);
    } catch (e) {
      this.findByIdError(e);
    }
  }

  /** Crea o actualiza un item */
  @Action
  async save(item: T) {
    await (item.id ? this.actualizar(item) : this.crear(item));
  }

  /** Crea un item */
  private async crear(item: T) {
    try {
      this.crearRequest();
      item = await this.api.create(item);
      this.crearSuccess(item);
    } catch (e) {
      this.crearError(e);
      throw e;
    }
  }

  /** Actualiza un item */
  private async actualizar(item: T) {
    try {
      this.actualizarRequest();
      item = await this.api.update(item);
      this.actualizarSuccess(item);
    } catch (e) {
      this.actualizarError(e);
      throw e;
    }
  }

  @Action
  async eliminar(item: T) {
    try {
      if (!item?.id) return;
      this.eliminarRequest();
      await this.api.delete(item.id);
      this.eliminarSuccess(item);
    } catch (e) {
      this.eliminarError(e);
      throw e;
    }
  }

  @Mutation
  private findByIdRequest() {
    listRequest(this.lista);
  }
  @Mutation
  findByIdSuccess(item: T) {
    entitySuccessInList(this.lista, item);
  }
  @Mutation
  private findByIdError(error: WebContabError) {
    listFail(this.lista);
    logError('buscar ' + this.entidad, error);
    notificationService.error(error);
  }

  @Mutation
  private listRequest() {
    listRequest(this.lista);
  }
  @Mutation
  private listSuccess(items: T[]) {
    listSuccess(this.lista, items);
  }
  @Mutation
  private listError(error: WebContabError) {
    listFail(this.lista);
    logError('buscar ' + this.entidad, error);
    notificationService.error(error);
  }

  @Mutation
  private crearRequest() {
    listRequest(this.lista);
  }
  @Mutation
  private crearSuccess(item: T) {
    entitySuccessInList(this.lista, item);
    this.lastSavedId = item.id;
    notificationService.success(capitalize(this.entidad) + ' guardada correctamente');
  }
  @Mutation
  private crearError(error: WebContabError) {
    listFail(this.lista);
    logError('crear ' + this.entidad, error);
    notificationService.error(error);
  }

  @Mutation
  private actualizarRequest() {
    listRequest(this.lista);
  }
  @Mutation
  private actualizarSuccess(item: T) {
    entitySuccessInList(this.lista, item);
    this.lastSavedId = item.id;
    notificationService.success(capitalize(this.entidad) + ' guardada correctamente');
  }
  @Mutation
  private actualizarError(error: WebContabError) {
    listFail(this.lista);
    logError('actualizar ' + this.entidad, error);
    notificationService.error(error);
  }

  @Mutation
  private eliminarRequest() {
    listRequest(this.lista);
  }
  @Mutation
  private eliminarSuccess(item: T) {
    listSuccess(
      this.lista,
      this.lista.items.filter(i => i.id !== item.id)
    );
    this.lastSavedId = null;
    notificationService.success('Se eliminó la organizacion');
  }
  @Mutation
  private eliminarError(error: WebContabError) {
    listFail(this.lista);
    logError('eliminar ' + this.entidad, error);
    notificationService.error(error);
  }
}
