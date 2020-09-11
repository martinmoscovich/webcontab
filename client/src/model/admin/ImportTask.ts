import { Ejercicio, mapEjercicicioFromServer } from '@/model/Ejercicio';
import { Organizacion } from '@/model/Organizacion';

/**
 * Tarea de importacion
 */
export interface ImportTask {
  /** id de la tarea */
  uuid: string;

  /** Organizacion a importar */
  organizacion: Organizacion;

  /** Ejercicio a importar */
  ejercicio: Ejercicio;

  /** Indica si se puede importar el ejercicio o solo la organizacion y sus cuentas */
  puedeImportarEjercicio: boolean;

  /** Estado actual de la importacion */
  status: ImportStatus;

  /** Mensaje de error en caso de fallar */
  error: string;

  /** Estadisticas de la importacion hasta el momento */
  summary: ImportTaskSummary;
}

export type ImportStatus = 'PENDING' | 'IMPORTING' | 'FINISHED' | 'ERROR';

/**
 * Estadisticas de la importacion.
 * Indica cuantos elementos se encontraron y cuantos se importaron.
 */
export interface ImportTaskSummary {
  organizacionesImportadas: number;
  ejerciciosImportados: number;
  cuentasTotales: number;
  cuentasImportadas: number;
  categoriasTotales: number;
  categoriasImportadas: number;
  asientosImportados: number;
  imputacionesImportadas: number;
}

/**
 * Convierte el dto del server al del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapImportTaskFormServer(json: any): ImportTask {
  return {
    ...json,
    ejercicio: json.ejercicio ? mapEjercicicioFromServer(json.ejercicio) : null
  };
}
