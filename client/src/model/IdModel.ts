/* eslint-disable @typescript-eslint/no-explicit-any */
import { isDefined } from '@/utils/general';
import { mapProperty } from '@/utils/array';

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

/** Convierte una lista de items que tienen un atributo id en una lista de los ids (lista de numeros) */
export function toIdList(list: IdModel[]) {
  return mapProperty(list, 'id');
}

/** Convierte una lista de items que tienen un atributo "codigo"" en una lista de los codigos (lista de strings) */
export function toCodigoList(list: CodigoModel[]) {
  return mapProperty(list, 'codigo');
}
