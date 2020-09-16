import { IdModel } from '@/model/IdModel';

/**
 * Moneda
 */
export interface Moneda extends IdModel {
  /** Simbolo ($, US$) */
  simbolo: string;

  /** Codigo (ARS, USD) */
  codigo: string;

  /** Nombre (Peso, Dolar) */
  nombre: string;

  /**
   * Indica si es la moneda por default.
   * Las nuevas cuentas tendran por default esta moneda.
   */
  default: boolean;

  /** Indica que la moneda se debe ajustar por inflacion */
  ajustable: boolean;
}
