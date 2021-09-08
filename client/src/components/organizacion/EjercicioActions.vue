<template>
  <b-dropdown position="is-bottom-left" :mobile-modal="false" aria-role="list">
    <!-- Boton que abre el menu -->
    <a class="navbar-item" slot="trigger" role="button">
      <b-icon icon="dots-vertical" slot="trigger"></b-icon>
    </a>

    <!-- Acciones disponibles en un ejercicio abierto -->
    <template v-if="!finalizado">
      <b-dropdown-item @click="$emit('inflacion')" aria-role="listitem">
        <b-icon icon="cash-multiple" custom-size="mdi-18px" />
        <span class="menuitem-text">Inflación</span>
      </b-dropdown-item>
      <b-dropdown-item @click="$emit('renumerar')" aria-role="listitem">
        <b-icon icon="order-numeric-ascending" custom-size="mdi-18px" />
        <span class="menuitem-text">Renumerar</span>
      </b-dropdown-item>
      <b-dropdown-item v-if="tieneApertura" @click="$emit('recalcular')" aria-role="listitem">
        <b-icon icon="calculator" custom-size="mdi-18px" />
        <span class="menuitem-text">Recalcular apertura</span>
      </b-dropdown-item>
      <b-dropdown-item @click="$emit('cerrar')" aria-role="listitem">
        <b-icon icon="lock" custom-size="mdi-18px" />
        <span class="menuitem-text">Cerrar</span>
      </b-dropdown-item>
    </template>

    <!-- Acciones disponibles en un ejercicio finalizado -->
    <b-dropdown-item v-else @click="$emit('reabrir')" aria-role="listitem">
      <b-icon icon="lock-open-variant" custom-size="mdi-18px" />
      <span class="menuitem-text">Reabrir</span>
    </b-dropdown-item>

    <hr class="dropdown-divider ml-1 mr-1" aria-role="listitem" />

    <!-- Eliminar ejercicio (siempre disponible) -->
    <b-dropdown-item @click="$emit('eliminar')" aria-role="listitem">
      <b-icon icon="delete" custom-size="mdi-18px" class="has-text-danger" />
      <span class="menuitem-text">Borrar</span>
    </b-dropdown-item>
  </b-dropdown>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';

/**
 * Boton con menu de acciones para un ejercicio
 */
@Component
export default class EjercicioActions extends Vue {
  /** Indica si el ejercicio esta finalizado o no */
  @Prop({ type: Boolean })
  finalizado: boolean;

  /** Indica si el ejercicio es el primero */
  @Prop({ type: Boolean })
  tieneApertura: boolean;
}
</script>

<style lang="stylus" scoped></style>
