import { IdModel } from '@/model/IdModel';
import { parseServerDate } from '@/utils/date';
import { toInt, toFloat } from '@/utils/general';

/**
 * Representa en indice de inflacion mensual
 */
export interface InflacionMes extends IdModel {
  /** Fecha que representa el mes */
  mes: Date;

  /** Id de la moneda */
  monedaId: number;

  /** Indica de inflacion del mes */
  indice: number;
}

/**
 * Convierte el Payload enviado por el server en un item de tipo InflacionMes
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapInflacionFromServer(json: any): InflacionMes {
  return {
    ...json,
    mes: parseServerDate(json.mes + '-01'),
    monedaId: toInt(json.moneda?.id),
    indice: toFloat(json.indice)
  };
}
