/**
 * Item utilizado en el balance
 */
export interface BalanceItem {
  /** Id de la cuenta */
  id: number;

  /** Codigo de la cuenta */
  codigo: string;

  /** Descripcion de la cuenta */
  descripcion: string;

  /** id de la moneda */
  monedaId: number;

  /** Saldo de la cuenta */
  saldo: number;
}

/** Totales del balance por moneda */
export type BalanceTotal = Record<string, number>;
