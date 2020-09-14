import { entitySuccessInList, listFail, listRequest, ListState, getInitialListState } from '@/core/ui/state/list';
import { AsientoDTO, UpstreamAsientoDTO } from '@/model/AsientoDTO';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, VuexModule } from 'vuex-class-modules';
import { byId } from '@/utils/array';
import { asientoApi } from '@/api';
import { notificationService, routerService } from '@/service';
import { WebContabError, isNotFound } from '@/core/ajax/error';
import { IdModel } from '@/model/IdModel';

@Module
export class AsientoStore extends VuexModule {
  asientos: ListState<AsientoDTO> = getInitialListState();
  lastSavedId: number | null = null;
  tempForm: Partial<AsientoDTO> | null = null;

  @Mutation
  reset() {
    this.asientos = getInitialListState();
    this.lastSavedId = null;
    this.tempForm = null;
  }

  get find() {
    return (id: number) => this.asientos.items.find(byId(id)) || null;
  }

  get lastSavedAsiento() {
    if (!this.lastSavedId) return null;
    return this.find(this.lastSavedId);
  }

  @Action
  async findById(payload: { id: number; imputaciones: boolean; refresh: boolean }) {
    try {
      this.findByIdRequest();
      let asiento = payload.refresh ? this.asientos.items.find(byId(payload.id)) : null;
      if (!asiento)
        asiento = await asientoApi.getById(payload.id, {
          imputaciones: payload.imputaciones
        });
      this.findByIdSuccess(asiento);
    } catch (e) {
      this.findByIdError(e);
      throw e;
    }
  }

  @Action
  async save(asiento: UpstreamAsientoDTO) {
    try {
      this.saveRequest();

      const isUpdate = asiento.id;

      const result = isUpdate ? await asientoApi.actualizar(asiento) : await asientoApi.crear(asiento);

      this.saveSuccess({ asiento: result, isNew: !isUpdate });
    } catch (e) {
      this.saveError(e);
      throw e;
    }
  }

  @Action
  async delete(asiento: AsientoDTO) {
    try {
      this.deleteRequest();

      await asientoApi.borrar(asiento);

      this.deleteSuccess(asiento);
    } catch (e) {
      this.deleteError(e);
      throw e;
    }
  }

  @Mutation
  saveForm(form: Partial<AsientoDTO>) {
    this.tempForm = form;
  }
  @Mutation
  clearForm() {
    this.tempForm = null;
  }

  @Mutation
  private saveRequest() {
    listRequest(this.asientos);
  }
  @Mutation
  private saveSuccess({ asiento, isNew }: { asiento: AsientoDTO; isNew: boolean }) {
    entitySuccessInList(this.asientos, asiento);
    this.lastSavedId = asiento.id;

    // Si es nuevo, se muestra action para ir a la pantalla del asiento
    // Si no, ya estamos en esa pantalla, no es necesaria la accion
    const toastAction = isNew ? { actionText: 'Ver', to: routerService.asiento(asiento) } : {};

    const message = `El asiento N° <b style="color: #1e3cce">${asiento.numero}</b> se guardó correctamente`;

    // Se alarga la duracion para que tenga tiempo de usar la action
    notificationService.show({ message, type: 'success', duration: 5000, ...toastAction });
  }
  @Mutation
  private saveError(error: WebContabError) {
    listFail(this.asientos);
    logError('guardar asiento %o', error);

    notificationService.error(error);
  }

  @Mutation
  private deleteRequest() {
    listRequest(this.asientos);
  }
  @Mutation
  private deleteSuccess(asiento: AsientoDTO) {
    this.asientos.items = this.asientos.items.filter(item => item.id !== asiento.id);
    notificationService.success(
      `El asiento N° <b style="color: #1e3cce">${asiento.numero}</b> se eliminó correctamente`
    );
  }
  @Mutation
  private deleteError(error: WebContabError) {
    listFail(this.asientos);
    logError('borrar asiento %o', error);

    notificationService.error(error);
  }

  @Mutation
  private findByIdRequest() {
    listRequest(this.asientos);
  }
  @Mutation
  private findByIdSuccess(asiento: AsientoDTO) {
    entitySuccessInList(this.asientos, asiento);
  }
  @Mutation
  private findByIdError(error: WebContabError) {
    listFail(this.asientos);
    logError('buscar asiento', error);
    if (isNotFound(error)) {
      notificationService.error('No se encontró el asiento');
    } else {
      notificationService.error(error);
    }
  }

  //   paginas: PaginatedListState<Asiento> = getInitialPaginatedListState();
  //   query: AsientosSearchOptions = {};
  //   @Action
  //   async search(query: AsientosSearchOptions) {
  //     try {
  //       this.searchRequest();
  //       let results: Cuenta[];
  //       if (this.query === query) {
  //         results = this.query.item.results;
  //       } else {
  //         results = await cuentaApi.search(text);
  //       }
  //       this.searchSuccess({ query: text, results });
  //     } catch (error) {
  //       this.searchFail(error);
  //     }
  //   }
  // @Mutation
  // searchRequest() {
  //   paginatedListRequest(this.paginas);
  // }
  // @Mutation
  // searchSuccess(payload: {
  //   query: AsientosSearchOptions;
  //   page: Page<Asiento>;
  // }) {
  //   paginatedListSuccess(this.paginas, payload.page);
  //   this.query = payload.query;
  // }
  // @Mutation
  // searchFail(_error: ErrorDTO) {
  //   paginatedListFail(this.paginas);
  // }
}
