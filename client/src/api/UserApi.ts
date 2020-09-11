import { toEntity, queryString, toList } from '@/core/ajax/helpers';
import { AxiosInstance } from 'axios';
import { User, UserWithPassword } from '@/model/User';

const BASE_URL = '/admin/users';

/**
 * API de manejo de usuarios.
 * Incluye la creacion del primer usuario, actualizacion del perfil y
 * manejo administrativo de usuarios
 */
export class UserApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Crea el primer usuario (admin) de la aplicacion.
   * @param user datos del usuario
   */
  createFirst(user: UserWithPassword): Promise<User> {
    return this.http.post('/first', user).then(toEntity());
  }

  /**
   * Actualiza los datos del usuario actual
   * @param user datos modificados
   */
  updateCurrent(user: UserWithPassword): Promise<User> {
    return this.http.put('/user', user).then(toEntity());
  }

  /****************************************************
   *        ENDPOINTS ADMINISTRATIVOS
   ****************************************************/

  /**
   * Crea un nuevo usuario (solo un admin puede hacerlo)
   * @param user datos del usuario
   */
  create(user: UserWithPassword): Promise<User> {
    return this.http.post(BASE_URL, user).then(toEntity());
  }

  /**
   * Actualiza los datos de un usuario (solo un admin puede hacerlo)
   * @param user datos modificados
   */
  update(user: UserWithPassword): Promise<User> {
    return this.http.put(BASE_URL + '/' + user.id, user).then(toEntity());
  }

  /**
   * Busca un usuario en base a parte de su nombre o username (solo un admin puede hacerlo)
   * @param query
   */
  search(query: string) {
    return this.http.get(BASE_URL + queryString({ query })).then(toList());
  }

  /**
   * Busca todos los usuarios del sistema (solo un admin puede hacerlo)
   */
  list(): Promise<Array<User>> {
    return this.http.get(BASE_URL).then(toList());
  }
}
