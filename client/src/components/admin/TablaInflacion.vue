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
          @keydown.native.tab="onTab"
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
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
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

  /** Item seleccionado */
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

  /**
   * Selecciona el item que esta a la distancia especificada desde el seleccionado
   * Si no hay seleccionado, se considera el primero como seleccionado.
   * @param step distancia a moverse, puede ser negativa para ir hacia atras
   */
  private goTo(step: number) {
    // Si hay un item seleccionado, se toma su indice.
    // Si no, se toma el primer item
    const index = this.selected ? this.indices.findIndex(indice => this.isSelected(indice)) : 0;

    const target = index + step;
    if (target >= 0 && target < this.indices.length) this.onSelected(this.indices[target]);
  }

  /** Handler cuando cambia el status de "cargando" */
  @Watch('loading')
  private onLoadingChange() {
    // Solo nos interesa cuando dejo de cargar
    if (this.loading) return;

    // Si hay seleccionado, ya se guardo, se elige el proximo
    if (this.selected) this.goTo(1);
  }

  /** Handler cuando cambia la lista */
  @Watch('indices')
  private onListChange(current: InflacionMes[], old: InflacionMes[]) {
    // Si la lista cambio de tamaño, significa que se cargo por primera vez o cambio el filtro.
    // Se selecciona el primer item
    if (current.length && old.length !== current.length) this.goTo(0);
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
  private async onSaveItem() {
    if (await this.$validate()) {
      this.$emit('change', this.selected);
    } else {
      notificationService.warn('El índice debe ser un número mayor a cero');
    }
  }

  /** Handler cuando se presiona Tab */
  private onTab(e: KeyboardEvent) {
    e.preventDefault();
    this.goTo(e.shiftKey ? -1 : 1);
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
