import { AxiosInstance } from 'axios';
import { Session, mapSessionFromServer } from '@/model/Session';
import { queryString, toEntity } from '@/core/ajax/helpers';

/**
 * API de login.
 * Usa un HttpClient distinto para evitar el prefijo "/api"
 */
export class LoginApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Autentica un usuario en base a sus credenciales
   * @returns los datos de la sesion autenticada
   */
  login(username: string, password: string): Promise<Session> {
    return this.http
      .post('/login', queryString({ username, password }).substring(1), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      .then(toEntity(mapSessionFromServer));
  }
}
