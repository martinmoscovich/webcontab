import { User } from '@/model/User';

/**
 * Interfaz para modelos auditables
 */
export interface Auditable {
  creationUser: User | null;
  creationDate: Date | null;

  updateUser: User | null;
  updateDate: Date | null;
}

/**
 * Convierte los atributos auditables del DTO del server al del cliente
 * @param json
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function mapAuditableFromServer(json: any): Auditable {
  return {
    ...json,
    creationDate: json.creationDate ? new Date(json.creationDate) : null,
    updateDate: json.updateDate ? new Date(json.updateDate) : null
  };
}
