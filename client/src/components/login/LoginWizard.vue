<template>
  <b-steps :value="step" :has-navigation="false" @input="onNavClick">
    <!-- Paso Autenticacion -->
    <b-step-item label="Usuario" icon="account"> <LoginForm :loading="loading" @submit="onLoginSubmit"/></b-step-item>

    <!-- Paso Organizacion o Admin -->
    <b-step-item label="Org." icon="office-building">
      <!-- Boton Modo Admin -->
      <b-button v-if="isAdmin" type="is-info" @click="onAdminClick" class="mb-2">Administrador</b-button>

      <!-- Lista Organizaciones -->
      <OrganizacionesList v-if="step === 1" @selected="onOrgSelected" />
    </b-step-item>

    <!-- Paso Ejercicio -->
    <b-step-item label="Ejercicio" icon="calendar">
      <EjerciciosStep v-if="organizacion" :organizacion="organizacion" @selected="onEjercicioSelected" />
    </b-step-item>
  </b-steps>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import LoginForm from './LoginForm.vue';
import OrganizacionesList from './OrganizacionesList.vue';
import EjerciciosStep from './EjerciciosStep.vue';
import { sessionStore, resetStore } from '@/store';
import { isNotAuthenticated } from '@/core/ajax/error';
import { notificationService, routerService } from '@/service';
import { Organizacion } from '@/model/Organizacion';
import { Ejercicio } from '@/model/Ejercicio';

/**
 * Wizard de Login.
 *
 * Incluye 3 Pasos:
 * - Autenticacion
 * - Seleccion de Organizacion (o modo admin)
 * - Seleccion de Ejercicio
 */
@Component({ components: { LoginForm, OrganizacionesList, EjerciciosStep } })
export default class LoginWizard extends Vue {
  /** Paso actual */
  private step = 0;

  private mounted() {
    this.checkStep();

    // Se limpian los stores, ya que se va a cambiar de organizacion o ejercicio
    // Si el paso es usuario u organizacion, se limpian las organizaciones tambien
    resetStore(this.step <= 1);
  }

  /** Indica que esta cargando */
  private get loading() {
    return sessionStore.status.loading;
  }

  /** Indica que el usuario es Admin general */
  private get isAdmin() {
    return sessionStore.user && sessionStore.hasRole('ADMIN');
  }

  /** Obtiene la organizacion actual, si se selecciono una */
  private get organizacion() {
    return sessionStore.organizacion;
  }

  /** Indica que el usuario esta autenticado */
  private get authenticated() {
    return sessionStore.authenticated;
  }

  private get enEjercicio() {
    return sessionStore.enEjercicio;
  }

  private get asientosEditables() {
    return this.enEjercicio && !sessionStore.asientosReadonly;
  }

  private get enOrganizacion() {
    return sessionStore.enOrganizacion;
  }

  /** Determina el paso actual segun el estado de la sesion */
  private checkStep() {
    // Si se selecciono un ejercicio, se termino el login
    // Se redirige a donde se indico o al plan de cuentas
    if (this.enEjercicio) {
      if (this.$route.query.redirect) {
        this.$router.replace(this.$route.query.redirect as string);
      } else {
        if (this.asientosEditables) {
          routerService.goToNuevoAsiento();
        } else {
          routerService.goToCategoria();
        }
      }
      this.step = 3;
    } else if (this.enOrganizacion) {
      // Si se selecciono organizacion, se muestra el paso de seleccion de ejercicio
      this.step = 2;
    } else if (this.authenticated) {
      // Si se autentico, se muestra el paso de seleccion de organizacion
      this.step = 1;
    } else {
      // Se muestra el form de autenticacion
      this.step = 0;
    }
  }

  /** Handler cuando el usuario se loguea */
  @Watch('authenticated')
  private onAuthenticated() {
    this.checkStep();
  }

  /** Handler cuando el usuario selecciona organizacion */
  @Watch('enOrganizacion')
  onOrgChange() {
    this.checkStep();
  }

  /** Handler cuando el usuario selecciona ejercicio */
  @Watch('enEjercicio')
  onFinish() {
    this.checkStep();
  }

  /** Handler de click en un paso del wizard */
  private onNavClick(value: number) {
    // No se puede ir para adelante
    if (value < this.step) this.step = value;
  }

  /** Handler cuando se autentica */
  private async onLoginSubmit(creds: { username: string; password: string }) {
    try {
      await sessionStore.login(creds);
    } catch (e) {
      if (isNotAuthenticated(e)) {
        notificationService.error('Usuario o password inválido');
      }
    }
  }

  /** Handler cuando selecciona una organizacion */
  private onOrgSelected(org: Organizacion) {
    sessionStore.seleccionarOrganizacion(org);
  }

  /** Handler cuando hace click en modo Administrador */
  private onAdminClick() {
    routerService.goToOrganizaciones();
  }

  /** Handler cuando selecciona un ejercicio */
  private async onEjercicioSelected(ej: Ejercicio) {
    if (ej) {
      sessionStore.seleccionarEjercicio(ej);
    } else {
      if (this.enEjercicio) await sessionStore.salirDeEjercicio();
      routerService.goToCategoria();
    }
  }
}
</script>

<style lang="stylus" scoped></style>
