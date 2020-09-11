import { IdModel } from '@/model/IdModel';

/**
 * Usuario de la aplicacion
 */
export interface User extends IdModel {
  /** Nombre de usuario */
  username: string;

  email: string;

  /** Nombre de la persona */
  name: string;

  /** URL del avatar */
  avatarUrl: string;
}

export interface UserWithType extends User {
  type: 'USER' | 'ADMIN';
}

/**
 * Usuario que contiene los datos basicos mas el tipo y el password.
 * Se usa para crear o modificar el usuario.
 */
export interface UserWithPassword extends UserWithType {
  password: string;
}
