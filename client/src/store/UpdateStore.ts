import { adminApi } from '@/api';
import { WebContabError, isNotFound } from '@/core/ajax/error';
import { LoadingStatus } from '@/core/ui/loading';
import { UpdateStatus, UpdateState } from '@/model/admin/AdminModels';
import { notificationService } from '@/service';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, VuexModule } from 'vuex-class-modules';
import { isNullOrUndefined, isDefined } from '@/utils/general';
import { delay } from '@/utils/delay';

/**
 * Store que maneja el estado del actualizador
 */
@Module
export class UpdateStore extends VuexModule {
  /** Estado del actualizador */
  update: UpdateStatus = {
    action: 'IDLE',
    currentRelease: {
      serverVersion: '',
      releaseDate: new Date(),
      releaseVersion: ''
    },
    targetRelease: undefined
  };
  status: LoadingStatus = { error: false, loading: false };
  availableToDownload: string | null = null;

  /**
   * Indica que el updater esta en estado IDLE.
   * O sea, no esta haciendo nada ni tampoco hay actualizaciones pendientes
   */
  get idle() {
    return this.update.action === 'IDLE' && isNullOrUndefined(this.update.targetRelease) && !this.downloadAvailable;
  }

  /**
   * Indica que el updater esta en estado Pendiente.
   * O sea, no esta haciendo nada y hay una actualizacion pendiente a instalar
   */
  get pending() {
    return this.update.action === 'IDLE' && isDefined(this.update.targetRelease) && !this.downloadAvailable;
  }

  /**
   * Indica que hay una actualizacion disponible para descargar del servidor de actualizaciones
   */
  get downloadAvailable() {
    return this.update.action === 'IDLE' && this.availableToDownload;
  }

  /** Indica que se esta descargando una actualizacion */
  get downloading() {
    return this.update.action === 'DOWNLOADING';
  }

  /** Indica que se esta actualizando la aplicacion */
  get updating() {
    return this.update.action === 'UPDATING';
  }

  /** Indica que se esta reiniciando la aplicacion */
  get restarting() {
    return this.update.action === 'RESTARTING';
  }

  /**
   * Indica que hay una actualizacion en curso.
   * Puede estar instalando o reiniciando.
   */
  get runningUpdate() {
    return this.updating || this.restarting;
  }

  @Mutation
  setUpdate(state: UpdateState) {
    this.update.action = state;
  }

  /**
   * Comprueba si existe una actualizacion disponible en el servidor de actualizaciones
   */
  @Action
  async check() {
    try {
      this.checkRequest();

      // Se buscan nuevas versiones
      const version = await adminApi.updateCheck();

      // Se indica la version nueva disponible
      this.checkSuccess(version);
    } catch (e) {
      this.checkError(e);
      throw e;
    }
  }

  @Mutation
  private checkRequest() {
    this.status = { loading: true };
  }
  @Mutation
  private checkSuccess(version: string | null) {
    this.availableToDownload = version;
    this.status = {};
  }
  @Mutation
  private checkError(error: WebContabError) {
    this.status = { error: true };
    logError('chequear version', error);
    notificationService.error(error);
  }

  /** Descarga una actualizacion del servidor de actualizaciones */
  @Action
  async download() {
    try {
      this.downloadRequest();

      // Le pide al server que descargue la actualizacion
      await adminApi.updateDownload();

      // Se llama periodicamente al server para actualizar el estado de descarga
      // Esta promise finaliza cuando lo hace la descarga
      await this.downloadBackgroundRefresh(true);
      this.downloadSuccess();
    } catch (e) {
      this.downloadError(e);
      throw e;
    }
  }

  /** Llama a refresh periodicamente hasta que finaliza la descarga */
  private async downloadBackgroundRefresh(force: boolean) {
    await delay(2000);
    await this.refresh(true);
    if (force || this.status.loading || this.update.action !== 'IDLE') {
      // Si aun no esta listo, se vuelve a llamar a este metodo
      this.downloadBackgroundRefresh(false);
    }
  }

  @Mutation
  private downloadRequest() {
    this.status = { loading: true };
  }
  @Mutation
  private downloadSuccess() {
    this.update.action = 'DOWNLOADING';
    this.status = {};
    this.availableToDownload = null;
  }
  @Mutation
  private downloadError(error: WebContabError) {
    this.status = { error: true };
    logError('pidiendo la descarga de actualizacion', error);
    notificationService.error(error);
  }

  /**
   * Realiza la actualizacion
   */
  @Action
  async applyUpdate() {
    try {
      this.updateRequest();

      // Le pide al server que realice la actualizacion
      await adminApi.updateApply();
      this.updateSuccess();

      // Se llama periodicamente al server para actualizar el estado de actualizacion
      this.updateBackgroundRefresh(true);
    } catch (e) {
      this.updateError(e);
      throw e;
    }
  }

  private async updateBackgroundRefresh(force: boolean) {
    try {
      // Se duerme por 5 segundos
      await delay(5000);

      // Se obtiene el estado actual del updater
      await this.refresh(true);

      if (!this.runningUpdate) {
        // Si ya no esta corriendo el update, termino
        this.setUpdate('IDLE');

        // Muestra mensaje
        notificationService.success('Actualizacion realizada, refrescando el navegador');

        // Deja 2 segundos para que se vea el mensaje y reinicia la aplicacion
        await delay(2000);
        window.location.reload();
      }
    } catch (e) {
      if (this.runningUpdate && !isNotFound(e)) {
        // Si el server tira error (que no es 404) y se esta actualizando, es que se esta reiniciando y no responde.
        // Setear el status si es necesario y seguir esperando
        if (!this.restarting) this.setUpdate('RESTARTING');
      } else {
        // Si no se esta actualizando o tira 404, hay un problema, reiniciar
        notificationService.error('Error al actualizar, se reinicia la app');
        await delay(1500);
        window.location.reload();
        throw e;
      }
    }

    // Si no termino, se llama de nuevo al metodo para seguir esperando
    if (force || this.status.loading || this.update.action !== 'IDLE') {
      this.updateBackgroundRefresh(false);
    }
  }

  @Mutation
  private updateRequest() {
    this.status = { loading: true };
  }
  @Mutation
  private updateSuccess() {
    this.update.action = 'UPDATING';
    this.status = {};
  }
  @Mutation
  private updateError(error: WebContabError) {
    this.status = { error: true };

    logError('pidiendo la actualizacion', error);
    notificationService.error(error);
  }

  /** Se actualiza el estado del updater */
  @Action
  async refresh(background: boolean) {
    try {
      this.refreshRequest();
      const update = await adminApi.updateStatus();
      this.refreshSuccess(update);
    } catch (e) {
      this.refreshError({ error: e, background });
      throw e;
    }
  }

  @Mutation
  private refreshRequest() {
    this.status = { loading: true };
  }
  @Mutation
  private refreshSuccess(update: UpdateStatus) {
    this.update = update;
    this.status = {};
  }
  @Mutation
  private refreshError(payload: { error: WebContabError; background: boolean }) {
    this.status = { error: true };
    logError('refrescar estado de actualizacion', payload.error);
    if (!payload.background) notificationService.error(payload.error);
  }
}
