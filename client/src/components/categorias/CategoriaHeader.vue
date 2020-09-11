<template>
  <div class="level">
    <div class="level-left">
      <CategoriasBreadcrumb
        :cuenta="item"
        :readonly="readonly"
        @edit="onDescripcionChange"
        @categoriaSelected="onCategoriaSelected"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { CuentaOCategoria, Categoria } from '../../model/Cuenta';
import CategoriasBreadcrumb from './CategoriasBreadcrumb.vue';
import { cuentaStore, sessionStore } from '@/store';
import { routerService } from '@/service';

/**
 * Header de Categoria o Cuenta.
 *
 * Tiene el breadcrumb y permite editar el nombre de la categoria/cuenta actual.
 */
@Component({ components: { CategoriasBreadcrumb } })
export default class CategoriaHeader extends Vue {
  /** Categoria o cuenta actual */
  @Prop()
  item: CuentaOCategoria;

  /** Indica si el usuario solo tiene rol de lectura (no puede modificar) */
  private get readonly() {
    return sessionStore.readonly;
  }

  /** Handler cuando se modifica la descripcion. Se guarda en el server */
  private onDescripcionChange(value: string) {
    if (!this.item || this.item.descripcion === value) return;

    cuentaStore.actualizar({
      ...this.item,
      descripcion: value
    } as CuentaOCategoria);
  }

  /** Handler cuando se hace click en una categoria del breadcrumb */
  private onCategoriaSelected(cat: Categoria) {
    routerService.goToCategoria(cat);
  }
}
</script>
