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
import { notificationService } from '@/service';
import { BaseSimpleStore } from '@/store/BaseSimpleStore';
import { logError } from '@/utils/log';
import { Action, Module, Mutation, RegisterOptions } from 'vuex-class-modules';
import { byId } from '@/utils/array';

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
      this.crearEjercicioSuccess({
        ejercicio,
        cerrarUltimo: payload.cerrarUltimo
      });
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
  private crearEjercicioSuccess(payload: { ejercicio: Ejercicio; cerrarUltimo: boolean }) {
    entitySuccessInList(this.ejercicios, payload.ejercicio);
    notificationService.show({
      message: 'Se creó el nuevo ejercicio' + (payload.cerrarUltimo ? ' y se cerró el anterior' : ''),
      type: 'success',
      duration: payload.cerrarUltimo ? 5000 : undefined
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
  agregarAStore(ejercicio: Ejercicio) {
    entitySuccessInList(this.ejercicios, ejercicio);
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
}
