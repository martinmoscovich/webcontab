<template>
  <section>
    <!-- Mensaje de resultado con error -->
    <div v-if="isError" class="columns">
      <div class="notification has-text-danger">
        {{ task.error }}
      </div>
    </div>

    <!-- Estado actual de la tarea o resultado exitoso -->
    <div v-else class="columns">
      <ImportTaskSummary :task="task" />
    </div>

    <!-- Botonera -->
    <div class="columns">
      <div class="column has-text-centered">
        <div v-if="loading" class="wc-is-loading" />
        <b-button v-else-if="isError" type="is-danger" icon-left="close" @click="onClose">
          Cerrar
        </b-button>
        <b-button v-else type="is-success" icon-left="check" @click="onClose">
          Listo
        </b-button>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ImportTaskSummary from './ImportTaskSummary.vue';
import { ImportTask } from '@/model/admin/ImportTask';

/**
 * Panel que muestra el estado de una tarea de importacion en tiempo real y el resultado final.
 */
@Component({ components: { ImportTaskSummary } })
export default class ImportTaskResult extends Vue {
  /** Tarea de importacion actual */
  @Prop()
  task: ImportTask;

  /** Indica si se esta procesando en el server */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Indica si la tarea fallo */
  private get isError() {
    return this.task.status === 'ERROR';
  }

  /** Handler cuando se pide cerrar el panel */
  private onClose() {
    this.$emit('close');
  }
}
</script>

<style lang="scss" scoped></style>
