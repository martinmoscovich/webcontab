<template>
  <!-- Layout principal una vez logueado -->
  <div class="app" v-if="status.started && !isLogin" :class="{ reduced: asideReduced }">
    <template>
      <!-- Toolbar -->
      <NavBar />

      <!-- Sidebar -->
      <AsideMenu :menu="menu" @itemSelected="onMenuItemSelected" />

      <!-- Contenido de la ruta actual -->
      <router-view />
    </template>
  </div>

  <!-- Si la aplicacion no se inicio o esta en login, mostrar la pantalla inicial -->
  <SplashScreen v-else :status="status" @retry="onRetry" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import NavBar from '@/components/layout/navbar/NavBar.vue';
import AsideMenu from '@/components/layout/aside/AsideMenu.vue';
import SplashScreen from '@/components/layout/SplashScreen.vue';
import { routerService } from '@/service';
import { MenuItem, MenuGroup, MenuParent } from '@/core/ui/MenuItem';
import { sessionStore, start, uiStore } from './store';

/**
 * Punto de entrada de la aplicacion
 */
@Component({ components: { AsideMenu, NavBar, SplashScreen } })
export default class App extends Vue {
  private async created() {
    // Inicializa la aplicacion al cargar
    start();
  }

  /** Handler del boton de reintento */
  private onRetry() {
    start();
  }

  /** Menu del admin */
  adminMenu: MenuGroup[] = [
    {
      name: 'Admin',
      items: [
        {
          name: 'ListaOrganizaciones',
          icon: 'office-building',
          label: 'Organizaciones',
          to: routerService.organizaciones()
        },
        {
          name: 'ListaMonedas',
          icon: 'currency-usd',
          label: 'Monedas',
          to: routerService.monedas()
        },
        {
          name: 'ListaUsuarios',
          icon: 'account-multiple',
          label: 'Usuarios',
          to: routerService.usuarios()
        },
        {
          name: 'AdminAjustes',
          icon: 'cog',
          label: 'Ajustes',
          to: routerService.adminAjustes()
        },
        {
          name: 'Inflacion',
          icon: 'cash-multiple',
          label: 'Inflacion',
          to: routerService.inflacion()
        }
        // {
        //   name: "ListaProvincias",
        //   icon: "ballot",
        //   label: "Provincias",
        //   to: routerService.provincias(),
        // }
      ]
    }
  ];

  /** Menu de organizacion */
  organizacionMenuGroups: MenuGroup[] = [
    {
      name: 'Organizacion',
      items: [
        {
          name: 'Categorias',
          icon: 'ballot',
          to: routerService.categoria(),
          label: 'Cuentas'
        },
        {
          name: 'ListaEjercicios',
          icon: 'calendar',
          label: 'Ejercicios',
          to: routerService.ejercicios(),
          roles: ['ADMIN', 'ORG:ADMIN']
        }
      ]
    }
  ];

  /** Menu de ejercicio */
  ejercicioMenuGroups: MenuGroup[] = [
    {
      name: 'Ejercicio',
      items: [
        {
          name: 'NuevoAsiento',
          icon: 'pencil-plus',
          label: 'Nuevo Asiento',
          to: routerService.nuevoAsiento(),
          onlyEdition: true
        },
        {
          name: 'InformeDiario',
          icon: 'format-list-bulleted',
          label: 'Diario',
          to: routerService.informe('DIARIO')
        },
        {
          name: 'InformeMayor',
          icon: 'file',
          label: 'Mayor',
          to: routerService.informe('MAYOR')
        },
        {
          name: 'InformeBalance',
          icon: 'scale-balance',
          label: 'Balance',
          to: routerService.informe('BALANCE')
        }
      ]
    }
  ];

  /** Menu del usuario */
  usuarioMenuGroups: MenuGroup[] = [
    {
      name: 'Usuario',
      items: [
        {
          name: 'Perfil',
          icon: 'account',
          label: 'Perfil',
          to: routerService.perfil()
        },
        {
          name: 'Salir',
          icon: 'logout',
          label: 'Salir'
        }
      ]
    }
  ];

  /**
   * Handler de cuando se selecciona una opcion del menu.
   * Solo se incluyen los que requieren una logica especial
   */
  private onMenuItemSelected(item: MenuItem) {
    if (item.name === 'Salir') sessionStore.logout();
  }

  /** Items del menu filtrados segun el contexto */
  private get menuGroups() {
    const menu: MenuGroup[] = [];
    if (sessionStore.enEjercicio) menu.push(...this.ejercicioMenuGroups);
    if (sessionStore.enOrganizacion) menu.push(...this.organizacionMenuGroups);
    if (sessionStore.hasRole('ADMIN')) menu.push(...this.adminMenu);
    menu.push(...this.usuarioMenuGroups);

    return menu;
  }

  /**
   * Items del menu, filtrados segun si es modo readonly o no (por ej cuando el ejercicio esta finalizado)
   */
  private get menu() {
    // Si no es readonly, no se filtra
    if (!sessionStore.asientosReadonly) return this.menuGroups;

    // Si es readonly, filtrar los que no lo sean
    return this.menuGroups.map(group => {
      return {
        name: group.name,
        items: this.getReadonlyMenuItems(group)
      };
    });
  }

  /** Obtiene los items del menu read only, recursivamente */
  private getReadonlyMenuItems(parent: MenuParent): MenuItem[] | undefined {
    if (!parent.items) return undefined;

    return parent.items.filter(i => !i.onlyEdition).map(i => ({ ...i, items: this.getReadonlyMenuItems(i) }));
  }

  /** Status de la sesion */
  private get status() {
    return { ...sessionStore.status, started: sessionStore.appStarted };
  }

  /** Indica si la ruta actual es la de login */
  private get isLogin() {
    return this.$route.name === routerService.login().name;
  }

  /** Indica si se esta usando el menu reducido */
  get asideReduced() {
    return uiStore.isAsideReduced;
  }
}
</script>
