/**
 * Interfaz que representa una entidad o elemento que se puede validar
 */
export interface Validable {
  /** Valida y devuelve `true` si es valido o `false` si no lo es */
  validate(): Promise<boolean>;
  reset(): void;
}

/**
 * Tipo que representa un elemento que se puede enfocar.
 */
export interface Focusable {
  /** Hace foco en el elemento */
  focus: () => void;
}

export type ValidableVue = Validable & Vue;
export type FocusableVue = Focusable & Vue;
