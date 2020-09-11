import { Nullable } from '@/core/TypeHelpers';
import { asientoStore, sessionStore } from '@/store';
import { twoDecimals } from '@/utils/currency';
import { Component, Vue, Watch } from 'vue-property-decorator';
import { ImputacionModel } from './Imputacion';
import { AsientoDTO, UpstreamAsientoDTO } from './AsientoDTO';
import { UpstreamImputacionDTO } from './ImputacionDTO';
import { formatDate } from '@/utils/date';
import { Dictionary } from 'vue-router/types/router';
import { FormValidation } from '@/model/FormValidation';

/**
 * Modelo de asiento que se utiliza en los componentes.
 * Es reactivo, permitiendo recalcular los valores en caso de ser necesario.
 * Cuando se quiere obtener del server o enviar al mismo, se lo convierte en los DTOs.
 */
@Component
export class AsientoModel extends Vue implements Nullable<Omit<AsientoDTO, 'imputaciones'>> {
  id: number | null = null;
  numero: number | null = null;
  fecha: Date | null = null;
  detalle: string | null = null;
  imputaciones: ImputacionModel[] = [];

  /**
   * Setea el id del asiento.
   * Esto dispara su busqueda y carga desde el store
   */
  setId(id: number | null) {
    this.id = id;
    if (this.id === null) this.setAsiento();
  }

  /** Carga los datos del asiento y sus imputaciones */
  @Watch('asiento')
  private setAsiento() {
    if (this.asiento) {
      // Existente
      this.numero = this.asiento.numero ?? null;
      this.fecha = this.asiento.fecha ?? null;
      this.detalle = this.asiento.detalle ?? null;
      this.imputaciones = this.asiento.imputaciones
        ? this.asiento.imputaciones.map(i => {
            const imp = new ImputacionModel();
            imp.fromDTO(i);
            return imp;
          })
        : [];
    } else {
      // Nuevo, todo null y una imputacion vacia
      this.numero = null;
      this.fecha = null;
      this.detalle = null;
      this.imputaciones = [new ImputacionModel()];
    }
  }

  /** Asiento cargado del store */
  private get asiento(): Partial<AsientoDTO> | null {
    if (!this.id) return null;
    return asientoStore.find(this.id) ?? null;
  }

  /** Crea una nueva imputacion en el asiento */
  newImputacion() {
    this.imputaciones.push(new ImputacionModel());
  }

  /**
   * Valida el asiento y sus imputaciones.
   * @returns objeto que indica si es valida y el mensaje de error si no lo es
   */
  validar(): FormValidation {
    if (!this.fecha) return { msg: 'Ingrese los datos requeridos' }; //{ msg: "Debe completar la fecha del asiento" };

    // Se valida que la fecha este dentro del ejercicio
    const ej = sessionStore.ejercicio;
    if (ej) {
      const ts = this.fecha.getTime();
      if (ts < ej.inicio.getTime() || ts > ej.finalizacion.getTime()) {
        return {
          msg: `La fecha indicada no esta dentro del ejercicio (${formatDate(ej.inicio)} - ${formatDate(
            ej.finalizacion
          )})`
        };
      }
    }

    // Tiene que haber imputaciones
    if (this.imputaciones.length === 0) return { msg: 'Ingrese imputaciones' };

    // Los saldos en todas las monedas deben ser 0
    if (!this.saldosEnCero) return { msg: 'El saldo del asiento no es cero' };

    // Todas las imputaciones deben ser validas
    if (this.imputaciones.some(i => !i.validar().valid)) {
      return { msg: 'Ingrese correctamente las imputaciones' };
    }
    return { valid: true };
  }

  /**
   * Mapa que conviene como key el id de la moneda y como value el saldo del asiento para dicha moneda.
   * Al ser un getter Vue, se actualiza en tiempo real
   */
  get saldos(): Dictionary<number> {
    const result: Record<string, number> = {};
    if (!this.imputaciones) return result;

    // Por cada imputacion
    for (const imp of this.imputaciones) {
      // Se obtiene el id de moneda
      const monedaId = imp.cuenta?.monedaId ?? -1;

      // Se acumula el saldo para esa moneda (usando 2 decimales)
      result[monedaId] = twoDecimals((result[monedaId] ?? 0) + (imp.importe ?? 0));
    }

    // Se asegura que sean 2 decimales
    Object.keys(result).forEach((k: string) => {
      result[k] = Math.floor(result[k] * 100) / 100;
    });

    return result;
  }

  /**
   * Indica si los saldos en todas las monedas son 0.
   * Al ser un getter Vue, se actualiza en tiempo real
   */
  get saldosEnCero() {
    return Object.values(this.saldos).every(saldo => saldo === 0);
  }

  /**
   * Genera el DTO para enviar al server
   * @param validate indica si se debe validar el asiento primero
   * @returns el DTO o null si no es valido
   */
  toDTO(validate: boolean): UpstreamAsientoDTO | null {
    if (validate && !this.validar().valid) return null;

    return {
      id: this.id ?? undefined,
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      fecha: this.fecha!,
      detalle: this.detalle ?? '',
      // Valida y convierte las imputaciones
      imputaciones: this.imputaciones.map(i => i.toDTO(validate)).filter(i => !!i) as UpstreamImputacionDTO[]
    };
  }

  // fromDTO(dto: AsientoDTO) {
  //   this.id = dto.id;
  //   this.fecha = dto.fecha;
  //   this.detalle = dto.detalle;
  //   this.imputaciones = dto.imputaciones.map(idto => {
  //     const imp = new ImputacionModel();
  //     imp.fromDTO(idto);
  //     return imp;
  //   });
  // }
}
