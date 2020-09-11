<template>
  <ul :class="{ 'menu-list': !submenu }">
    <AsideMenuItem
      v-for="(item, index) in menu"
      :item="item"
      :key="index"
      :reduced="reduced"
      @itemClick="onMenuItemClick"
    />
  </ul>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import AsideMenuItem from './AsideMenuItem.vue';
import { MenuItem } from '@/core/ui/MenuItem';

/** Lista de Items de un grupo del menu */
@Component({ components: { AsideMenuItem } })
export default class AsideMenuList extends Vue {
  /** Items del menu */
  @Prop({ type: Array, default: () => [] })
  menu: MenuItem[];

  /** Indica si es un menu raiz o un submenu */
  @Prop({ type: Boolean })
  submenu: boolean;

  /** Indica que el Sidebar esta en modo reducido */
  @Prop({ type: Boolean })
  reduced: boolean;

  /** Handler cuando se hace click en un item */
  private onMenuItemClick(item: MenuItem) {
    this.$emit('itemClick', item);
  }
}
</script>
