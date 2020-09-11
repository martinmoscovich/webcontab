import { User } from './User';
import { Ejercicio, mapEjercicicioFromServer } from './Ejercicio';
import { Organizacion } from '@/model/Organizacion';

/**
 * Sesion de usuario
 */
export interface Session {
  /** Usuario logueado o null si es anonimo */
  user: User | null;

  /** Organizacion actual o null si no hay */
  organizacion: Organizacion | null;

  /** Ejercicio actual o null si no hay */
  ejercicio: Ejercicio | null;

  /**
   * Roles del usuario en la sesion actual.
   * Asociado a la organizacion actual.
   */
  roles: string[];
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapSessionFromServer(json: any): Session {
  return {
    user: json.user,
    organizacion: json.organizacion ?? null,
    ejercicio: json.ejercicio ? mapEjercicicioFromServer(json.ejercicio) : null,
    roles: json.roles ? json.roles : []
  };
}
