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

  /** Lista de monedas */
  private get lista() {
    return monedaStore.lista.items;
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
