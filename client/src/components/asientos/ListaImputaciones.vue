<template>
  <CardComponentWithActions title="Imputaciones" icon="ballot" dense class="card-imputaciones">
    <!-- Boton de nueva imputacion -->
    <template v-slot:title-actions>
      <b-tooltip v-if="!readonly" label="Nueva imputación" position="is-bottom" type="is-info">
        <b-button icon-left="plus" @click="onNewClick" />
      </b-tooltip>
    </template>

    <!-- Saldos del asiento por moneda -->
    <template v-if="!loading" v-slot:actions>
      <span class="mr-3">Saldo</span>
      <span
        v-for="(item, index) in formattedSaldos"
        class="title mb-0 ml-3"
        :key="index"
        :class="{ 'has-text-danger': !item.cero }"
        >{{ item.formattedSaldo }}</span
      >
    </template>

    <!-- Lista de Forms de imputaciones -->
    <ImputacionItem
      v-for="(item, index) in imputacionesConSaldo"
      ref="items"
      :key="index"
      :item="item"
      :isFirst="index === 0"
      :isLast="index === imputaciones.length - 1"
      :readonly="readonly"
      @rowRequested="onRowRequested"
      @input="onImputacionesChange"
      @delete="onDelete(index)"
    />

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { formatCurrency, twoDecimals } from '@/utils/currency';
import ImputacionItem from '@/components/asientos/ImputacionItem.vue';
import { ImputacionModel } from '@/model/Imputacion';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { cuentaStore, monedaStore } from '../../store';
import { ValidableVue } from '../../core/ui/elements';

/**
 * Form de imputaciones.
 * Permite ver/editar imputaciones.
 */
@Component({ components: { ImputacionItem } })
export default class ListaImputaciones extends Vue implements ValidableVue {
  /** Imputaciones a ver/editar */
  @Prop()
  imputaciones: ImputacionModel[];

  /** Indica si se estan cargando las imputaciones */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Indica si no se pueden modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /**
   * Saldos del asiento por moneda.
   * Se utiliza para mostrar dichos saldos en el header, al lado del titulo "Imputaciones"
   */
  @Prop({ type: Object })
  saldos: Record<string, number>;

  $refs: { items: ImputacionItem[] };

  /** Valida el form de CADA imputacion */
  async validate(): Promise<boolean> {
    const v = await Promise.all(this.$refs.items.map(i => i.validate()));
    return v.every(r => r);
  }

  /** Resetea todos los forms de imputaciones */
  reset() {
    this.$refs.items?.forEach(i => i.reset());
  }

  /** Calcula los saldos con el formato correcto y el simbolo de la moneda */
  private get formattedSaldos() {
    return (
      Object.keys(this.saldos)
        .map(id => {
          const monedaId = parseInt(id, 10);
          return {
            cero: this.saldos[id] === 0,
            monedaId,
            formattedSaldo: formatCurrency(this.saldos[id], monedaStore.find(monedaId)?.simbolo || '')
          };
        })
        // Solo se incluyen si tienen moneda y no son 0
        .filter(s => s.monedaId !== -1 || !s.cero)
    );
  }

  /**
   * Genera la imputaciones con saldo.
   * Procesa las imputaciones una por una y devuelve un objeto que tiene la imputacion y el
   * saldo parcial del asiento hasta esa imputacion
   */
  get imputacionesConSaldo(): ImputacionSaldo<ImputacionModel>[] {
    if (this.loading || this.imputaciones?.length === 0) return [];
    const saldos: Record<number, number> = {};

    // TODO Ver que hacer si no esta configurada la moneda
    // Se recorren las imputaciones
    return this.imputaciones.map(imputacion => {
      // Se obtiene la cuenta y la moneda de la imputacion
      const cuenta = this.getCuentaParaImputacion(imputacion);
      const monedaId = cuenta?.monedaId ?? -1;

      // Valor de la imputacion
      const valor = isNaN(imputacion.importe) ? 0 : imputacion.importe;

      // Se acumula el valor para la moneda indicada
      saldos[monedaId] = twoDecimals((saldos[monedaId] ?? 0) + valor);

      // Se genera un item con la imputacion y el saldo parcial
      return {
        imputacion,
        saldo: saldos[monedaId]
      };
    });
  }

  /** Obtiene la cuenta cacheada para una imputacion (a partir del id de cuenta) */
  private getCuentaParaImputacion(imp: ImputacionModel) {
    if (!imp.cuenta) return undefined;
    const cuenta = cuentaStore.findCuenta(imp.cuenta.id);
    if (!cuenta) return undefined;
    return cuenta;
  }

  /** Handler cuando se desea crear una nueva imputacion */
  private onRowRequested() {
    const imputacion = new ImputacionModel();
    // Si no es el primer item, se copia el detalle del ultimo
    if (this.imputaciones.length > 0) {
      imputacion.detalle = this.imputaciones[this.imputaciones.length - 1].detalle;
    }

    // Se agrega la imputacion
    this.imputaciones.push(imputacion);

    // Se emite evento de cambio
    this.onImputacionesChange();
  }

  /**
   * Handler de click en "Nueva imputacion".
   * Se crea una fila nueva y se hace focus en ella
   */
  private async onNewClick() {
    this.onRowRequested();
    await this.$nextTick();
    this.$refs.items[this.$refs.items.length - 1].focus();
  }

  /** Handler de borrar una imputacion */
  private onDelete(index: number) {
    // Se elimina localmente (los cambios en el server se hacen en un solo req)
    if (this.imputaciones.length > 0) this.imputaciones.splice(index, 1);

    if (this.imputaciones.length === 0) {
      // Si no quedo ninguna, debe haber al menos una
      // Se crea y emite el evento
      this.onRowRequested();
    } else {
      // Se emite evento de cambio
      this.onImputacionesChange();
    }
  }

  /** Handler de que hubo algun cambio en cualquier imputacion */
  private onImputacionesChange() {
    this.$emit('input', this.imputaciones);
  }
}
</script>

<style lang="scss">
.card-imputaciones {
  .card-content {
    margin-bottom: 30px;
  }
}
</style>
