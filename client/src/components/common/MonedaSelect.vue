<template>
  <b-select placeholder="Moneda" :loading="loading" :disabled="readonly" :value="value ? value : null" @input="onInput">
    <option v-for="option in lista" :value="option.id" :key="option.id">
      {{ option.codigo }} ({{ option.simbolo }})
    </option>
  </b-select>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { monedaStore } from '@/store';
import { Moneda } from '@/model/Moneda';
import { ListItemPredicate } from '@/utils/array';

/**
 * Select de Monedas
 */
@Component
export default class MonedaSelect extends Vue {
  /** Id de la moneda seleccionada */
  @Prop()
  value: number;

  /** Indica si no se puede modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /**
   * Funcion que permite filtrar las monedas a mostrar.
   * Si no se define, no se filtran.
   */
  @Prop({ type: Function, required: false })
  filterFn?: ListItemPredicate<Moneda>;

  /** Lista de monedas filtrada con la function provista */
  private get lista() {
    // Si no se paso una funcion del filtrado, se devuelve la lista completa
    if (!this.filterFn) return monedaStore.lista.items;

    return monedaStore.lista.items.filter(this.filterFn);
  }

  /** Indica si se estan cargando las monedas */
  private get loading() {
    return monedaStore.lista.status.loading;
  }

  /** Handler de cuando se selecciona una moneda */
  private onInput(value: number) {
    this.$emit('input', value);
  }
}
</script>
