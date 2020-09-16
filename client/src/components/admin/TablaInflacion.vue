<template>
  <b-table
    :row-class="() => 'imputacion-row'"
    :data="indices"
    narrowed
    hoverable
    :loading="loading"
    @click="onSelected"
    v-click-outside="onClickOutside"
  >
    <template slot-scope="props">
      <!-- Columna Mes -->
      <b-table-column field="mes" label="Mes" :class="{ 'has-text-danger': !props.row.indice }">
        {{ formatMonth(props.row.mes) }}
      </b-table-column>

      <!-- Columna Indice que puede editarse -->
      <b-table-column field="indice" label="Indice" numeric width="100px">
        <currency-input
          v-if="selected && isSelected(props.row)"
          ref="txtIndice"
          class="input"
          style="width: 100px"
          v-model="selected.indice"
          :precision="4"
          :currency="{ prefix: '', suffix: '' }"
          :allow-negative="false"
          :distraction-free="true"
          @keydown.native.enter="onSaveItem"
          @keydown.native.esc="onCancelItem"
        />
        <template v-else> {{ props.row.indice }}</template>
      </b-table-column>
    </template>

    <!-- Si no puede traer ningun indice (no deberia pasar) -->
    <template slot="empty">
      <section class="section">
        <div class="content has-text-grey has-text-centered">
          <p>
            <b-icon icon="emoticon-sad" size="is-large"> </b-icon>
          </p>
          <p>No hay Indices</p>
        </div>
      </section>
    </template>
  </b-table>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Focusable } from '@/utils/browser';
import { number } from '@/utils/validation';
import { notificationService } from '@/service';
import { InflacionMes } from '@/model/InflacionMes';
import { prettyFormatDate } from '@/utils/date';

/**
 * Tabla que permite ver y editar los indices de inflacion
 */
@Component({
  validators: { 'selected.indice': number({ required: true, greaterThan: 0 }) }
})
export default class TablaInflacion extends Vue {
  @Prop({ type: Array })
  indices: InflacionMes[];

  @Prop({ type: Boolean })
  loading: boolean;

  /** Indice seleccionado */
  private selected: InflacionMes | null = null;

  $refs: { txtIndice: Focusable & Vue };

  /** Indica si el indice especificado es el seleccionado */
  private isSelected(indice: InflacionMes) {
    if (indice.id && this.selected?.id) {
      return this.selected.id === indice.id;
    } else {
      return this.selected?.mes.getTime() === indice.mes.getTime();
    }
  }

  /** Formatea el mes del indice */
  private formatMonth(date: Date) {
    return prettyFormatDate(date, { shortMonth: false }).substring(2);
  }

  /** Handler cuando se hace click en un item */
  private async onSelected(row: InflacionMes) {
    // Si ya esta seleccionado, no se hace nada
    if (this.isSelected(row)) return;

    // Se clona el seleccionado
    this.selected = { ...row };

    await this.$nextTick();
    (this.$refs.txtIndice.$el as HTMLElement).focus();
    (this.$refs.txtIndice.$el as HTMLInputElement).select();
  }

  /** Handler cuando se guarda un indice */
  async onSaveItem() {
    if (await this.$validate()) {
      this.$emit('change', this.selected);
      this.selected = null;
    } else {
      notificationService.warn('El índice debe ser un número mayor a cero');
    }
  }

  /** Handler cuando se hace click fuera del componente */
  private onClickOutside() {
    this.onCancelItem();
  }

  /** Handler cuando se cancela la edicion */
  private onCancelItem() {
    this.selected = null;
  }
}
</script>
