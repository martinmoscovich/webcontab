<template>
  <NavBarMenu class="has-divider has-user-avatar">
    <!-- Avatar -->
    <UserAvatar />

    <!-- Nombre -->
    <div class="is-user-name">
      <span>{{ userName }}</span>
    </div>

    <!-- Menu del usuario -->
    <div slot="dropdown" class="navbar-dropdown">
      <!-- Ir a Perfil -->
      <router-link class="navbar-item" :to="{ name: 'Perfil' }">
        <b-icon icon="account" custom-size="default"></b-icon>
        <span>Perfil</span>
      </router-link>

      <!-- Salir del ejercicio -->
      <a class="navbar-item" v-if="enEjercicio" @click="onEjercicioClick">
        <b-icon icon="calendar" custom-size="default"></b-icon>
        <span>Cambiar Ejericicio</span>
      </a>

      <!-- Salir de la organizacion -->
      <a class="navbar-item" @click="onOrganizacionClick">
        <b-icon icon="office-building" custom-size="default"></b-icon>
        <span>Cambiar Organzacion</span>
      </a>
      <hr class="navbar-divider" />

      <!-- Logout -->
      <a class="navbar-item" @click="onLogoutClick">
        <b-icon icon="logout" custom-size="default"></b-icon>
        <span>Log Out</span>
      </a>
    </div>
  </NavBarMenu>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import UserAvatar from '@/components/dashboard/UserAvatar.vue';
import NavBarMenu from './NavBarMenu.vue';
import { sessionStore } from '@/store';

/**
 * Menu del usuario en la Toolbar.
 */
@Component({ components: { NavBarMenu, UserAvatar } })
export default class NavBarUserMenu extends Vue {
  /** Nombre del usuario */
  private get userName() {
    return sessionStore.user?.username;
  }

  /** Indica si la sesion actual tiene ejercicio asignado */
  private get enEjercicio(): boolean {
    return sessionStore.enEjercicio;
  }

  /** Handler cuando se desea salir de la organizacion actual */
  private onOrganizacionClick() {
    sessionStore.salirDeOrganizacion();
  }

  /** Handler cuando se desea salir del ejercicio actual */
  private onEjercicioClick() {
    sessionStore.salirDeEjercicio();
  }

  /** Handler cuando se hace click en "logout" */
  private onLogoutClick() {
    sessionStore.logout();
  }
}
</script>

<style lang="stylus" scoped></style>
