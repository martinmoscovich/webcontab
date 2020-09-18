import { categoriaApi, cuentaApi } from '@/api';
import { ApplicationError, WebContabError, isNotFound, isAnyRemoteError } from '@/core/ajax/error';
import {
  entitiesSuccessInList,
  entitySuccessInList,
  getInitialListState,
  listFail,
  listRequest,
  ListState
} from '@/core/ui/state/list';
import { Categoria, Cuenta, CuentaOCategoria } from '@/model/Cuenta';
import { notificationService } from '@/service';
import { byId, buildCompareFn } from '@/utils/array';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, VuexModule } from 'vuex-class-modules';
import Page, { emptyPage } from '@/core/Page';
import { toIdList } from '@/model/IdModel';

interface CuentaQuery {
  text: string;
  mode: 'cuenta' | 'categoria' | 'todos';
}

interface SearchResult<T extends number | CuentaOCategoria> {
  query: CuentaQuery;
  page: Page<T>;
}

/**
 * Store que maneja y cachea las categorias y cuentas
 */
@Module
export class CuentaStore extends VuexModule {
  /** Lista de categorias y cuentas cacheadas */
  cuentas: ListState<CuentaOCategoria> = getInitialListState();

  /** Ultima busqueda autocomplete y su resultado */
  lastSearch: SearchResult<number> = { query: { text: '', mode: 'cuenta' }, page: emptyPage() };

  /** Vuelve el store a su estado incial */
  @Mutation
  reset() {
    this.cuentas = getInitialListState();
    this.lastSearch = { query: { text: '', mode: 'cuenta' }, page: emptyPage() };
  }

  /** Busca una categoria o cuenta por id */
  get find(): (id: number) => CuentaOCategoria | undefined {
    return (id: number) => this.cuentas.items.find(byId(id));
  }

  /** Busca una cuenta por id */
  get findCuenta(): (id: number) => Cuenta | null {
    return (id: number) => (this.cuentas.items as Cuenta[]).find((c: Cuenta) => c.id === id && c.imputable) ?? null;
  }

  /** Busca una categoria por id junto con sus hijos */
  get findCategoria(): (id: number) => Categoria | null {
    return (id: number) => {
      const cate = (this.cuentas.items as Categoria[]).find((c: Categoria) => c.id === id && !c.imputable) ?? null;

      if (!cate) return null;

      return {
        ...cate,
        children: this.cuentas.items.filter(h => h.categoriaId === id).sort(buildCompareFn({ field: 'numero' }))
      };
    };
  }

  /** Obtiene las categorias raiz */
  get root() {
    return this.cuentas.items.filter(c => !c.categoriaId);
  }

  /**
   * Carga una cuenta del server por id
   * @param payload id a buscar y flag que indica si se puede usar la cacheada o se debe ir al server
   */
  @Action
  async findCuentaById(payload: { id: number; refresh: boolean; path: boolean }) {
    try {
      this.findByIdRequest();

      // Si se pidio refresh o el path, no se puede usar la cacheada
      let cuenta = payload.refresh || payload.path ? null : this.cuentas.items.find(byId(payload.id));

      if (cuenta && !cuenta.imputable) {
        throw new ApplicationError('Error interno, reinicie la aplicacion');
      }

      // Si no se encontro cuenta cacheada (o no se puede usar), cargarla del server
      if (!cuenta) {
        cuenta = await cuentaApi.getById(payload.id, { path: payload.path });
      }
      this.findByIdSuccess(cuenta);
    } catch (e) {
      this.findByIdError({ error: e, entidad: 'cuenta' });
    }
  }

  /**
   * Busca una categoria por id en el server remoto
   * @param payload id a buscar, flag que indica si interesa el path y los hijos
   */
  @Action
  async findCategoriaById(payload: { id: number; path: boolean; children: boolean }) {
    try {
      this.findByIdRequest();

      // Se carga la categoria desde el server
      const promiseCat = categoriaApi.getById(payload.id, {
        path: payload.path
      });

      // Si interesan los hijos, se cargan tambien
      const promiseChildren = payload.children ? categoriaApi.getChildren(payload.id) : [];

      const [categoria, children] = await Promise.all([promiseCat, promiseChildren]);

      this.findCategoriaByIdSuccess({ categoria, children });
    } catch (e) {
      this.findByIdError({ error: e, entidad: 'categoría' });
    }
  }

  /**
   * Obtiene las categorias raiz desde el server
   */
  @Action
  async getRootCategories() {
    try {
      this.getRootCategoriesRequest();

      const root = await categoriaApi.getRootCategories();

      this.getRootCategoriesSuccess(root);
    } catch (e) {
      this.getRootCategoriesError(e);
    }
  }

  /**
   * Busca en el servidor las cuentas con los ids indicados.
   * Si no se pide refresh, solo se pide del server las que no estan cacheadas
   * @param payload
   */
  @Action
  async findByIds(payload: { ids: number[]; refresh?: boolean }) {
    try {
      this.findByIdsRequest();
      let ids: number[] = [];

      if (payload.refresh) {
        // Si se pide refresh, buscar todos
        ids = payload.ids;
      } else {
        // Si no, buscar las que no estan locales
        for (const id of payload.ids) {
          const found = this.cuentas.items.find(byId(id));
          if (!found) ids.push(id);
        }
      }

      // Si hay ids para buscar, llamar al server
      const cuentas = ids.length > 0 ? await cuentaApi.findByIds(ids) : [];
      this.findByIdsSuccess(cuentas);
    } catch (e) {
      this.findByIdsError(e);
    }
  }

  /**
   * Crea una categoria o cuenta
   * @param item
   */
  @Action
  async crear(item: CuentaOCategoria) {
    try {
      this.crearRequest();
      if (item.imputable) {
        item = await cuentaApi.crear(item as Cuenta);
      } else {
        item = await categoriaApi.crear(item as Categoria);
      }
      this.crearSuccess(item);
    } catch (e) {
      this.crearError(e);
      throw e;
    }
  }

  /**
   * Actualiza una categoria o cuenta
   * @param item
   */
  @Action
  async actualizar(item: CuentaOCategoria) {
    try {
      this.actualizarRequest();
      if (item.imputable) {
        item = await cuentaApi.update(item as Cuenta);
      } else {
        item = await categoriaApi.update(item as Categoria);
      }
      this.actualizarSuccess(item);
    } catch (e) {
      this.actualizarError(e);
      throw e;
    }
  }

  /**
   * Elimina una categoria o cuenta
   * @param item
   */
  @Action
  async borrar(item: CuentaOCategoria) {
    try {
      this.borrarRequest();
      if (item.imputable) {
        await cuentaApi.borrar(item as Cuenta);
      } else {
        await categoriaApi.borrar(item as Categoria);
      }
      this.borrarSuccess(item);
    } catch (e) {
      this.borrarError(e);
      throw e;
    }
  }

  @Mutation
  private findByIdRequest() {
    listRequest(this.cuentas);
  }

  @Mutation
  private findByIdSuccess(cuenta: CuentaOCategoria) {
    entitySuccessInList(this.cuentas, cuenta);
  }

  @Mutation
  private findCategoriaByIdSuccess(payload: { categoria: Categoria; children: CuentaOCategoria[] }) {
    entitySuccessInList(this.cuentas, payload.categoria);

    // Se agregan las categorias o cuentas a la cache si el existente no tiene path
    entitiesSuccessInList(this.cuentas, payload.children, {
      overwrite: (existing, item) => (existing.path ? existing : item)
    });
  }

  @Mutation
  private findByIdError({ error, entidad }: { error: WebContabError; entidad: string }) {
    listFail(this.cuentas);
    logError('buscar cuenta', error);
    if (isNotFound(error)) {
      notificationService.error('No se encontró la ' + entidad);
    } else {
      notificationService.error(error);
    }
  }

  @Mutation
  private getRootCategoriesRequest() {
    listRequest(this.cuentas);
  }

  @Mutation
  private getRootCategoriesSuccess(categorias: Categoria[]) {
    entitiesSuccessInList(this.cuentas, categorias);
  }

  @Mutation
  private getRootCategoriesError(error: WebContabError) {
    listFail(this.cuentas);
    logError('buscar categorias root', error);
    notificationService.error(error);
  }

  @Mutation
  private findByIdsRequest() {
    listRequest(this.cuentas);
  }
  @Mutation
  private findByIdsSuccess(cuentas: CuentaOCategoria[]) {
    entitiesSuccessInList(this.cuentas, cuentas);
  }
  @Mutation
  private findByIdsError(error: WebContabError) {
    listFail(this.cuentas);
    logError('buscar cuenta', error);
    notificationService.error(error);
  }

  @Mutation
  private crearRequest() {
    listRequest(this.cuentas);
  }
  @Mutation
  private crearSuccess(item: CuentaOCategoria) {
    entitySuccessInList(this.cuentas, item, {
      overwrite: (existing, item) => ({ ...existing, ...item })
    });
  }
  @Mutation
  private crearError(error: WebContabError) {
    listFail(this.cuentas);
    logError('crear ' + this.cuentas, error);
    this.showError(error);
  }

  @Mutation
  private actualizarRequest() {
    listRequest(this.cuentas);
  }
  @Mutation
  private actualizarSuccess(item: CuentaOCategoria) {
    entitySuccessInList(this.cuentas, item, {
      overwrite: (existing, item) => ({ ...existing, ...item })
    });

    if (!item.imputable) {
      // Se actualizo una categoria.
      // Si esta cacheada en algun path, se debe actualizar su descripcion
      this.cuentas.items
        .filter(c => c.path)
        .forEach(cuenta => {
          const path = cuenta.path?.find(byId(item.id));
          if (path) path.name = item.descripcion;
        });
    }
  }
  @Mutation
  private actualizarError(error: WebContabError) {
    listFail(this.cuentas);
    logError('actualizar ' + this.cuentas, error);
    this.showError(error);
  }

  /** Notifica del error al usuario */
  private showError(error: WebContabError) {
    // Los errores esperados muestran el mensaje que envia el server
    // Los demas, un mensaje generico
    const message = isAnyRemoteError(error, [400, 403, 409]) ? error.description : 'Error al guardar';
    notificationService.error(message);
  }

  @Mutation
  private borrarRequest() {
    listRequest(this.cuentas);
  }
  @Mutation
  private borrarSuccess(item: CuentaOCategoria) {
    // Se remueve de la cache
    this.cuentas.items = this.cuentas.items.filter(cuenta => cuenta.id !== item.id);
    notificationService.success('La cuenta se eliminó exitosamente');
  }
  @Mutation
  private borrarError(error: WebContabError) {
    listFail(this.cuentas);
    logError('borrar cuenta o categoria', error);
    notificationService.error(error);
  }

  /********************************************************************************
   * Este search es un caso particular, solo se usa el store para cachear.
   * La busqueda se hace en el componente, asi cada uno maneja su loading y error
   ********************************************************************************/

  @Mutation
  searchSuccess(payload: SearchResult<CuentaOCategoria>) {
    // Se agregan las cuentas y categorias encontradas a la cache
    entitiesSuccessInList(this.cuentas, payload.page.items);

    // Se guarda la ultima busqueda con la query, el numero de pagina y los resultados como ids
    this.lastSearch = {
      query: payload.query,
      page: { ...payload.page, items: toIdList(payload.page.items) }
    };
  }
  @Mutation
  searchError(error: WebContabError) {
    logError('buscar cuenta por nombre', error);
    notificationService.error(error);
  }
}
