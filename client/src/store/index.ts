import { monedaApi, provinciaApi } from '@/api';
import Vue from 'vue';
import Vuex from 'vuex';
import { AsientoStore } from './AsientoStore';
import { CuentaStore } from './CuentaStore';
import { SessionStore } from './SessionStore';
import { SimpleStore } from './SimpleStore';
import { UIStore } from './UIStore';
import { OrganizacionStore } from '@/store/OrganizacionStore';
import { UpdateStore } from '@/store/UpdateStore';
import { UserStore } from '@/store/UserStore';

Vue.use(Vuex);

export const store = new Vuex.Store({
  state: {},
  mutations: {},
  actions: {},
  modules: {}
});

// Se instancias los modulos
export const uiStore = new UIStore({ store, name: 'ui' });
export const sessionStore = new SessionStore({ store, name: 'session' });
export const cuentaStore = new CuentaStore({ store, name: 'cuenta' });
export const asientoStore = new AsientoStore({ store, name: 'asiento' });
export const updateStore = new UpdateStore({ store, name: 'update' });
export const provinciaStore = new SimpleStore(
  { api: provinciaApi, entidad: 'provincia', entidades: 'provincias' },
  { store, name: 'provincia' }
);
export const monedaStore = new SimpleStore(
  { api: monedaApi, entidad: 'moneda', entidades: 'monedas' },
  { store, name: 'moneda' }
);
export const organizacionStore = new OrganizacionStore({ store, name: 'organizacion' });
export const userStore = new UserStore({ store, name: 'usuario' });

/**
 * Inicializa el store
 */
export async function start() {
  return sessionStore.start();
}

/**
 * Inicializa el store una vez autenticado
 */
export function initAuthenticated() {
  return Promise.all([monedaStore.list({ refresh: true }), provinciaStore.list({ refresh: true })]);
}

/**
 * Vuelve el store a su estado incial
 * @param resetOrg
 */
export async function resetStore(resetOrg: boolean) {
  if (resetOrg) {
    organizacionStore.reset();
    cuentaStore.reset();
  }
  asientoStore.reset();
}

// LOGICA DE RESIZE

/** Timer utilizado para hacer Debounce */
let timer: number | null = null;
window.addEventListener('resize', () => {
  // Si existe el timer, se cancela
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }

  // Se genera un nuevo timer con el tiempo indicado
  timer = setTimeout(() => uiStore.onResize(), 200);
});

// Cuando se pasa a modo tablet (768 - 1024) se pone el menu reducido
uiStore.$watch(
  theModule => theModule.isTablet,
  newValue => uiStore.asideReducedToggle(newValue)
);

// Cuando se pasa a modo mobile (< 768) se quita el menu
uiStore.$watch(
  theModule => theModule.isMobile,
  () => uiStore.asideMobileStateToggle(false)
);
