<template>
  <article class="media card selectable card-list-item mb-0 mt-0 pa-1" @click="onClick">
    <!-- Cuenta  -->
    <div class="media-content ml-1">
      <div class="content">
        <p>
          <strong>#{{ imputacion.asiento.numero }}</strong> - {{ date }}
          <small class="ml-1">{{ cuenta.descripcion }}</small>
          <br />
          {{ imputacion.detalle }}
          <br />
          <strong :class="{ 'has-text-danger': imputacion.importe < 0 }">{{ formattedImporte }}</strong>
        </p>
      </div>
      <div class="has-text-right" :class="{ 'has-text-danger': item.saldo < 0 }">Saldo: {{ formattedSaldo }}</div>
    </div>
  </article>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { Cuenta } from '@/model/Cuenta';
import { formatCurrency } from '@/utils/currency';
import { monedaStore } from '@/store';
import { routerService } from '@/service';
import { formatDate } from '@/utils/date';

/** Item de mayor para mobile */
@Component
export default class ItemMayorMobile extends Vue {
  @Prop()
  item: ImputacionSaldo;

  @Prop()
  cuenta: Cuenta;

  /** Handler cuando se hace click en un item */
  private onClick() {
    this.$emit('itemClick', this.item);
  }

  private get imputacion() {
    return this.item.imputacion;
  }

  /** Devuelve el link a la pagina para ver la imputacion indicada */
  private get link() {
    return routerService.imputacion(this.imputacion);
  }

  private get formattedImporte() {
    return formatCurrency(this.imputacion.importe, monedaStore.find(this.cuenta.monedaId)?.simbolo);
  }

  /** Formatea un monto */
  private get formattedSaldo() {
    return formatCurrency(this.item.saldo, monedaStore.find(this.cuenta.monedaId)?.simbolo);
  }

  /** Fecha formateada */
  private get date() {
    return formatDate(this.imputacion.asiento.fecha);
  }
}
</script>

<style lang="scss" scoped>
.content {
  margin-bottom: 2px !important;
}
</style>
