import Page, { pageFromJSON } from '@/core/Page';
import { ImputacionDTO, mapImputacionFromServer } from '@/model/ImputacionDTO';

/**
 * DTO utilizado en el Mayor.
 * Contiene una pagina de imputaciones de una cuenta y el saldo previo a las mismas
 */
export interface ImputacionesCuenta extends Page<ImputacionDTO> {
  /** Saldo previo a las imputaciones */
  saldoAnterior: number;
}

/**
 * Mapea el DTO del server al del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapImputacionesCuentaFromServer(json: any): ImputacionesCuenta {
  const p: ImputacionesCuenta = pageFromJSON<ImputacionDTO>(json, mapImputacionFromServer) as ImputacionesCuenta;

  p.saldoAnterior = json.saldoAnterior;

  return p;
}
