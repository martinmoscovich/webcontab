import { toEntity, queryString } from '@/core/ajax/helpers';
import { AxiosInstance } from 'axios';
import { ImportTask, mapImportTaskFormServer } from '@/model/admin/ImportTask';

const BASE_URL = '/import';

/**
 * API que se encarga de la importacion
 */
export class ImportAPI {
  constructor(private http: AxiosInstance) {}

  /**
   * Carga en el server un archivo a importar.
   * El server analiza el archivo y devuelve una tarea que incluye la info y
   * que se usa para confirmar la importacion.
   * @param file archivo a importar
   * @param params indica si se debe importar en la organizacion actual
   * @returns la tarea de importacion creada con la info analizada
   */
  create(file: File, params: { enActual: boolean }): Promise<ImportTask> {
    const form = new FormData();
    form.append('file', file);
    form.append('enActual', '' + params.enActual);
    return this.http.post(BASE_URL, form).then(toEntity(mapImportTaskFormServer));
  }

  /**
   * Obtiene una tarea de importacion creada previamente por UUID
   * @param uuid
   */
  get(uuid: string): Promise<ImportTask> {
    return this.http.get(BASE_URL + '/' + uuid).then(toEntity(mapImportTaskFormServer));
  }

  /**
   * Ejecuta una tarea de importacion creada previamente
   * @param uuid id de la tarea
   * @param params indica la estrategia de importacion de cuentas y si se deben importar los asientos e imputaciones.
   */
  async run(uuid: string, params: { cuentas: 'CODIGO' | 'NIVEL'; asientos: boolean }): Promise<void> {
    await this.http.put(`${BASE_URL}/${uuid}${queryString(params)}`);
  }
}
