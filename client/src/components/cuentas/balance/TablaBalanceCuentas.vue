<template>
  <b-table
    :row-class="() => 'imputacion-row'"
    :data="page.items"
    narrowed
    hoverable
    :loading="loading"
    :total="page.total"
    paginated
    backend-pagination
    :current-page="page.number"
    :per-page="pageSize"
    @page-change="onPageChange"
    @click="onItemClick"
  >
    <template slot-scope="props">
      <!-- Columna Codigo Cuenta -->
      <b-table-column field="codigo" label="Código" width="40" numeric>
        {{ props.row.codigo }}
      </b-table-column>

      <!-- Columna Descripcion Cuenta -->
      <b-table-column field="descripcion" label="Desc">
        <small class="has-text-grey is-abbr-like">{{ props.row.descripcion }}</small>
      </b-table-column>

      <!-- Columna Saldo -->
      <b-table-column field="saldo" label="Saldo" numeric :class="{ 'has-text-danger': props.row.saldo < 0 }">
        {{ formatSaldo(props.row) }}
      </b-table-column>
    </template>

    <!-- Cuando esta vacia la tablas, mostrar esto -->
    <template slot="empty">
      <section class="section">
        <div class="content has-text-grey has-text-centered">
          <p>
            <b-icon icon="emoticon-sad" size="is-large"> </b-icon>
          </p>
          <p>No hay Cuentas</p>
        </div>
      </section>
    </template>
  </b-table>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import Page from '@/core/Page';
import { BalanceItem } from '@/model/Balance';
import { formatCurrency } from '@/utils/currency';
import { monedaStore } from '@/store';

/** Tabla de Balance de Cuentas (cuentas con sus saldos) */
@Component
export default class TablaBalanceCuentas extends Vue {
  /** Pagina actual */
  @Prop()
  page: Page<BalanceItem>;

  /** Indica si esta cargando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Tamanio de pagina (utilizado por el paginador) */
  @Prop()
  pageSize: number;

  /** Formatea el saldo del item, usando la moneda */
  private formatSaldo(item: BalanceItem) {
    const moneda = monedaStore.find(item.monedaId)?.simbolo ?? '$';
    return formatCurrency(item.saldo, moneda + ' ');
  }

  /** Handler cuando se pide una pagina */
  private onPageChange(page: number) {
    this.$emit('request', page);
  }

  /** Handler cuando se selecciona una cuenta  */
  private onItemClick(item: BalanceItem) {
    this.$emit('selected', item);
  }
}
</script>

<style lang="scss">
.imputacion-row {
  cursor: pointer;
}
</style>
