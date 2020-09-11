<template>
  <nav v-show="isNavBarVisible" id="navbar-main" class="navbar is-fixed-top">
    <div class="navbar-brand">
      <!-- Boton para quitar o mostrar el menu -->
      <a class="navbar-item is-hidden-tablet" @click.prevent="menuToggleMobile">
        <b-icon :icon="menuToggleMobileIcon" />
      </a>

      <!-- Backbutton (opcional) y Breadcrumb (titulo) -->
      <div class="navbar-item navbar-all-mobile">
        <portal-target name="navbar-back-button" slim />
        <NavBarBreadcrumb :title-stack="path" />
      </div>

      <portal-target v-if="!isDesktop" name="navbar-items-mobile" multiple />
    </div>
    <div class="navbar-menu fadeIn animated faster" :class="{ 'is-active': isMenuNavBarActive }">
      <!-- Items (search, botones, etc) de la toolbar especificados usando portales -->
      <portal-target name="navbar-items" class="navbar-start navbar-all" multiple></portal-target>

      <!-- Alineado a la derecha -->
      <div class="navbar-end">
        <!-- Usuario -->
        <NavBarUserMenu />

        <!-- Boton Ayuda -->
        <a
          href="https://justboil.me/bulma-admin-template/one"
          class="navbar-item has-divider is-desktop-icon-only"
          title="Ayuda"
        >
          <b-icon icon="help-circle-outline" custom-size="default" />
          <span>Ayuda</span>
        </a>
      </div>
    </div>
  </nav>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import NavBarBreadcrumb from './NavBarBreadcrumb.vue';
import NavBarUserMenu from './NavBarUserMenu.vue';
import { uiStore } from '@/store';

/**
 * Toolbar de la aplicacion
 */
@Component({
  components: { NavBarUserMenu, NavBarBreadcrumb }
})
export default class NavBar extends Vue {
  private isMenuNavBarActive = false;

  private get menuNavBarToggleIcon() {
    return this.isMenuNavBarActive ? 'close' : 'dots-vertical';
  }
  private get menuToggleMobileIcon() {
    return this.isAsideMobileExpanded ? 'backburger' : 'forwardburger';
  }

  private get isNavBarVisible() {
    return uiStore.isNavBarVisible;
  }
  private get isAsideMobileExpanded() {
    return uiStore.isAsideMobileExpanded;
  }

  /** Path a mostrar como titulo de la Toolbar en el breadcrumb */
  private get path() {
    return this.$route.meta.path;
  }

  private menuToggleMobile() {
    uiStore.asideMobileStateToggle();
  }

  private menuNavBarToggle() {
    this.isMenuNavBarActive = !this.isMenuNavBarActive;
  }

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }
  /** Indica si es ancho desktop */
  private get isDesktop() {
    return uiStore.isDesktop;
  }
}
</script>
<style lang="scss" scoped>
.vue-portal-target {
  padding: 0 !important;
  margin: 0;
}
</style>
<style lang="scss">
.navbar-all {
  flex-grow: 1;
}
</style>
