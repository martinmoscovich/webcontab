import { IdModel } from '@/model/IdModel';
import { parseServerDate } from '@/utils/date';
import { toInt, toFloat } from '@/utils/general';
import { OptionalId } from '@/core/TypeHelpers';

/**
 * Representa en indice de inflacion mensual
 */
export interface InflacionMes extends IdModel {
  /** Fecha que representa el mes */
  mes: Date;

  /** Id de la moneda */
  monedaId: number;

  /**
   * Indica de inflacion del mes.
   * No esta definido cuando aun no se persistio
   */
  indice?: number;
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

/** Convierte el item local en el JSON que espera el server */
export function mapInflacionToServer(item: OptionalId<InflacionMes>): unknown {
  return {
    id: item.id,
    moneda: { id: item.monedaId },
    indice: item.indice,
    mes: item.mes.getFullYear() + '-' + (item.mes.getMonth() + 1).toString().padStart(2, '0')
  };
}
