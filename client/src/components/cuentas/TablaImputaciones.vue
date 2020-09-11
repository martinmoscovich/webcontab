<template>
  <b-table
    :row-class="() => 'imputacion-row'"
    :data="imputacionesConSaldo"
    narrowed
    hoverable
    :loading="loading"
    @click="onItemClick"
  >
    <template slot-scope="props">
      <!-- Numero de asiento -->
      <b-table-column field="numero" label="Asiento" width="40" numeric>
        <template v-if="isNotHeader(props.row)">{{ props.row.imputacion.asiento.numero }}</template>
      </b-table-column>

      <!-- Fecha del asiento -->
      <b-table-column field="fecha" label="Fecha" centered>
        <small v-if="isNotHeader(props.row)" class="has-text-grey is-abbr-like">{{
          formatDate(props.row.imputacion.asiento.fecha)
        }}</small>
      </b-table-column>

      <!-- Detalle imputacion -->
      <b-table-column field="detalle" label="Detalle">
        {{ props.row.imputacion.detalle }}
      </b-table-column>

      <!-- Debe -->
      <b-table-column field="debe" label="Debe" numeric>
        <template v-if="isNotHeader(props.row)">
          {{ props.row.imputacion.importe >= 0 ? formatCurrency(props.row.imputacion.importe) : '' }}
        </template>
      </b-table-column>

      <!-- Haber -->
      <b-table-column field="haber" label="Haber" numeric>
        <template v-if="isNotHeader(props.row)">
          {{ !(props.row.imputacion.importe >= 0) ? formatCurrency(props.row.imputacion.importe * -1) : '' }}
        </template>
      </b-table-column>

      <!-- Saldo parcial -->
      <b-table-column
        field="saldo"
        label="Saldo"
        numeric
        :class="{
          'has-text-danger': props.row.saldo < 0,
          'has-text-weight-bold': !isNotHeader(props.row)
        }"
      >
        <!-- Checkbox que indica si usar saldo anterior o no -->
        <b-checkbox
          :value="useSaldoAnterior"
          @input="useSaldoAnterior = !useSaldoAnterior"
          v-if="!isNotHeader(props.row)"
          >{{ formatCurrency(useSaldoAnterior ? props.row.saldo : 0) }}
        </b-checkbox>
        <template v-else>{{ formatCurrency(props.row.saldo) }}</template>
      </b-table-column>
    </template>

    <!-- Contenido cuando no hay imputaciones -->
    <template slot="empty">
      <section class="section">
        <div class="content has-text-grey has-text-centered">
          <p>
            <b-icon icon="emoticon-sad" size="is-large"> </b-icon>
          </p>
          <p>No hay imputaciones</p>
        </div>
      </section>
    </template>
  </b-table>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import Page from '@/core/Page';
import { formatDate } from '@/utils/date';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { twoDecimals, formatCurrency } from '../../utils/currency';
import { ImputacionDTO } from '../../model/ImputacionDTO';
import { isDefined } from '../../utils/general';
import { Moneda } from '@/model/Moneda';

/**
 * Imputaciones de una cuenta (para el Mayor)
 */
@Component({
  components: {}
})
export default class TablaImputaciones extends Vue {
  /** Pagina actual de imputaciones */
  @Prop()
  page: Page<ImputacionDTO>;

  /** Saldo anterior a la primer imputacion de la pagina actual */
  @Prop()
  saldoAnterior: number;

  /** Indica si esta cargando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Moneda de la cuenta */
  @Prop()
  moneda: Moneda;

  /** Indica si se muestra el saldo anterior en la primera fila */
  private useSaldoAnterior = true;

  /** Recorre la pagina de imputaciones y genera el saldo parcial por cada una */
  private get imputacionesConSaldo(): ImputacionSaldo[] {
    if (this.page?.items?.length === 0) return [];

    // Si se usa el saldo anterior y esta definido, arranca de dicho saldo.
    // Si no, arranca de 0
    let saldo = this.useSaldoAnterior && this.saldoAnterior ? this.saldoAnterior : 0;

    // Se va acumulando el saldo en cada item y se genera un item con la imputacion y el saldo parcial
    const items = this.page.items.map(imputacion => {
      saldo = twoDecimals(saldo + (imputacion.importe ?? 0));
      return { imputacion, saldo };
    });

    // Si hay saldo anterior, se lo agrega como primera fila de la tabla
    if (this.hasSaldoAnterior) items.unshift(this.imputacionSaldoAnterior);
    return items;
  }

  /** Indica si hay saldo anterior */
  private get hasSaldoAnterior() {
    return isDefined(this.saldoAnterior) && this.saldoAnterior !== 0;
  }

  /** Genera el item "placeholder" para el saldo anterior */
  private get imputacionSaldoAnterior() {
    return {
      imputacion: {
        id: -1,
        cuenta: { id: -1 },
        asiento: {
          id: -1,
          numero: -1,
          detalle: '',
          imputaciones: [],
          fecha: this.page?.items?.[0].asiento.fecha
        },
        detalle: 'Saldo Anterior',
        importe: 0,
        tipo: 'DEBE'
      },
      saldo: this.saldoAnterior
    };
  }

  /** Formatea una fecha */
  private formatDate(date: Date) {
    return formatDate(date);
  }

  /** Formatea un importe */
  private formatCurrency(importe: number) {
    return formatCurrency(importe, this.moneda?.simbolo + ' ' ?? '');
  }

  /** Indica si es una imputacion o el saldo anterior */
  private isNotHeader(item: ImputacionSaldo) {
    return item.imputacion.id > -1;
  }

  /** Handler cuando se selecciona una imputacion */
  private onItemClick(item: ImputacionSaldo) {
    if (item.imputacion.id === -1) return;
    this.$emit('itemClick', item.imputacion);
  }
}
</script>

<style lang="scss">
.imputacion-row {
  cursor: pointer;
}
</style>
