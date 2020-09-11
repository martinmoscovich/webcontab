import { AsientoDTO, mapAsientoFromServer } from './AsientoDTO';
import { IdModel } from './IdModel';
import { ImputacionModel } from '@/model/Imputacion';

/**
 * Imputacion de un asiento
 */
export interface ImputacionDTO extends IdModel {
  /** Asiento al que pertenece */
  asiento: AsientoDTO;

  /** Cuenta a la que pertence */
  cuenta: IdModel;

  /** Detalle de la imputacion */
  detalle: string;

  /**
   * Importe
   * Si es positivo es DEBE. Si es negativo es HABER
   */
  importe: number;
}

/**
 * DTO utilizado para enviar la imputacion al server.
 * La diferencia es que no se envia el asiento (no es necesario),
 * y el id es opcional, ya que una nueva imputacion no tiene id.
 */
export interface UpstreamImputacionDTO extends Omit<ImputacionDTO, 'id' | 'asiento'> {
  id?: number;
}

/**
 * Imputacion a la que se le agrega un saldo hasta ese momento
 */
export interface ImputacionSaldo<T extends ImputacionModel | ImputacionDTO = ImputacionDTO> {
  imputacion: T;
  saldo: number;
}

/**
 * Mapea una imputacion del server a la del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapImputacionFromServer(json: any): ImputacionDTO {
  return {
    ...json,
    asiento: json.asiento ? mapAsientoFromServer(json.asiento) : undefined
  };
}
