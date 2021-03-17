<template>
  <div>
    <b-loading :is-full-page="true" :active="loading" />

    <!-- Saldo anterior -->
    <article v-if="hasSaldoAnterior" class="media card selectable card-list-item mb-0 mt-0 pa-1">
      <div class="media-content ml-1 mb-1 mt-1">
        <div class="content">
          <strong>Saldo Anterior</strong>
          <div class="has-text-right" :class="{ 'has-text-danger': saldoAnterior < 0 }">
            {{ formatCurrency(saldoAnterior) }}
          </div>
        </div>
      </div>
    </article>

    <!-- Items -->
    <ItemMayorMobile
      v-for="item in imputacionesConSaldo"
      :key="item.imputacion.id"
      :item="item"
      :cuenta="item.imputacion.cuenta"
      @itemClick="onItemClick"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import ItemMayorMobile from './ItemMayorMobile.vue';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { isDefined } from '@/utils/general';
import { formatCurrency } from '@/utils/currency';
import { Moneda } from '@/model/Moneda';

/** Lista de imputaciones (mayor) para mobile */
@Component({ components: { ItemMayorMobile } })
export default class ListaImputacionesMobile extends Vue {
  /** Pagina actual de imputaciones */
  @Prop()
  imputacionesConSaldo: ImputacionSaldo[];

  /** Saldo anterior a la primer imputacion de la pagina actual */
  @Prop()
  saldoAnterior: number;

  /** Indica si esta cargando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Moneda de la cuenta */
  @Prop()
  moneda: Moneda;

  /** Formatea un importe */
  private formatCurrency(importe: number) {
    return formatCurrency(importe, this.moneda?.simbolo + ' ' ?? '');
  }

  /** Indica si hay saldo anterior */
  private get hasSaldoAnterior() {
    return isDefined(this.saldoAnterior) && this.saldoAnterior !== 0;
  }

  /** Handler cuando se selecciona una imputacion */
  private onItemClick(item: ImputacionSaldo) {
    if (item.imputacion.id === -1) return;
    this.$emit('itemClick', item.imputacion);
  }
}
</script>

<style lang="scss" scoped>
.content {
  display: flex;
  justify-content: space-between;
}
</style>
