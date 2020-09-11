<template>
  <NavBarItem :dense="true">
    <b-tooltip label="Exportar a Excel" position="is-bottom" type="is-info">
      <a :href="url"><b-icon icon="microsoft-excel" class="mr-1 ml-1"/></a>
    </b-tooltip>
  </NavBarItem>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';

/**
 * Boton de la toolbar para exportar a Excel
 */
@Component({ components: {} })
export default class ToolbarExcelButton extends Vue {
  /** URL donde se descarga el reporte deseado */
  @Prop()
  url: string;
}
</script>

<style lang="stylus" scoped></style>
