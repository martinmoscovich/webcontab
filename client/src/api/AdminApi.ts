import { toEntity } from '@/core/ajax/helpers';
import { AxiosInstance } from 'axios';
import { UpdateStatus, mapUpdateStatusFromServer, mapDbBackupItem, BackupItem } from '@/model/admin/AdminModels';

const BASE_URL = '/admin';
const DB_URL = BASE_URL + '/db';
const UPDATE_URL = BASE_URL + '/update';

/**
 * API de mantenimiento
 */
export class AdminApi {
  constructor(private http: AxiosInstance) {}

  /*****************************************
   *              DB BACKUP
   *****************************************/

  /** Obtiene el ultimo backup de base de datos, si existe */
  dbBackupGetLast(): Promise<BackupItem | null> {
    return this.http.get(DB_URL + '/backups/last').then(r => (r.status === 204 ? null : mapDbBackupItem(r.data)));
  }

  /**
   * Crea un backup de la base de datos
   */
  createDbBackup(): Promise<BackupItem> {
    return this.http.post(DB_URL + '/backups').then(toEntity(mapDbBackupItem));
  }

  // dbBackupList(): Promise<Array<BackupItem>> {
  //   return this.http.get(DB_URL + "/backups").then(toList(mapDbBackupItem));
  // }

  /*****************************************
   *             APP UPDATE
   *****************************************/

  /** Comprueba si existe una actualizacion disponible */
  updateCheck(): Promise<string | null> {
    return this.http.get(UPDATE_URL + '/check').then(r => (r.data.pending ? r.data.version : null));
  }

  /** Obtiene el estado actual del updater */
  updateStatus(): Promise<UpdateStatus> {
    return this.http.get(UPDATE_URL + '/status').then(toEntity(mapUpdateStatusFromServer));
  }

  /** Descarga una actualizacion del server de actualizacion */
  async updateDownload() {
    await this.http.post(UPDATE_URL + '/download');
  }

  /** Instala una actualizacion */
  async updateApply() {
    await this.http.post(UPDATE_URL + '/apply');
  }
}
