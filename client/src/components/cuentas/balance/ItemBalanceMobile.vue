<template>
  <div>
    <article class="media card pa-2 selectable card-list-item" @click="onClick">
      <!-- Cuenta  -->
      <div class="media-content ml-1">
        <div class="content">
          <p>
            <small>{{ item.codigo }}</small>
            <strong> - {{ item.descripcion }}</strong>
          </p>
        </div>
        <div class="has-text-right" :class="{ 'has-text-danger': item.saldo < 0 }">{{ formattedSaldo }}</div>
      </div>
      <div class="media-right"></div>
    </article>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { BalanceItem } from '@/model/Balance';
import { monedaStore } from '@/store';
import { formatCurrency } from '@/utils/currency';

/** Item de balance para mobile */
@Component
export default class ItemBalanceMobile extends Vue {
  @Prop()
  item: BalanceItem;

  /** Handler cuando se hace click en un item */
  private onClick() {
    this.$emit('click', this.item);
  }

  /** Formatea el saldo del item, usando la moneda */
  private get formattedSaldo() {
    const moneda = monedaStore.find(this.item.monedaId)?.simbolo ?? '$';
    return formatCurrency(this.item.saldo, moneda + ' ');
  }
}
</script>

<style lang="scss" scoped>
.content {
  margin-bottom: 2px !important;
}
</style>
