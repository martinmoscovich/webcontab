import { User } from '@/model/User';

/**
 * Membresia de un usuario a una organizacion.
 * Indica que usuarios pueden acceder a la organizacion y con que rol
 */
export interface Member {
  /** Usuario */
  user: User;

  /** Id de la organizacion */
  organizacionId: number;

  /** Rol */
  rol: Rol;

  /** Indica si esta membresia es modificable */
  readonly: boolean;
}

export type Rol = 'READ_ONLY' | 'USER' | 'ADMIN';
