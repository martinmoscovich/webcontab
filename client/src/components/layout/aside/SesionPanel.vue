<template>
  <div>
    <!-- Organizacion -->
    <div class="aside-tools ejercicio organizacion has-text-grey-lighter">
      <template v-if="organizacion">
        <b-icon icon="office-building" custom-size="mdi-18px" size="is-small" />
        <div class="nombre-org">
          {{ organizacion ? organizacion.nombre : '' }}
        </div>
      </template>
      <div v-else>Administrador</div>
    </div>

    <!-- Ejercicio -->
    <AsideTools v-if="ejercicio" class="ejercicio fechas has-text-grey-lighter">
      <b-icon icon="calendar" custom-size="mdi-18px" size="is-small" />
      <small>{{ inicio }} - {{ finalizacion }}</small>
    </AsideTools>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import AsideTools from '@/components/layout/aside/AsideTools.vue';
import { prettyFormatDate } from '@/utils/date';
import { sessionStore } from '@/store';

/**
 * Panel que muestra informacion de la sesion (organizacion y ejercicio)
 */
@Component({ components: { AsideTools } })
export default class SesionPanel extends Vue {
  /** Organizacion actual */
  private get organizacion() {
    return sessionStore.organizacion;
  }

  /** Ejercicio actual */
  private get ejercicio() {
    return sessionStore.ejercicio;
  }

  /** Fecha formateada del inicio del ejercicio */
  private get inicio() {
    if (!this.ejercicio) return '';
    return prettyFormatDate(this.ejercicio.inicio, { shortMonth: true });
  }

  /** Fecha formateada del final del ejercicio */
  private get finalizacion() {
    if (!this.ejercicio) return '';
    return prettyFormatDate(this.ejercicio.finalizacion, { shortMonth: true });
  }
}
</script>
<style lang="scss" scoped>
.nombre-org {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.ejercicio {
  background-color: #2a2a2a !important;

  &.organizacion {
    display: flex;
    width: 100%;
    align-items: center;
    line-height: 2rem;
    height: 2rem;
  }
  &.fechas {
    line-height: 2rem;
    height: 2rem;
  }
  .btn-change {
    color: white;
  }
}
</style>
