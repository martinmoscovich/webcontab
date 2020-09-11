/**
 * Clase que representa un error en la aplicacion (ya sea local o remoto)
 *
 * @class ErrorDTO
 */
export class ErrorDTO {
  /**
   * Crea una instancia de ErrorDTO.
   * @param {string} code codigo de error (string parseable por el sistema)
   * @param {string} message descripcion del error (mensaje a mostrar al usuario)
   * @memberof ErrorDTO
   */
  constructor(public code: string, public message: string) {}
}
