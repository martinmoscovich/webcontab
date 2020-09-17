import { toEntity } from '@/core/ajax/helpers';
import { RemoteError } from '@/core/ajax/error';
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

  /**
   * Permite hacer ping para estirar la vida de la sesion.
   */
  async ping(): Promise<void> {
    const response = await this.http.post(BASE_URL + '/ping');
    // Debe devolver 202 o lanzar excepcion
    // Si no lo hace, se lanza una desde aqui
    if (response.status !== 202) throw new RemoteError(response.status, 'server_error', 'Error en el servidor');
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
