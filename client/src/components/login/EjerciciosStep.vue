<template>
  <div>
    <!-- Boton "Entrar sin ejercicio" -->
    <b-button type="is-primary" @click="onSinEjercicioClick" style="margin-bottom: 10px"
      >Ingresar sin ejercicio</b-button
    >

    <!-- Lista de ejercicios -->
    <EjerciciosList :ejercicios="ejercicios" @selected="onItemClick" />

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </div>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Organizacion } from '@/model/Organizacion';
import EjerciciosList from '@/components/organizacion/EjerciciosList.vue';
import { Ejercicio } from '@/model/Ejercicio';
import { organizacionStore } from '../../store';
import { buildCompareFn } from '../../utils/array';

/**
 * Seleccion de ejercicio para la sesion.
 * Incluye un modal para crear un ejercicio.
 */
@Component({ components: { EjerciciosList } })
export default class EjerciciosStep extends Vue {
  @Prop()
  organizacion: Organizacion;

  private mounted() {
    this.load();
  }

  /** Handler cuando cambia la organizacion actual */
  @Watch('organizacion')
  private onOrganizacionChange() {
    this.load();
  }

  /** Carga los ejercicios de la organizacion */
  private load() {
    if (this.organizacion) organizacionStore.findEjercicios(this.organizacion);
  }

  /** Obtiene los ejercicios cargados en la cache */
  private get ejercicios() {
    // Si no hay organizacion, no mostrar nada
    if (!this.organizacion) return [];

    // Obtener los ejercicios
    return (
      organizacionStore
        .findEjerciciosLocal(this.organizacion)
        .sort(buildCompareFn({ field: 'id', descending: true })) ?? []
    );
  }

  /** Indica si se estan cargando los ejercicios */
  private get loading() {
    return organizacionStore.lista.status.loading;
  }

  /** Handler cuando se selecciona un ejercicio */
  private onItemClick(item: Ejercicio) {
    this.$emit('selected', item);
  }

  /** Handler cuando se hace click en "Entrar sin ejercicio" */
  private onSinEjercicioClick() {
    this.$emit('selected', null);
  }
}
</script>

<style lang="scss">
.active {
  background: #36ffab;
}
</style>
