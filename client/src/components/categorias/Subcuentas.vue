<template>
  <nav class="panel" v-if="categoria.children">
    <a class="panel-block" v-for="item in items" :key="item.id" @click="onItemClick(item)">
      <span class="panel-icon">
        <b-icon :icon="getIcon(item)" custom-size="mdi-24px" size="is-small" />
      </span>
      {{ item.numero }}. {{ item.descripcion }}
    </a>
  </nav>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Cuenta, Categoria, CuentaOCategoria } from '@/model/Cuenta';
import { buildCompareFn } from '@/utils/array';

/**
 * Lista de subcategorias / subcuentas de una Categoria
 */
@Component
export default class Subcuentas extends Vue {
  @Prop()
  categoria: Categoria;

  /** Icono a mostrar en el item */
  private getIcon(item: Cuenta | Categoria) {
    return item.imputable ? 'file' : 'folder';
  }

  /** Subcuentas ordenadas por numero */
  private get items() {
    return this.categoria.children?.sort(buildCompareFn({ field: 'numero' })) ?? null;
  }

  /** Handler cuando se selecciona un hijo */
  private onItemClick(item: CuentaOCategoria) {
    if (item.imputable) {
      this.$emit('cuentaSelected', item);
    } else {
      this.$emit('categoriaSelected', item);
    }
  }
}
</script>

<style lang="scss"></style>
