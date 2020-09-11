<template>
  <li :class="{ 'is-active': isDropdownActive }">
    <router-link
      :is="componentIs"
      v-bind="links"
      @click="onItemClick"
      exact-active-class="is-active"
      :class="{
        'has-icon': !!item.icon,
        'has-dropdown-icon': hasDropdown,
        'has-text-centered': reduced
      }"
    >
      <!-- Si el menu esta reducido, mostrar solo el icono y un tooltip (sin texto) -->
      <b-tooltip v-if="reduced" position="is-right" :label="item.label">
        <b-icon
          v-if="item.icon"
          :icon="item.icon"
          :class="{ 'has-update-mark': item.updateMark }"
          custom-size="default"
        />
      </b-tooltip>

      <!-- Si esta expandido, mostrar el icono y el texto -->
      <template v-else>
        <b-icon
          v-if="item.icon"
          :icon="item.icon"
          :class="{ 'has-update-mark': item.updateMark }"
          custom-size="default"
        />
        <span v-if="!reduced && item.label" :class="{ 'menu-item-label': !!item.icon }">{{ item.label }}</span>
      </template>

      <!-- Si tiene subitems, mostrar el icono de dropdown -->
      <div v-if="hasDropdown" class="dropdown-icon">
        <b-icon :icon="dropdownIcon" custom-size="default" />
      </div>
    </router-link>

    <!-- Si tiene subitems, mostrar submenu -->
    <AsideMenuList v-if="hasDropdown" submenu :menu="item.items" :reduced="reduced" />
  </li>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import AsideMenuList from './AsideMenuList.vue';
import { MenuItem } from '@/core/ui/MenuItem';

/**
 * Item del menu.
 * Puede a su vez tener subitems.
 */
@Component({ components: { AsideMenuList } })
export default class AsideMenuItem extends Vue {
  @Prop()
  item: MenuItem;

  @Prop({ type: Boolean })
  reduced: boolean;

  /** Indica si se esta mostrando el submenu */
  private isDropdownActive = false;

  /** Obtiene el link correcto a mostrar para el item */
  private get links() {
    // Se prioriza la prop "itemTo", luego el href
    return this.item.to ? { to: this.item.to } : this.item.href ? { href: this.item.href } : {};
  }

  /** Determina si mostrar un link comun o uno del router */
  private get componentIs() {
    return this.item.to ? 'router-link' : 'a';
  }

  /** Indica si se debe mostrar un submenu */
  private get hasDropdown() {
    return !!this.item.items;
  }

  /** Determina el icono del dropdown segun si esta desplegado o no */
  private get dropdownIcon() {
    return this.isDropdownActive ? 'minus' : 'plus';
  }

  /** Handler cuando se hace click en el item */
  private onItemClick() {
    this.$emit('itemClick', this.item);

    if (this.hasDropdown) {
      this.isDropdownActive = !this.isDropdownActive;
    }
  }
}
</script>
