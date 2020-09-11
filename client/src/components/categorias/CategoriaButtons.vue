<template>
  <b-field>
    <!-- Exportar plan -->
    <!-- <b-field class="mr-1">
      <b-button type="is-success" icon-left="microsoft-excel" tag="a" :href="exportUrl">
        Exportar Plan
      </b-button>
    </b-field> -->

    <!-- Nueva Categoria -->
    <b-field class="mr-1">
      <b-button type="is-info" icon-left="plus" @click="onNewClick('categoria')">
        Categoría
      </b-button>
    </b-field>

    <!-- Nueva Cuenta -->
    <b-field>
      <b-button v-if="cuentaAllowed" type="is-primary" icon-left="plus" @click="onNewClick('cuenta')">
        Cuenta
      </b-button>
    </b-field>
  </b-field>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';

/**
 * Botones de acciones para Categorias
 */
@Component
export default class CategoriaButtons extends Vue {
  /** URL del reporte Plan De Cuentas */
  // @Prop()
  // exportUrl: string;

  /** Indica si se permite crear una nueva cuenta */
  @Prop()
  cuentaAllowed: boolean;

  /** Handler de los botones de "Nueva" */
  private onNewClick(tipo: 'cuenta' | 'categoria') {
    this.$emit('new', tipo);
  }
}
</script>

<style lang="stylus" scoped></style>
