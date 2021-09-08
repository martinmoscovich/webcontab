<template>
  <nav class="panel">
    <!-- Mensaje cuando no hay ejercicios -->
    <div v-if="sortedEjercicios.length === 0">
      <p class="subtitle has-text-centered pa-3">Aún no hay ejercicios</p>
    </div>

    <!-- Item -->
    <div
      v-else
      v-for="item in sortedEjercicios"
      :key="item.id"
      class="panel-block ejercicio-item"
      :class="{ finalizado: item.finalizado, hand: !esSeleccionado(item) }"
      @click="onItemClick(item)"
    >
      <!-- Icono -->
      <span class="panel-icon">
        <b-icon :icon="getIcon(item)" custom-size="mdi-24px" size="is-small" />
      </span>
      <!-- Fechas -->
      {{ formatDate(item.inicio) }} - {{ formatDate(item.finalizacion) }}

      <!-- Acciones -->
      <template v-if="mostarAcciones && !readonly">
        <div style="flex-grow: 1" />

        <!-- Menu de acciones posibles para un ejercicio -->
        <EjercicioActions
          :finalizado="item.finalizado"
          :tieneApertura="!!item.asientoAperturaId"
          @renumerar="onRenumerar(item)"
          @inflacion="onInflacion(item)"
          @cerrar="onCerrar(item)"
          @reabrir="onReabrir(item)"
          @recalcular="onRecalcularApertura(item)"
          @eliminar="onEliminar(item)"
        />

        <ModalRenumerarEjercicio
          :ejercicio="ejercicioRenumerar"
          @confirm="onRenumerarConfirm"
          @close="onRenumerarClose"
        />
      </template>
    </div>
  </nav>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import ModalRenumerarEjercicio from './ModalRenumerarEjercicio.vue';
import EjercicioActions from './EjercicioActions.vue';
import { Ejercicio } from '@/model/Ejercicio';
import { formatDate } from '@/utils/date';
import { buildCompareFn } from '@/utils/array';
import { sessionStore } from '@/store';
import { notificationService } from '@/service';

/** Lista de ejercicios de una organizacion */
@Component({ components: { EjercicioActions, ModalRenumerarEjercicio } })
export default class EjerciciosList extends Vue {
  @Prop()
  ejercicios: Ejercicio[];

  /** Indica si permite realizar acciones sobre el ejercicio (cerrar, reabrir) */
  @Prop({ type: Boolean })
  mostarAcciones: boolean;

  /** Indica si no se puede modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si se muestra el modal para renumerar y para cual ejercicio. Si es null, no muestra el modal */
  private ejercicioRenumerar: Ejercicio | null = null;

  /** Ordena los ejercicios por orden de creacion */
  private get sortedEjercicios() {
    return this.ejercicios?.sort(buildCompareFn({ field: 'id', descending: true })) ?? [];
  }

  /** Formatea una fecha */
  private formatDate(date: Date) {
    return formatDate(date);
  }

  /** Determina si el ejercicio indicado es el de la sesion */
  private esSeleccionado(item: Ejercicio) {
    return sessionStore.ejercicio?.id === item.id;
  }

  /** Determina el icono del ejercicio, segun si esta abierto o cerrado */
  private getIcon(ej: Ejercicio) {
    return ej.finalizado ? 'lock' : 'lock-open-variant';
  }

  /** Handler cuando se selecciona un ejercicio */
  private onItemClick(item: Ejercicio) {
    // Si ya esta seleccionado, no hay que hacer nada
    if (this.esSeleccionado(item)) return;

    this.$emit('selected', item);
  }

  /** Handler cuando se hace click en "Renumerar" */
  private onRenumerar(item: Ejercicio) {
    this.ejercicioRenumerar = item;
  }

  private onRenumerarConfirm(fecha: Date) {
    this.$emit('renumerar', this.ejercicioRenumerar, fecha);
    this.ejercicioRenumerar = null;
  }

  private onRenumerarClose() {
    this.ejercicioRenumerar = null;
  }

  private onInflacion(item: Ejercicio) {
    // Si ya existe el asiento de ajuste, es un "reajuste"
    const accion = item.asientoAjusteId ? 'Reajustar' : 'Ajustar';

    // Se pide confirmacion
    this.$buefy.dialog.confirm({
      title: accion + ' por inflación',
      icon: 'help',
      hasIcon: true,
      message: this.getConfirmMessage(item, accion.toLowerCase() + ' por inflación'),
      cancelText: 'No',
      confirmText: 'Ajustar',
      // Si confirma, se emite el evento
      onConfirm: () => this.$emit('inflacion', item)
    });
  }

  /** Handler cuando se hace click en "Cerrar" */
  private onCerrar(item: Ejercicio) {
    // Se pide confirmacion
    this.$buefy.dialog.confirm({
      title: 'Cerrar ejercicio',
      icon: 'help',
      hasIcon: true,
      message: this.getConfirmMessage(item, 'cerrar'),
      cancelText: 'No',
      confirmText: 'Cerrar',
      // Si confirma, se emite el evento
      onConfirm: () => this.$emit('cerrar', item)
    });
  }

  /** Handler cuando se hace click en "Reabrir" */
  private onReabrir(item: Ejercicio) {
    // Se pide confirmacion
    this.$buefy.dialog.confirm({
      title: 'Reabrir ejercicio',
      icon: 'help',
      hasIcon: true,
      message: this.getConfirmMessage(item, 'reabrir'),
      cancelText: 'No',
      confirmText: 'Reabrir',
      // Si confirma, se emite el evento
      onConfirm: () => this.$emit('reabrir', item)
    });
  }

  /** Handler cuando se hace click en "Recalcula Apertura" */
  private onRecalcularApertura(item: Ejercicio) {
    // Se pide confirmacion
    this.$buefy.dialog.confirm({
      title: 'Recalcular apertura de ejercicio',
      icon: 'help',
      hasIcon: true,
      message: this.getConfirmMessage(item, 'recalcular apertura'),
      cancelText: 'No',
      confirmText: 'Recalcular',
      // Si confirma, se emite el evento
      onConfirm: () => this.$emit('recalcular', item)
    });
  }

  /** Handler cuando se hace click en "Eliminar" */
  private onEliminar(item: Ejercicio) {
    // Se pide confirmacion, que incluye por seguridad el requermiento que el usuario
    // ingrese el nombre de la organizacion
    this.$buefy.dialog.prompt({
      title: 'Eliminar ejercicio',
      type: 'is-danger',
      hasIcon: true,
      message: this.getConfirmMessage(item, 'eliminar'),
      inputAttrs: {
        placeholder: 'Ingrese el nombre de la organizacion'
      },
      cancelText: 'Cancelar',
      confirmText: 'Eliminar',
      // Si confirma, se chequea el nombre y se emite el evento
      onConfirm: (value: string) => {
        if (value === item.organizacion.nombre) {
          this.$emit('eliminar', item, value);
        } else {
          notificationService.warn('Nombre incorrecto');
        }
      }
    });
  }

  /** Genera el mensaje de confirmacion para el ejercicio y la accion deseada */
  private getConfirmMessage(item: Ejercicio, accion: string) {
    return `¿Está seguro que desea <b>${accion}</b> el ejercicio que comprende desde ${formatDate(
      item.inicio
    )} hasta ${formatDate(item.finalizacion)}?`;
  }
}
</script>

<style lang="scss">
.hand:hover {
  background: #f2f2f2;
}
.finalizado {
  background: #e2e2e2;
}
.ejercicio-item {
  .menuitem-text {
    margin-left: 8px;
  }
  .icon {
    width: 20px;
    height: 20px;
  }
}
</style>
