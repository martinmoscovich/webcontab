/** Resultado de validar un componente */
export interface FormValidation {
  /** Indica si es valido */
  valid?: boolean;

  /** Mensaje de error, en caso de no ser valido. */
  msg?: string;
}
