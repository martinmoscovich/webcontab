<template>
  <article>
    <section>
      <!-- Organizacion importada -->
      <b-field label="Organización" class="has-text-centered">
        {{ task.organizacion.nombre }} ({{ task.organizacion.cuit }})
      </b-field>

      <!-- Ejercicio importado -->
      <b-field label="Ejercicio" class="has-text-centered">
        {{ formatDate(task.ejercicio.inicio) }} -
        {{ formatDate(task.ejercicio.finalizacion) }}
      </b-field>
    </section>

    <nav class="level pa-2 mt-3 mb-3">
      <!-- Categorias importadas -->
      <ImportTaskSummaryItem
        label="Categorías"
        :value="task.summary.categoriasImportadas"
        :total="task.summary.categoriasTotales"
      />

      <!-- Cuentas importadas -->
      <ImportTaskSummaryItem
        label="Cuentas"
        :value="task.summary.cuentasImportadas"
        :total="task.summary.cuentasTotales"
      />
    </nav>
    <nav class="level pa-2 mt-3 mb-3">
      <!-- Asientos importados -->
      <ImportTaskSummaryItem label="Asientos" :value="task.summary.asientosImportados" />

      <!-- Imputaciones importadas -->
      <ImportTaskSummaryItem label="Imputaciones" :value="task.summary.imputacionesImportadas" />
    </nav>
  </article>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ImportTaskSummaryItem from './ImportTaskSummaryItem';
import { ImportTask } from '@/model/admin/ImportTask';
import { formatDate } from '../../utils/date';

/**
 * Muestra el estado de la importacion, indicando cuantos elementos
 * se importaron de cada tipo
 */
@Component({ components: { ImportTaskSummaryItem } })
export default class ImportTaskSummary extends Vue {
  @Prop()
  task: ImportTask;

  /** Formatea una fecha */
  private formatDate(date: Date) {
    return formatDate(date);
  }
}
</script>

<style lang="scss" scoped></style>
