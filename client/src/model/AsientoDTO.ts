import { ImputacionDTO, mapImputacionFromServer, UpstreamImputacionDTO } from './ImputacionDTO';
import { parseServerDate } from '@/utils/date';
import { IdModel } from '@/model/IdModel';

/**
 * Asiento
 */
export interface AsientoDTO extends IdModel {
  /** Numero de asiento */
  numero: number;

  /** Fecha del asiento */
  fecha: Date;

  /** Detalle del asiento */
  detalle: string;

  /** Imputaciones del asiento */
  imputaciones: ImputacionDTO[];
}

/**
 * DTO que se envia al server al crear y actualizar asientos.
 * Las diferencias con el DTO son:
 * - No se envia el numero (se calcula en el sever)
 * - Utiliza otro tipo de imputaciones
 * - El id es opcional, ya que al crear no hay id
 */
export interface UpstreamAsientoDTO extends Omit<AsientoDTO, 'id' | 'numero' | 'imputaciones'> {
  id?: number;
  imputaciones: UpstreamImputacionDTO[];
}

/**
 * Convierte un asiento del DTO del server al del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapAsientoFromServer(json: any): AsientoDTO {
  return {
    ...json,
    fecha: parseServerDate(json.fecha),
    imputaciones: json.imputaciones?.map(mapImputacionFromServer) ?? []
  };
}
