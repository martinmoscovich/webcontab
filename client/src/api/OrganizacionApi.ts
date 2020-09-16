import { toList, toEntity, queryString } from '@/core/ajax/helpers';
import { Organizacion } from '@/model/Organizacion';
import { AxiosInstance } from 'axios';
import { Ejercicio, mapEjercicicioFromServer } from '@/model/Ejercicio';
import { SimpleApi } from '@/api/SimpleApi';
import { Periodo } from '@/model/Periodo';
import { Member } from '@/model/admin/Member';
import { formatDateForServer } from '@/utils/date';

const BASE_URL = '/organizaciones';
const EJERCICIOS_BASE_URL = '/ejercicios';

/** Payload necesario para crear un ejercicio */
export interface CrearEjercicioPayload {
  /** Organizacion a la que pertenece */
  organizacion: Organizacion;
  /** Periodo del ejercicio */
  periodo: Periodo;
  /** Indica si se debe crear el ejercicio anterior */
  cerrarUltimo: boolean;
}

/** Payload necesario para eliminar un ejercicio */
export interface EliminarEjercicioPayload {
  /** Ejercicio a eliminar */
  ejercicio: Ejercicio;
  /**
   * Nombre de la organizacion a la que pertenece.
   * Se usa para confirmar que el usuario realiza la accion intencionalmente.
   */
  organizacion: string;
}

/**
 * API de manejo de organizaciones y ejercicios.
 * Extiende de SimpleApi y agrega llamadas para ejercicios.
 */
export class OrganizacionApi extends SimpleApi<Organizacion> {
  constructor(http: AxiosInstance) {
    super(http, { baseUrl: BASE_URL });
  }

  /** Busca los ejercicios de una organizacion */
  findEjercicios(org: Organizacion): Promise<Ejercicio[]> {
    return this.http.get(`${BASE_URL}/${org.id}/ejercicios`).then(toList(mapEjercicicioFromServer));
  }

  /**
   * Crea un nuevo ejercicio
   * @param payload payload con los datos de un ejercicio
   */
  crearEjercicio(payload: CrearEjercicioPayload): Promise<Ejercicio> {
    return this.http
      .post(`${BASE_URL}/${payload.organizacion.id}/ejercicios`, {
        ...payload.periodo,
        cerrarUltimo: payload.cerrarUltimo
      })
      .then(toEntity(mapEjercicicioFromServer));
  }

  /**
   * Elimina un ejercicio
   * @param payload payload que incluye el nombre de la organizacion para confirmar
   */
  async eliminarEjercicio(payload: EliminarEjercicioPayload): Promise<void> {
    await this.http
      .delete(
        `${EJERCICIOS_BASE_URL}/${payload.ejercicio.id}${queryString({
          organizacion: payload.organizacion
        })}`
      )
      .then(toEntity(mapEjercicicioFromServer));
  }

  /**
   * Cierra un ejercicio
   */
  finalizarEjercicio(ejercicio: Ejercicio): Promise<Ejercicio> {
    return this.http.put(`${EJERCICIOS_BASE_URL}/${ejercicio.id}/cierre`).then(toEntity(mapEjercicicioFromServer));
  }

  /**
   * Reabre un ejercicio cerrado
   */
  reabrirEjercicio(ejercicio: Ejercicio): Promise<Ejercicio> {
    return this.http.delete(`${EJERCICIOS_BASE_URL}/${ejercicio.id}/cierre`).then(toEntity(mapEjercicicioFromServer));
  }

  /**
   * Renumera los asientos de un ejercicio y los confirma (commit) hasta la fecha indicada
   * @param ejercicio ejercicio a renumerar
   * @param fecha fecha de confirmacion
   */
  confirmarAsientosDelEjercicio(ejercicio: Ejercicio, fecha: Date): Promise<Ejercicio> {
    return this.http
      .put(`${EJERCICIOS_BASE_URL}/${ejercicio.id}/confirmacion` + queryString({ fecha: formatDateForServer(fecha) }))
      .then(toEntity(mapEjercicicioFromServer));
  }

  /**
   * Genera o regenera el asiento de ajuste por inflacion del ejercicio
   * @param ejercicio ejercicio a ajustar
   */
  ajustarEjercicioPorInflacion(ejercicio: Ejercicio): Promise<Ejercicio> {
    return this.http.put(`${EJERCICIOS_BASE_URL}/${ejercicio.id}/inflacion`).then(toEntity(mapEjercicicioFromServer));
  }

  findMiembros(org: Organizacion): Promise<Member[]> {
    return this.http.get(`${BASE_URL}/${org.id}/miembros`).then(toList());
  }

  /**
   * Agrega un miembro a una organizacion, con el rol deseado
   */
  addMember(member: Member): Promise<Member> {
    return this.http
      .post(`${BASE_URL}/${member.organizacionId}/miembros/${member.user.id}`, {
        rol: member.rol
      })
      .then(toEntity());
  }

  /** Elimina un miembro de una organizacion */
  async removeMember(member: Member): Promise<void> {
    await this.http.delete(`${BASE_URL}/${member.organizacionId}/miembros/${member.user.id}`);
  }
}
