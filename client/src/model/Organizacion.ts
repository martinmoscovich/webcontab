import { IdModel } from './IdModel';

/**
 * Organizacion
 */
export interface Organizacion extends IdModel {
  cuit: string;
  nombre: string;
}
