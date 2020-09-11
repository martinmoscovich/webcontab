<template>
  <CardComponentWithActions title="Monedas" icon="ballot" dense list>
    <template v-slot:actions>
      <!-- Boton nueva moneda -->
      <b-tooltip label="Nueva moneda" position="is-bottom" type="is-info">
        <b-button icon-left="plus" @click="onNewClick" />
      </b-tooltip>
    </template>

    <!-- Item -->
    <MonedaItem v-for="moneda in items" :key="moneda.id || moneda.nombre" :moneda="moneda" @click="onSelected" />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import MonedaItem from './MonedaItem.vue';
import { Moneda } from '@/model/Moneda';

/**
 * Lista de monedas disponibles
 */
@Component({ components: { MonedaItem } })
export default class MonedaList extends Vue {
  @Prop()
  items: Moneda[];

  /** Handler de click en "Nueva" */
  private onNewClick() {
    this.$emit('new');
  }

  /** Handler de seleccion de una moneda */
  private onSelected(moneda: Moneda) {
    this.$emit('selected', moneda);
  }
}
</script>

<style lang="stylus" scoped></style>
