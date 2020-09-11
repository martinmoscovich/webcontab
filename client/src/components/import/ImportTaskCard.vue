<template>
  <!-- Panel de Review y confirmacion -->
  <ImportTaskReview
    v-if="review"
    :task="task"
    :organizacion="organizacion"
    @import="$emit('import', $event)"
    @close="$emit('close')"
  />

  <!-- Panel de estado y resultado -->
  <ImportTaskResult v-else :task="task" :loading="loading" @close="$emit('close')" />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ImportTaskReview from './ImportTaskReview.vue';
import ImportTaskResult from './ImportTaskResult.vue';
import { ImportTask } from '@/model/admin/ImportTask';
import { Organizacion } from '../../model/Organizacion';

/**
 * Informacion de Importacion.
 * Contiene 2 partes:
 * - Primero, muestra la info del archivo y pide confirmacion
 * - Luego, muestra el estado en tiempo real y el resultado final.
 *
 */
@Component({ components: { ImportTaskReview, ImportTaskResult } })
export default class ImportTaskCard extends Vue {
  /** Tarea de importacion a mostrar */
  @Prop()
  task: ImportTask;

  /** Organizacion donde se importa */
  @Prop()
  organizacion: Organizacion;

  /** Indica si se esta procesando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Indica si se debe mostrar la pantalla de Review y confirmacion */
  private get review() {
    return this.task.status === 'PENDING';
  }
}
</script>

<style lang="scss" scoped></style>
