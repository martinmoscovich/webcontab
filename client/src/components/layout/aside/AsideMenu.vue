<template>
  <aside class="aside is-placed-left " :class="reduced ? 'is-reduced' : 'is-expanded'">
    <!-- Nombre de la app y boton de reduce/expand -->
    <AsideTools
      :class="{ 'pl-2': reduced }"
      :is-main-menu="true"
      :actionIcon="reduceToggleIcon"
      @action="onReduceToggleClick"
    >
      <span v-if="!reduced"> Web<b>Contab</b> </span>
    </AsideTools>

    <!-- Panel con info de la sesion (org y ejercicio) -->
    <SesionPanel v-if="!reduced" />

    <!-- Menu -->
    <div class="menu is-menu-main">
      <template v-for="group in visibleMenu">
        <!-- Titulo del grupo -->
        <p class="menu-label" :class="{ 'pa-0 has-text-centered': reduced }" :key="'g' + group.name">
          <!-- Si esta en modo reducido, se muestran solo las primeras 3 letras del nombre del grupo -->
          {{ reduced ? group.name.substring(0, 3) : group.name }}
        </p>

        <!-- Submenu -->
        <AsideMenuList :key="'i' + group.name" :menu="group.items" :reduced="reduced" @itemClick="onMenuItemClick" />
      </template>
    </div>
  </aside>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import AsideTools from './AsideTools.vue';
import AsideMenuList from './AsideMenuList.vue';
import SesionPanel from './SesionPanel.vue';
import { uiStore } from '@/store';
import { MenuGroup, MenuItem } from '@/core/ui/MenuItem';

/** Sidebar de la aplicacion */
@Component({ components: { AsideTools, AsideMenuList, SesionPanel } })
export default class AsideMenu extends Vue {
  /** Menu jerarquico a mostrar */
  @Prop({ type: Array, default: () => [] })
  menu: MenuGroup[];

  /** Obtiene los items del menu que tienen subitems */
  private get visibleMenu() {
    return this.menu.filter(g => g.items && g.items.length > 0);
  }

  /** Indica si mostrar la version reducida */
  private get reduced() {
    return uiStore.isAsideReduced;
  }

  /** Icono a mostrar en el boton que reduce/expande el menu */
  private get reduceToggleIcon() {
    return this.reduced ? 'forwardburger' : 'backburger';
  }

  /** Handler cuando se hace click en un item del menu */
  private onMenuItemClick(item: MenuItem) {
    this.$emit('itemSelected', item);
  }

  /** Handler cuando se hace click en el boton que reduce/expande el menu */
  private onReduceToggleClick() {
    uiStore.asideReducedToggle();
  }
}
</script>
