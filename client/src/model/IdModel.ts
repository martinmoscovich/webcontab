/* eslint-disable @typescript-eslint/no-explicit-any */
import { isDefined } from '@/utils/general';

/** Modelo con id */
export interface IdModel {
  id: number;
}

/** Modelo con id y name */
export interface IdNameModel extends IdModel {
  name: string;
}

/** Modelo con codigo */
export interface CodigoModel {
  codigo: string;
}

export function hasId(item: any): item is IdModel {
  return isDefined(item.id);
}
export function hasCodigo(item: any): item is CodigoModel {
  return isDefined(item.codigo);
}
