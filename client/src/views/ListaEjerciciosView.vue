<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Ejercicios" cuentaSearch />

    <!-- Lista -->
    <div class="columns is-centered">
      <div class="column is-two-thirds-tablet is-half-widescreen">
        <ListaEjerciciosContainer :organizacion="organizacion" :readonly="readonly" />
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ListaEjerciciosContainer from '@/components/organizacion/ListaEjerciciosContainer.vue';
import { Organizacion } from '../model/Organizacion';
import { sessionStore } from '../store';

/** Pagina que muestra los ejercicios de una organizacion */
@Component({ components: { ListaEjerciciosContainer } })
export default class ListaEjerciciosView extends Vue {
  /** Indica si el usuario solo tiene rol de lectura (no puede modificar) */
  private get readonly() {
    return sessionStore.readonly;
  }

  private get organizacion(): Organizacion | null {
    return sessionStore.organizacion;
  }
}
</script>

<style lang="scss" scoped></style>
