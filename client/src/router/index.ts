import Vue from 'vue';
import VueRouter from 'vue-router';
import routes from './routes';
import { secureRouter } from './secure';
import { uiStore } from '@/store';

Vue.use(VueRouter);

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
});

// Agrega los interceptores necesarios para seguridad
secureRouter(router);

// Listener para quitar el menu en mobile luego de cambiar de ruta
router.afterEach(() => {
  uiStore.asideMobileStateToggle(false);
});

export default router;
