<template>
  <CardEjercicios
    :ejercicios="ejercicios"
    :loading="loading"
    :readonly="readonly"
    @save="onNewEjercicio"
    @renumerar="onRenumerar"
    @inflacion="onInflacion"
    @cerrar="onCerrar"
    @reabrir="onReabrir"
    @recalcular="onRecalcularApertura"
    @eliminar="onEliminar"
    @selected="onSelected"
  />
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator';
import CardEjercicios from '@/components/organizacion/CardEjercicios.vue';
import { Ejercicio } from '@/model/Ejercicio';
import { organizacionStore, sessionStore } from '@/store';
import { Organizacion } from '@/model/Organizacion';

/**
 * Componente inteligente que maneja el panel de ejercicios de una organizacion, interactuando con el Store.
 */
@Component({ components: { CardEjercicios } })
export default class ListaEjerciciosContainer extends Vue {
  /** Organizacion seleccionada */
  @Prop()
  organizacion: Organizacion;

  /** Indica que no se pueden modificar los ejercicios */
  @Prop({ type: Boolean })
  readonly: boolean;

  mounted() {
    this.onOrganizacionChange();
  }

  /**
   * Handler cuando cambia la organizacion seleccionada.
   * Se buscan los ejercicios.
   */
  @Watch('organizacion')
  private onOrganizacionChange() {
    if (!this.organizacion) return;
    organizacionStore.findEjercicios(this.organizacion);
  }

  /** Se obtienen los ejercicios cacheados para la organizacion */
  private get ejercicios() {
    if (!this.organizacion) return [];
    return organizacionStore.findEjerciciosLocal(this.organizacion);
  }

  /** Indica que se estan cargando los ejercicios */
  private get loading() {
    return organizacionStore.ejercicios.status.loading;
  }

  /** Handler cuando se crea un nuevo ejercicio */
  private onNewEjercicio(ej: Ejercicio & { cerrarAnterior: boolean }) {
    if (!this.organizacion) return;
    organizacionStore.crearEjercicio({
      organizacion: this.organizacion,
      periodo: { desde: ej.inicio, hasta: ej.finalizacion },
      cerrarUltimo: ej.cerrarAnterior
    });
  }

  /**
   * Handler cuando se desea ingresar a un ejercicio.
   * Primero se sale del actual.
   */
  private onSelected(item: Ejercicio) {
    sessionStore.salirDeEjercicio({ keepPath: true, nuevo: item });
  }

  /** Handler cuando se desea renumerar un ejercicio */
  private onRenumerar(ejercicio: Ejercicio, fecha: Date) {
    organizacionStore.confirmarAsientosDelEjercicio({ ejercicio, fecha });
  }

  /** Handler cuando se desea ajustar por inflacion un ejercicio */
  private onInflacion(ejercicio: Ejercicio) {
    organizacionStore.ajustarPorInflacion(ejercicio);
  }

  /** Handler cuando se desea cerrar un ejercicio */
  private onCerrar(item: Ejercicio) {
    organizacionStore.finalizarEjercicio(item);
  }

  /** Handler cuando se desea reabrir un ejercicio */
  private onReabrir(item: Ejercicio) {
    organizacionStore.reabrirEjercicio(item);
  }

  /** Handler cuando se desea recalcular el asiento de apertura de un ejercicio */
  private onRecalcularApertura(item: Ejercicio) {
    organizacionStore.recalcularApertura(item);
  }

  /** Handler cuando se desea borrar un ejercicio */
  private async onEliminar(ejercicio: Ejercicio, organizacion: string) {
    const esEjercicioActual = ejercicio.id === sessionStore.ejercicio?.id;

    // Borrar
    await organizacionStore.eliminarEjercicio({ ejercicio, organizacion });

    // Si fallo, no hacer nada
    if (organizacionStore.ejercicios.status.error) return;

    // Si borro y era el actual, salir
    if (esEjercicioActual) sessionStore.salirDeEjercicio();
  }
}
</script>

<style lang="scss" scoped></style>
