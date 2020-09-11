<template>
  <portal to="navbar-items" target-class="navbar-item">
    <!-- Busqueda de Categorias y Cuentas -->
    <div v-if="cuentaSearch && enOrganizacion" class="navbar-item navbar-all">
      <CuentaSearch
        modo="todos"
        clearOnSelect
        placeholder="Buscar Categoria o Cuenta"
        @input="onSearchSelect"
        style="width: 100%"
      />
    </div>

    <!-- Titulo -->
    <NavBarTitle v-if="title">{{ title }}</NavBarTitle>

    <!-- Contenido custom -->
    <slot />

    <portal to="navbar-items-mobile" target-class="navbar-item">
      <slot />
    </portal>
  </portal>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria } from '@/model/Cuenta';
import { routerService } from '@/service';
import { sessionStore } from '@/store';

/**
 * Toolbar de la aplicacion.
 * Utiliza el NavBar y le da funcionalidades de Toolbar general.
 */
@Component
export default class PageNavBar extends Vue {
  /**
   * Titulo del toolbar (ultimo item del breadcrumb).
   * Opcional, se puede indicar cuando el titulo es un simple texto.
   * Si se necesita un componente mas complejo, usar NavBarTitle
   */
  @Prop({ required: false })
  title: string;

  /** Indica si se debe incluir el autocomplete de cuentas, que permite ir al mayor rapidamente */
  @Prop({ type: Boolean })
  cuentaSearch: boolean;

  /** Indica si la sesion actual tiene organizacion seleccionada */
  private get enOrganizacion() {
    return sessionStore.enOrganizacion;
  }

  /** Handler cuando se selecciona una cuenta o categoria */
  private onSearchSelect(item: CuentaOCategoria | null) {
    if (!item) return;

    if (item.imputable) {
      routerService.goToCuenta(item);
    } else {
      routerService.goToCategoria(item);
    }
  }
}
</script>

<style lang="scss" scoped>
.autocomplete {
  border: 1px solid #dbdbdb;
  border-radius: 4px;
}
</style>
