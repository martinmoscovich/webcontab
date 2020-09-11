<template>
  <section class="hero is-fullheight is-medium is-primary is-bold full-screen" style="background: transparent">
    <div class="hero-body">
      <div class="container">
        <div class="columns is-centered">
          <!-- Registro del primer usuario cuando no existe -->
          <FirstUse v-if="firstUse" />

          <!-- Wizard de Login (auth, org y ejercicio) -->
          <article v-else class="card is-rounded">
            <div class="card-content">
              <LoginWizard />
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import { sessionStore } from '../store';

import LoginWizard from '@/components/login/LoginWizard.vue';
import FirstUse from '@/components/login/FirstUse.vue';

/**
 * Pagina de Login.
 * Incluye autenticacion, seleccion de organizacion y ejercicio.
 * Tambien maneja el registro del primer usuario.
 *
 * Esta pantalla no muestra Navbar ni Menu
 */
@Component({
  components: { FirstUse, LoginWizard }
})
export default class LoginView extends Vue {
  /** Indica que es el primer uso, hay que registrar al admin */
  private get firstUse() {
    return sessionStore.firstUse;
  }
}
</script>
