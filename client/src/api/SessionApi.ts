import { toEntity } from '@/core/ajax/helpers';
import { AxiosInstance } from 'axios';
import { Ejercicio } from '@/model/Ejercicio';
import { Session, mapSessionFromServer } from '@/model/Session';
import { Organizacion } from '@/model/Organizacion';

const BASE_URL = '/session';

/**
 * API de manejo de sesion
 */
export class SessionApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Obtiene la sesion actual.
   * Puede estar autenticada o no.
   */
  check(): Promise<Session> {
    return this.http.get(BASE_URL).then(toEntity(mapSessionFromServer));
  }

  /** Hace logout del usuario */
  logout(): Promise<void> {
    return this.http.post('/logout');
  }

  /** Asocia la sesion a una organizacion */
  seleccionarOrganizacion(organizacion: Organizacion): Promise<Session> {
    return this.http.put(`${BASE_URL}/organizacion/${organizacion.id}`).then(toEntity(mapSessionFromServer));
  }

  /** Remueve la organizacion de la sesion */
  salirDeOrganizacion(): Promise<Session> {
    return this.http.delete(`${BASE_URL}/organizacion`).then(toEntity(mapSessionFromServer));
  }

  /** Asocia la sesion a un ejercicio */
  seleccionarEjercicio(ejercicio: Ejercicio): Promise<Session> {
    return this.http.put(`${BASE_URL}/ejercicio/${ejercicio.id}`).then(toEntity(mapSessionFromServer));
  }

  /** Remueve el ejercicio de la sesion */
  salirDeEjercicio(): Promise<Session> {
    return this.http.delete(`${BASE_URL}/ejercicio`).then(toEntity(mapSessionFromServer));
  }
}
