/* eslint-disable @typescript-eslint/no-explicit-any */
import { IdModel, IdNameModel, CodigoModel } from './IdModel';
import { buildCompareFn } from '@/utils/array';
import { isNullOrUndefined } from '@/utils/general';

/**
 * Modelo base para categorias y cuentas
 */
interface CuentaBase extends IdModel, CodigoModel {
  /** Descripcion */
  descripcion: string;

  /**
   * Indica si esta activa o no
   * Las inactivas no se listan ni se pueden imputar
   */
  activa: boolean;

  /** Id de la categoria padre */
  categoriaId?: number;

  /** Numero dentro del padre */
  numero: number;

  /** Path completo desde la raiz (con id y nombre de cada nivel) */
  path?: IdNameModel[];
}

/**
 * Categoria
 */
export interface Categoria extends CuentaBase {
  /** No es imputable */
  imputable: false;

  /** Indica si es categoria de resultados */
  resultado: boolean;

  /** Hijos de la categoria */
  children?: CuentaOCategoria[];
}

/**
 * Cuenta
 */
export interface Cuenta extends CuentaBase {
  /** Es imputable */
  imputable: true;

  /** Se redefine porque una cuenta DEBE tener padre */
  categoriaId: number;

  /** Id de la moneda asociada */
  monedaId: number;

  /**
   * Indica si es una cuenta de un individuo
   * Se utiliza para filtrarlas, ya que pueden ser muchas
   */
  individual: boolean;

  /** Indica si la cuenta debe incluirse en el asiento de ajuste por inflacion */
  ajustable: boolean;

  /**
   * Indica si es una cuenta que se utiliza en el asiento de refundicion para balancear los resultados
   */
  balanceaResultados: boolean;

  /**
   * Indica si es la cuenta que se utiliza en el asiento de ajuste por inflacion para balancear las cuentas ajustables.
   */
  balanceaAjustables: boolean;
}

export type CuentaOCategoria = Cuenta | Categoria;

/**
 * Convierte el dto de cuenta del server al del cliente
 * @param json
 */
export function mapCuenta(json: any): Cuenta {
  return {
    ...json,
    moneda: { id: json.monedaId },
    individual: json.individual,
    ajustable: json.ajustable,
    imputable: true
  };
}

/**
 * Convierte el dto de categoria del server al del cliente
 * @param json
 */
export function mapCategoria(json: any): Categoria {
  return { ...json, /*numero: calculateNumero(json),*/ imputable: false };
}

/**
 * Convierte el dto de categoria o cuenta del server al del cliente
 * @param json
 */
export function mapCuentaOCategoria(json: any): CuentaOCategoria {
  return json.tipo === 'CUENTA' ? mapCuenta(json) : mapCategoria(json);
}

/**
 * Funcion que ordena cuentas o categorias por numero
 */
const sortPorNumero = buildCompareFn<CuentaOCategoria>({
  field: 'numero'
});

/**
 * Ordena por texto primero (el que coincida directamente), luego por si es cuenta primero y finalmente por numero
 * @param text
 */
export function sorterPorText(text: string) {
  return (a: CuentaOCategoria, b: CuentaOCategoria) => {
    if (a.imputable !== b.imputable) return a.imputable ? -1 : 1;

    const aIncludes = a.descripcion.toLowerCase().includes(text) || a.codigo.startsWith(text);
    const bIncludes = b.descripcion.toLowerCase().includes(text) || b.codigo.startsWith(text);

    if (aIncludes !== bIncludes) return aIncludes ? -1 : 1;

    return sortPorNumero(a, b);
  };
}

// /**
//  * Ordena cuenta y categorias para que las categorias esten primero, luego por numero
//  * @param a
//  * @param b
//  */
// export function sorterPorCategoriaPrimero(
//   a: CuentaOCategoria,
//   b: CuentaOCategoria
// ) {
//   if (a.imputable === b.imputable) return sortPorNumero(a, b);
//   return a.imputable ? 1 : -1;
// }

/**
 * Obtiene el numero mas grande de hijo de la categoria.
 * Si no tiene hijos, devuelve 0
 * @param cat
 */
export function getMaxNumeroHijo(cat: Categoria) {
  if (isNullOrUndefined(cat.children)) return null;
  if (cat.children.length === 0) return 0;
  return cat.children.map(c => c.numero).reduce((max, current) => (current ? Math.max(max ?? 0, current) : max), 0);
}
