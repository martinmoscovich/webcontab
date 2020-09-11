import { IdModel } from './IdModel';
import { Organizacion } from './Organizacion';
import { parseServerDate } from '@/utils/date';

/**
 * Ejercicio de una organizacion
 */
export interface Ejercicio extends IdModel {
  /** Fecha de inicio */
  inicio: Date;

  /** Fecha de fin */
  finalizacion: Date;

  /** Organizacion a la que pertenece */
  organizacion: Organizacion;

  /** Indica si esta cerrado */
  finalizado: boolean;
}

/**
 * Convierte el DTO del server al del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapEjercicicioFromServer(json: any): Ejercicio {
  return {
    ...json,
    inicio: parseServerDate(json.inicio as string),
    finalizacion: parseServerDate(json.finalizacion as string)
  };
}
