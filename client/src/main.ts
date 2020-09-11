/* Styles */
import '@/styles/main.scss';
// import "@mdi/font/css/materialdesignicons.css";

/* Core */
import Vue from 'vue';

/* Vue. Main component */
import App from './App.vue';

// Registra plugins y componentes globales
import './global';

/* Router & Store */
import router from './router';
import { store } from './store';

import Component from 'vue-class-component';

// Para poder usar hooks del router dentro de los componentes
Component.registerHooks([
  // "beforeRouteEnter",
  'beforeRouteLeave',
  'beforeRouteUpdate'
]);

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
