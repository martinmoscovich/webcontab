import { organizacionApi } from '@/api';
import { CrearEjercicioPayload, EliminarEjercicioPayload } from '@/api/OrganizacionApi';
import { WebContabError } from '@/core/ajax/error';
import {
  entitySuccessInList,
  getInitialListState,
  listFail,
  listRequest,
  ListState,
  listSuccess,
  entitiesSuccessInList
} from '@/core/ui/state/list';
import { Ejercicio } from '@/model/Ejercicio';
import { Organizacion } from '@/model/Organizacion';
import { notificationService, routerService } from '@/service';
import { BaseSimpleStore } from '@/store/BaseSimpleStore';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, RegisterOptions } from 'vuex-class-modules';
import { byId } from '@/utils/array';
import { asientoStore } from '@/store';
import { formatDate } from '@/utils/date';

/**
 * Store que maneja y cachea organizaciones y ejercicios.
 * Extiende BaseSimpleStore con funcionalidad para manejar ejercicios
 */
@Module
export class OrganizacionStore extends BaseSimpleStore<Organizacion> {
  /** Lista de ejercicios cacheados */
  ejercicios: ListState<Ejercicio> = getInitialListState();

  constructor(options: RegisterOptions) {
    super(
      {
        api: organizacionApi,
        entidad: 'organizacion',
        entidades: 'Organizaciones'
      },
      options
    );
  }

  /** Vuelve el store a su estado inicial */
  @Mutation
  reset() {
    super.reset();
    this.ejercicios = getInitialListState();
  }

  /** Busca los ejercicios de una organizacion en la cache */
  get findEjerciciosLocal() {
    return (organizacion: Organizacion) => this.ejercicios.items.filter(ej => ej.organizacion.id === organizacion.id);
  }

  /** Busca un ejercicio por id en la cache */
  get findEjercicioByIdLocal() {
    return (id: number) => this.ejercicios.items.find(byId(id));
  }

  /**
   * Carga los ejercicios de una organizacion del servidor
   * @param organizacion
   */
  @Action
  async findEjercicios(organizacion: Organizacion) {
    try {
      this.findEjerciciosRequest();
      const ejercicios = await organizacionApi.findEjercicios(organizacion);
      this.findEjerciciosSuccess(ejercicios);
    } catch (e) {
      this.findEjerciciosError(e);
      throw e;
    }
  }

  /**
   * Crea un ejercicio
   * @param payload
   */
  @Action
  async crearEjercicio(payload: CrearEjercicioPayload) {
    try {
      this.crearEjercicioRequest();
      const ejercicio = await organizacionApi.crearEjercicio(payload);

      // Como al crear un ejercicio se puede haber cerrado el anterior, se vuelve a cargar la lista
      // de ejercicios
      this.findEjercicios(ejercicio.organizacion);
      this.crearEjercicioSuccess(payload.cerrarUltimo);
    } catch (e) {
      this.crearEjercicioError(e);
      throw e;
    }
  }

  /**
   * Elimina un ejercicio
   * @param payload
   */
  @Action
  async eliminarEjercicio(payload: EliminarEjercicioPayload) {
    try {
      this.eliminarEjercicioRequest();
      await organizacionApi.eliminarEjercicio(payload);
      this.eliminarEjercicioSuccess(payload.ejercicio);
    } catch (e) {
      this.eliminarEjercicioError(e);
      throw e;
    }
  }

  /**
   * Cierra un ejercicio
   * @param ejercicio
   */
  @Action
  async finalizarEjercicio(ejercicio: Ejercicio) {
    try {
      this.finalizarEjercicioRequest();
      const result = await organizacionApi.finalizarEjercicio(ejercicio);
      this.finalizarEjercicioSuccess(result);
    } catch (e) {
      this.finalizarEjercicioError(e);
      throw e;
    }
  }

  /**
   * Reabre un ejercicio
   * @param ejercicio
   */
  @Action
  async reabrirEjercicio(ejercicio: Ejercicio) {
    try {
      this.reabrirEjercicioRequest();
      const result = await organizacionApi.reabrirEjercicio(ejercicio);
      this.reabrirEjercicioSuccess(result);
    } catch (e) {
      this.reabrirEjercicioError(e);
      throw e;
    }
  }

  /**
   * Renumera los asientos de un ejercicio y los confirma hasta la fecha indicada.
   * @param ejercicio
   */
  @Action
  async confirmarAsientosDelEjercicio(payload: { ejercicio: Ejercicio; fecha: Date }) {
    try {
      this.confirmarAsientosDelEjercicioRequest();

      // Se confirman y renumeran los asientos
      const result = await organizacionApi.confirmarAsientosDelEjercicio(payload.ejercicio, payload.fecha);

      // Se resetea la cache de asientos, ya que cambiaron de numero
      asientoStore.reset();

      this.confirmarAsientosDelEjercicioSuccess(result);
    } catch (e) {
      this.confirmarAsientosDelEjercicioError(e);
      throw e;
    }
  }

  @Action
  async ajustarPorInflacion(ejercicio: Ejercicio) {
    try {
      this.ajustarPorInflacionRequest();

      // Se guarda el id del asiento de ajuste (si existe)
      const asientoId = ejercicio.asientoAjusteId;

      const result = await organizacionApi.ajustarEjercicioPorInflacion(ejercicio);

      // Si habia asiento de ajuste, se lo quita de la cache
      if (asientoId) asientoStore.clearAsientoFromCache(asientoId);

      this.ajustarPorInflacionSuccess(result);
    } catch (e) {
      this.ajustarPorInflacionError(e);
      throw e;
    }
  }

  /**
   * Agrega un ejercicio al store.
   * Se llama desde el Store de session para registrar el ejercicio
   * actual dentro del store.
   */
  @Mutation
  agregarAStore(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);
  }

  @Mutation
  private findEjerciciosRequest() {
    listRequest(this.ejercicios);
  }
  @Mutation
  private findEjerciciosSuccess(items: Ejercicio[]) {
    entitiesSuccessInList(this.ejercicios, items);
  }
  @Mutation
  private findEjerciciosError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('buscar ejercicios', error);
    notificationService.error(error);
  }

  @Mutation
  private crearEjercicioRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private crearEjercicioSuccess(cerrarUltimo: boolean) {
    notificationService.show({
      message: 'Se creó el nuevo ejercicio' + (cerrarUltimo ? ' y se cerró el anterior' : ''),
      type: 'success',
      duration: cerrarUltimo ? 5000 : undefined
    });
  }

  @Mutation
  private crearEjercicioError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('crear ejercicio', error);
    notificationService.error(error);
  }

  @Mutation
  private eliminarEjercicioRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private eliminarEjercicioSuccess(ejercicio: Ejercicio) {
    listSuccess(
      this.ejercicios,
      this.ejercicios.items.filter(item => item.id !== ejercicio.id)
    );
    notificationService.success('Se eliminó el ejercicio');
  }

  @Mutation
  private eliminarEjercicioError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('eliminar ejercicio', error);
    notificationService.error(error);
  }

  @Mutation
  private finalizarEjercicioRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private finalizarEjercicioSuccess(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);
    notificationService.success('Se cerró el ejercicio');
  }

  @Mutation
  private finalizarEjercicioError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('cerrar ejercicio', error);
    notificationService.error(error);
  }

  @Mutation
  private reabrirEjercicioRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private reabrirEjercicioSuccess(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);
    notificationService.success('Se reabrió el ejercicio');
  }

  @Mutation
  private reabrirEjercicioError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('reabrir ejercicio', error);
    notificationService.error(error);
  }

  @Mutation
  private confirmarAsientosDelEjercicioRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private confirmarAsientosDelEjercicioSuccess(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);
    notificationService.success(
      'Se renumeraron los asientos del ejercicio y se confirmaron hasta la fecha ' +
        formatDate(ejercicio.fechaConfirmada)
    );
  }

  @Mutation
  private confirmarAsientosDelEjercicioError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('confirmar asientos de ejercicio', error);
    notificationService.error(error);
  }

  @Mutation
  private ajustarPorInflacionRequest() {
    listRequest(this.ejercicios);
  }

  @Mutation
  private ajustarPorInflacionSuccess(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);

    if (!ejercicio.asientoAjusteId) return;

    notificationService.show({
      type: 'success',
      message: 'Se generó el asiento de ajuste por inflación del ejercicio',
      actionText: 'Ver',
      to: routerService.asiento({ id: ejercicio.asientoAjusteId })
    });
  }

  @Mutation
  private ajustarPorInflacionError(error: WebContabError) {
    listFail(this.ejercicios);
    logError('ajustar por inflacion el ejercicio', error);
    notificationService.error(error);
  }
}
