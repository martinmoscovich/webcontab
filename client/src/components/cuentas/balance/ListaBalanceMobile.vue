<template>
  <div>
    <b-loading :is-full-page="true" :active="loading" />
    <ItemBalanceMobile v-for="item in page.items" :key="item.id" :item="item" @click="onItemClick" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import Page from '@/core/Page';
import { BalanceItem } from '@/model/Balance';
import ItemBalanceMobile from './ItemBalanceMobile.vue';

/** Lista de Balance de Cuentas (cuentas con sus saldos) para mobile */
@Component({ components: { ItemBalanceMobile } })
export default class ListaBalanceMobile extends Vue {
  /** Pagina actual */
  @Prop()
  page: Page<BalanceItem>;

  /** Indica si esta cargando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Handler cuando se selecciona una cuenta  */
  private onItemClick(item: BalanceItem) {
    this.$emit('selected', item);
  }
}
</script>
