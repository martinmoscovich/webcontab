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
    @click="onRowClick"
    @page-change="onPageChange"
  >
    <template slot-scope="props">
      <b-table-column field="numero" label="Número" width="40" numeric>
        <template slot="header" slot-scope="{ column }">
          <b-checkbox v-model="areExpanded">{{ column.label }}</b-checkbox>
        </template>
        <small>
          <router-link :to="getAsientoRoute(props.row)">{{ props.row.numero }}</router-link>
        </small>
      </b-table-column>

      <b-table-column field="fecha" label="Fecha" centered>
        <small class="has-text-grey is-abbr-like">
          <router-link :to="getAsientoRoute(props.row)"> {{ formatDate(props.row.fecha) }}</router-link>
        </small>
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
      <router-link
        tag="tr"
        class="hand"
        v-for="item in props.row.imputaciones"
        :key="item.id"
        :to="getImputacionRoute(item)"
      >
        <td></td>
        <td></td>
        <td></td>
        <td>
          <small>
            <router-link :to="getCuentaRoute(item.cuenta)">{{ item.cuenta.codigo }}</router-link>
          </small>
        </td>
        <td>
          <small>
            <router-link :to="getCuentaRoute(item.cuenta)">{{ item.cuenta.descripcion }}</router-link>
          </small>
        </td>
        <td>
          <small>
            <router-link :to="getImputacionRoute(item)">{{ item.detalle }}</router-link>
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
      </router-link>
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
import { formatCurrency } from '@/utils/currency';
import { Cuenta } from '@/model/Cuenta';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import { monedaStore } from '@/store';
import { IdModel } from '@/model/IdModel';
import { routerService } from '@/service';

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

  /** Devuelve el link a la pagina para ver el asiento indicado */
  private getAsientoRoute(item: AsientoDTO) {
    return routerService.asiento(item);
  }

  /** Devuelve el link a la pagina para ver la cuenta indicada */
  private getCuentaRoute(cuenta: Cuenta) {
    return routerService.cuenta(cuenta);
  }

  /** Devuelve el link a la pagina para ver la imputacion indicada */
  private getImputacionRoute(imputacion: ImputacionDTO) {
    return routerService.imputacion(imputacion);
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
  private onRowClick(row: object) {
    routerService.goToAsiento(row);
  }
}
</script>

<style lang="scss">
.asiento-row {
  cursor: pointer;
}
</style>
