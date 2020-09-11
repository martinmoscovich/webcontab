<template>
  <b-table
    :row-class="() => 'asiento-row'"
    :data="page.items"
    striped
    narrowed
    hoverable
    detailed
    custom-detail-row
    show-detail-icon
    :opened-detailed="ids"
    detail-key="id"
    :loading="loading"
    paginated
    :current-page="page.number"
    :total="page.total"
    backend-pagination
    :per-page="pageSize"
    @page-change="onPageChange"
  >
    <template slot-scope="props">
      <b-table-column field="numero" label="Número" width="40" numeric>
        <template slot="header" slot-scope="{ column }">
          <b-checkbox v-model="areExpanded">{{ column.label }}</b-checkbox>
        </template>
        <small>
          <a href="#" @click.prevent="onAsientoClick(props.row)">{{ props.row.numero }}</a>
        </small>
      </b-table-column>

      <b-table-column field="fecha" label="Fecha" centered>
        <small class="has-text-grey is-abbr-like">{{ formatDate(props.row.fecha) }}</small>
      </b-table-column>

      <b-table-column field="cuentaCodigo" label="Cuenta"> </b-table-column>

      <b-table-column field="cuentaDescripcion" label=""> </b-table-column>

      <b-table-column field="detalle" label="Detalle">
        {{ props.row.detalle }}
      </b-table-column>

      <b-table-column field="debe" label="Debe" numeric> </b-table-column>

      <b-table-column field="haber" label="Haber" numeric> </b-table-column>
    </template>

    <template slot="detail" slot-scope="props">
      <tr v-for="item in props.row.imputaciones" :key="item.id">
        <td></td>
        <td></td>
        <td></td>
        <td>
          <small>
            <a @click="onCuentaClick(item.cuenta)">
              {{ item.cuenta.codigo }}
            </a>
          </small>
        </td>
        <td>
          <small>
            <a @click="onCuentaClick(item.cuenta)">
              {{ item.cuenta.descripcion }}
            </a>
          </small>
        </td>
        <td>
          <small>
            <a @click="onImputacionClick(item)">{{ item.detalle }}</a>
          </small>
        </td>
        <td class="has-text-right">
          <small>{{
            /* DEBE */
            item.importe >= 0 ? formatCurrency(item.importe, item.cuenta.moneda) : ''
          }}</small>
        </td>
        <td class="has-text-right">
          <small>{{
            /* HABER */
            !(item.importe >= 0) ? formatCurrency(item.importe * -1, item.cuenta.moneda) : ''
          }}</small>
        </td>
      </tr>
    </template>

    <template slot="empty">
      <section class="section">
        <div class="content has-text-grey has-text-centered">
          <p>
            <b-icon icon="emoticon-sad" size="is-large"> </b-icon>
          </p>
          <p>No hay resultados</p>
        </div>
      </section>
    </template>
  </b-table>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { AsientoDTO } from '@/model/AsientoDTO';
import Page from '@/core/Page';
import { formatDate } from '@/utils/date';
import { formatCurrency } from '../../utils/currency';
import { Cuenta } from '../../model/Cuenta';
import { ImputacionDTO } from '../../model/ImputacionDTO';
import { monedaStore } from '@/store';
import { IdModel } from '@/model/IdModel';

/**
 * Tabla de asientos con sus imputaciones.
 * Usada en el mayor
 */
@Component
export default class TablaAsientos extends Vue {
  /** Pagina de asientos actual */
  @Prop()
  page: Page<AsientoDTO>;

  /** Indica que se estan cargando asientos */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Tamanio de pagina (usado en el paginador) */
  @Prop()
  pageSize: number;

  /** Indica si se debe expandir todos los asientos (mostrar sus imputaciones) */
  @Prop({ type: Boolean })
  expandAll: boolean;

  /** Indica si estan expandidos todos los asientos */
  areExpanded = false;

  mounted() {
    this.onExpandChange();
  }

  /**
   * Ids de los items expandidos.
   * Si areExpanded es false, se devuelve vacio.
   * Si es true, se devuelven todos los ids
   */
  private get ids() {
    if (!this.page || !this.areExpanded) return [];
    return this.page.items.map(item => item.id);
  }

  /** Formatea una fecha */
  private formatDate(date: Date) {
    return formatDate(date);
  }

  /** Formatea un monto */
  private formatCurrency(importe: number, moneda: IdModel) {
    return formatCurrency(importe, monedaStore.find(moneda.id)?.simbolo);
  }

  /** Handler cuando cambia la Prop de ExpandAll */
  @Watch('expandAll')
  private onExpandChange() {
    this.areExpanded = this.expandAll;
  }

  /** Handler cuando se pide otra pagina */
  private onPageChange(page: number) {
    this.$emit('request', page);
  }

  /** Handler cuando se selecciona un asiento */
  private onAsientoClick(item: AsientoDTO) {
    this.$emit('asientoSelected', item);
  }

  /** Handler cuando se selecciona una cuenta */
  private onCuentaClick(cuenta: Cuenta) {
    this.$emit('cuentaSelected', cuenta);
  }

  /** Handler cuando se selecciona una imputacion */
  private onImputacionClick(imputacion: ImputacionDTO) {
    this.$emit('imputacionSelected', imputacion);
  }
}
</script>

<style lang="scss">
.asiento-row {
  cursor: pointer;
}
</style>
