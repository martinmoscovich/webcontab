<template>
  <section class="hero is-fullheight is-medium is-primary is-bold full-screen">
    <div class="home-columns columns is-vcentered is-centered">
      <!-- Titulo de la aplicacion (al costado izquierdo) -->
      <div class="column is-one-quarter is-home-left-column">
        <div class="container" style="margin-left: 5rem">
          <h1 class="title"><span class="has-text-weight-light">Web</span>Contab</h1>
          <h2 class="subtitle">
            Sistema Contable
          </h2>
        </div>
      </div>

      <!-- Contenido del Splash -->
      <div class="column is-home-right-column has-text-centered">
        <!-- Login -->
        <router-view v-if="status.started" />

        <!-- Loader de la aplicacion -->
        <div v-else-if="status.loading" class="content">
          <div class="wc-is-loading big" style="margin: 0 auto"></div>
          <h1 class="title">
            Cargando
          </h1>
        </div>

        <!-- Error al iniciar -->
        <div v-else-if="status.error" class="content">
          <h2 class="subtitle">
            Error al iniciar
          </h2>
          <b-button type="is-info" size="is-medium" icon-left="refresh" @click="onRetry">Reintentar</b-button>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import LoginForm from '@/components/login/LoginForm.vue';
import EjerciciosStep from '@/components/login/EjerciciosStep.vue';
import { LoadingStatus } from '../../core/ui/loading';

/**
 * Pantalla a mostrar en la carga inicial y en el login.
 * Es fullscreen, no muestra ni menu ni toolbar
 */
@Component({
  components: { LoginForm, EjerciciosStep }
})
export default class SplashScreen extends Vue {
  /** Status de la aplicacion */
  @Prop()
  status: LoadingStatus & { started: boolean };

  /** Handler cuando se clickea el boton de Reintentar */
  private onRetry() {
    this.$emit('retry');
  }
}
</script>
<style lang="scss" scoped>
.home-columns {
  margin: 0;
  display: flex;
  min-height: 100vh;
  margin: 10px;
}
.is-home-left-column,
.is-home-right-column {
  padding: 1.5rem 2rem 3rem 0.75rem;
}
</style>
