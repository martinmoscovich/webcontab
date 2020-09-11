<template>
  <CardComponentWithActions title="Ejercicios" icon="ballot" dense>
    <template v-slot:actions>
      <!-- Boton Nuevo ejercicio -->
      <b-tooltip v-if="!readonly && !loading" label="Nuevo ejercicio" position="is-bottom" type="is-info">
        <b-button icon-left="plus" @click="onNewClick" />
      </b-tooltip>

      <!-- boton Importar ejercicio -->
      <b-tooltip v-if="!readonly && !loading" label="Importar ejercicio" position="is-bottom" type="is-info">
        <ImportButton />
      </b-tooltip>
    </template>

    <!-- Lista de ejercicios -->
    <EjerciciosList
      :ejercicios="ejercicios"
      :readonly="readonly"
      mostarAcciones
      class="pa-2"
      @cerrar="onCerrar"
      @reabrir="onReabrir"
      @eliminar="onEliminar"
      @selected="onSelected"
    />

    <!-- Modal de nuevo ejercicio -->
    <b-modal :active.sync="showModalNew" has-modal-card :canCancel="false">
      <CardComponent title="Nuevo Ejercicio">
        <EjercicioForm
          isModal
          :minDate="nextAvailableDate"
          :isFirst="ejercicios.length === 0"
          @save="onEjercicioSave"
          @close="showModalNew = false"
        />
      </CardComponent>
    </b-modal>

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Ejercicio } from '../../model/Ejercicio';
import EjercicioForm from './EjercicioForm.vue';
import EjerciciosList from './EjerciciosList.vue';
import ImportButton from '@/components/import/ImportButton.vue';

/**
 * Panel ABM de ejercicios de una organizacion.
 * Permite ver la lista y crear, modificar o eliminar ejercicios.
 */
@Component({
  components: { EjercicioForm, EjerciciosList, ImportButton }
})
export default class CardEjercicios extends Vue {
  @Prop({ type: Array })
  ejercicios: Ejercicio[];

  @Prop({ type: Boolean })
  loading: boolean;

  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si se muestra el modal para crear un ejercicio */
  private showModalNew = false;

  /**
   * Obtiene la fecha minima disponible para el comienzo de un nuevo ejercicio.
   * Se calcula tomando la fecha de fin del ultimo ejercicio y sumando un dia.
   */
  private get nextAvailableDate() {
    // Si no hay ejercicios, no hay fecha minima
    if (!this.ejercicios) return null;

    // Se obtiene la fecha de finalizacion mas tardia de todos los ejercicios
    const ts = this.ejercicios.map(ej => ej.finalizacion).reduce((max, fin) => Math.max(max, fin.getTime()), 0);

    // Si el ts es 0, no hay fecha minima
    if (ts === 0) return null;

    // Se le suma un dia, ya que debe empezar un dia despues del ultimo
    return new Date(ts + 86400000);
  }

  /** Handler del boton de crear nuevo ejercicio */
  private onNewClick() {
    this.showModalNew = true;
  }

  /** Handler cuando se guarda un nuevo ejercicio */
  private onEjercicioSave(ej: Ejercicio & { cerrarAnterior: boolean }) {
    this.$emit('save', ej);
    this.showModalNew = false;
  }

  /** Handler cuando se selecciona un ejercicio */
  private onSelected(item: Ejercicio) {
    this.$emit('selected', item);
  }

  /** Handler del boton de cerrar ejercicio */
  private onCerrar(item: Ejercicio) {
    this.$emit('cerrar', item);
  }

  /** Handler del boton de reabrir ejercicio */
  private onReabrir(item: Ejercicio) {
    this.$emit('reabrir', item);
  }

  /** Handler del boton de borrar ejercicio */
  private onEliminar(item: Ejercicio, organizacion: string) {
    this.$emit('eliminar', item, organizacion);
  }
}
</script>
