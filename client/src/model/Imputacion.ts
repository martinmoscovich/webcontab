import { Nullable } from '@/core/TypeHelpers';
import { cuentaStore } from '@/store';
import { Component, Vue } from 'vue-property-decorator';
import { Cuenta } from './Cuenta';
import { ImputacionDTO, UpstreamImputacionDTO } from './ImputacionDTO';

/**
 * Modelo de imputacion que se utiliza en los componentes.
 * Es reactiva, permitiendo recalcular los valores en caso de ser necesario.
 * Cuando se quiere obtener del server o enviar al mismo, se la convierte en los DTOs.
 */
@Component
export class ImputacionModel extends Vue implements Nullable<Omit<ImputacionDTO, 'asiento'>> {
  id: number | null = null;
  private cuentaId: number | null = null;
  detalle: string | null = null;
  // tipo: "DEBE" | "HABER" = "DEBE";
  importe = 0;

  /** Carga esta imputacion a partir del DTO */
  fromDTO(imputacion: ImputacionDTO) {
    this.id = imputacion.id;
    this.detalle = imputacion.detalle;
    this.importe = imputacion.importe || 0;
    this.cuenta = imputacion.cuenta as Cuenta;
  }

  /** Cuenta asociada a la imputacion */
  set cuenta(cuenta: Cuenta | null) {
    // Solo se guarda el id
    this.cuentaId = cuenta?.id ?? null;
  }

  get cuenta(): Cuenta | null {
    // Se obtiene la cuenta del store a partir del id
    if (!this.cuentaId) return null;
    return cuentaStore.find(this.cuentaId) as Cuenta;
  }

  /**
   * Valida la imputacion.
   * @returns objeto que indica si es valida y el mensaje de error si no lo es
   */
  validar() {
    if (!this.cuenta) return { msg: 'La imputacion debe tener cuenta asociada' };
    if (!this.detalle) return { msg: 'La imputacion debe tener un detalle' };
    if (!this.importe || this.importe === 0) return { msg: 'La imputacion debe tener un importe' };

    return { valid: true };
  }

  /**
   * Genera el DTO para enviar al server
   * @param validate indica si se debe validar la imputacion primero
   * @returns el DTO o null si no es valido
   */
  toDTO(validate: boolean): UpstreamImputacionDTO | null {
    if (validate && !this.validar().valid) return null;
    return {
      id: this.id ?? undefined,
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      cuenta: { id: this.cuentaId! },
      detalle: this.detalle || '',
      importe: this.importe
    };
  }
}
