/* eslint-disable @typescript-eslint/no-explicit-any */
/*****************************************
 *          APP UPDATE
 *****************************************/

export type UpdateState = 'IDLE' | 'DOWNLOADING' | 'UPDATING' | 'RESTARTING';

/** Estado del actualizador */
export interface UpdateStatus {
  /** Tarea que esta realizando */
  action: UpdateState;

  /** Release actual ejecutando en el servidor */
  currentRelease: CurrentReleaseInfo;

  /** Release al cual se esta actualizando */
  targetRelease?: ReleaseInfo;
}

/**
 * Info de la release
 */
export interface CurrentReleaseInfo {
  releaseVersion: string;
  releaseDate: Date;
  serverVersion: string;
}

/**
 * Info extendida de la release que incluye el tamanio del archivo y
 * si requiere actualizacion
 */
export interface ReleaseInfo extends CurrentReleaseInfo {
  fileSize: number;

  /** Indica que se debe reiniciar el server para aplicar la actualizacion */
  requiresRestart: boolean;
}

function mapReleaseInfoFromServer(json: any) {
  return {
    ...json,
    releaseDate: new Date(json.releaseDate)
  };
}

export function mapUpdateStatusFromServer(json: any) {
  return {
    ...json,
    currentRelease: mapReleaseInfoFromServer(json.currentRelease),
    targetRelease: json.targetRelease ? mapReleaseInfoFromServer(json.targetRelease) : undefined
  };
}

/*****************************************
 *          DB BACKUP
 *****************************************/

/**
 * Backup de base de datos
 */
export interface BackupItem {
  /** Nombre del backup */
  nombre: string;

  /** Fecha del backup */
  ts: Date;

  /**
   * Tipo
   * Puede ser archivo binario o script SQL.
   */
  tipo: 'DB' | 'SQL';
}

export function mapDbBackupItem(json: any): BackupItem {
  return {
    ...json,
    ts: new Date(json.ts)
  };
}
