<template>
  <nav class="panel">
    <!-- Titulo del panel -->
    <p v-if="$slots.title" class="panel-heading">
      <slot name="title" />
    </p>

    <!-- Contenido cuando no hay items -->
    <div v-if="cuentas.length === 0">
      <p class="subtitle has-text-centered pa-3">
        Aún no hay categorias ni cuentas
      </p>
    </div>

    <!-- Lista de items -->
    <div v-else class="panel-block" v-for="item in cuentas" :key="item.id">
      <!-- Icono -->
      <span class="panel-icon">
        <b-icon :icon="getIcon(item)" custom-size="mdi-24px" size="is-small" />
      </span>
      {{ item.descripcion }} <small class="has-text-grey-light is-size-7 ml-1"> [{{ item.codigo }}]</small>
      <div style="flex-grow: 1" />

      <!-- Boton de Quitar -->
      <b-button
        @click.stop="onQuitar(item)"
        class="ml-1"
        size="is-small"
        type="is-danger"
        icon-right="delete"
      ></b-button>
    </div>
  </nav>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria } from '@/model/Cuenta';

/** Lista de categorias y cuentas */
@Component
export default class SeleccionCuentasList extends Vue {
  @Prop()
  cuentas: CuentaOCategoria[];

  /** Icono a mostrar para el item */
  private getIcon(item: CuentaOCategoria) {
    return item.imputable ? 'file' : 'folder';
  }

  /** Handler cuando se quiere quitar una categoria/cuenta de la lista */
  private onQuitar(item: CuentaOCategoria) {
    this.$emit('eliminar', item);
  }
}
</script>

<style lang="scss"></style>
