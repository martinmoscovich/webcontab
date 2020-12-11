import { userApi } from '@/api';
import { WebContabError } from '@/core/ajax/error';
import { entitySuccessInList, getInitialListState, listFail, listRequest, ListState } from '@/core/ui/state/list';
import { User } from '@/model/User';
import { notificationService } from '@/service';
import { byId } from '@/utils/array';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, VuexModule } from 'vuex-class-modules';

/**
 * Store que busca en el server y cachea los datos de los usuarios para mostrar info de auditoria
 */
@Module
export class UserStore extends VuexModule {
  /** Lista de usuarios cacheados */
  lista: ListState<User> = getInitialListState();

  pendingIds: number[] = [];

  /** Busca un usuario por id en la cache */
  get find() {
    return (id: number) => this.lista.items.find(byId(id));
  }

  /** Busca un usuario por id en la cache o en el server */
  @Action
  async findById(payload: { id: number; refresh: boolean }) {
    try {
      // Si ya esta buscando este id, no se llama al server otra vez
      if (this.pendingIds.includes(payload.id)) return;

      this.findByIdRequest(payload.id);

      // Si no se pidio refresh, se busca en local
      let item = !payload.refresh ? this.lista.items.find(byId(payload.id)) : null;

      // Si no existe en local o se pidio refresh, se llama al server
      if (!item) item = await userApi.getById(payload.id);
      this.agregarUsuarioEnCache(item);
    } catch (error) {
      this.findByIdError({ id: payload.id, error });
    }
  }

  /**
   * Agrega el usuario a la cache
   */
  @Mutation
  agregarUsuarioEnCache(item: User) {
    // Se quita el id de la lista de pendientes
    this.pendingIds = this.pendingIds.filter(pendingId => pendingId !== item.id);

    entitySuccessInList(this.lista, item);
  }

  @Mutation
  private findByIdRequest(id: number) {
    listRequest(this.lista);

    // Se agrega el id a la lista de pendientes
    this.pendingIds.push(id);
  }

  @Mutation
  private findByIdError({ error, id }: { error: WebContabError; id: number }) {
    // Se quita el id de la lista de pendientes
    this.pendingIds = this.pendingIds.filter(pendingId => pendingId !== id);

    listFail(this.lista);
    logError('buscar usuario', error);
    notificationService.error(error);
  }
}
