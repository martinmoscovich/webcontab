<template>
  <section>
    <template v-if="permitido">
      <div class="columns">
        <div class="column">
          <!-- Info de la organizacion -->
          <Field label="Organización">
            <span class="">{{ task.organizacion.cuit }} - {{ task.organizacion.nombre }}</span>
          </Field>

          <!-- Info del ejercicio -->
          <Field v-if="task.puedeImportarEjercicio" label="Ejercicio">
            <span class=""
              >{{ formatDate(task.ejercicio.inicio) }} - {{ formatDate(task.ejercicio.finalizacion) }}</span
            >
          </Field>
        </div>
        <div class="column is-narrow">
          <!-- Seleccion de estrategia de importacion de cuentas -->
          <Field label="Cuentas" style="margin-top: 5px">
            <b-select v-model="modoCuenta">
              <option value="CODIGO">Por Codigo</option>
              <option value="NIVEL">Por Nivel</option>
            </b-select>
          </Field>

          <!-- Switch que indica si importar asientos e imputaciones -->
          <Field v-if="task.puedeImportarEjercicio" style="margin-top: 5px">
            <b-switch v-model="incluirAsientos">Incluir Asientos</b-switch>
          </Field>
        </div>
      </div>

      <!-- Mensaje de que elementos se importaran -->
      <div class="columns">
        <div class="column notification">
          <strong v-html="notificationMessage"> </strong>
        </div>
      </div>
    </template>

    <!-- Mensaje cuando la importacion no esta permitida -->
    <div v-else class="columns">
      <div class="notification">
        Solo se puede importar ejercicios y cuentan de
        <strong>{{ organizacion.nombre }} ({{ organizacion.cuit }})</strong>
        <br />pero esta base de datos pertenece a
        <strong>{{ task.organizacion.nombre }} ({{ organizacion.cuit }})</strong>
      </div>
    </div>

    <!-- Botonera -->
    <b-field style="justify-content: flex-end">
      <b-button style="width: 0; height: 0; visibility: hidden" />
      <b-field style="margin-right: 5px">
        <b-button v-if="permitido" type="is-success" icon-left="content-save" @click="onImport">
          Importar
        </b-button>
      </b-field>
      <b-field>
        <b-button type="is-danger" icon-left="close" @click="onClose">
          Cerrar
        </b-button>
      </b-field>
    </b-field>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { ImportTask } from '@/model/admin/ImportTask';
import { formatDate } from '@/utils/date';
import { Organizacion } from '@/model/Organizacion';

/**
 * Panel que muestra la informacion de un archivo a importar, permite
 * configurar parametros de importacion y confirmar el proceso.
 *
 */
@Component
export default class ImportTaskReview extends Vue {
  /** Tarea de imporacion actual */
  @Prop()
  task: ImportTask;

  /** Organizacion actual */
  @Prop()
  organizacion: Organizacion;

  /** Flag configurable que indica si importar asientos e imputaciones */
  private incluirAsientos = true;

  /** Indica la estrategia de importacion de cuentas */
  private modoCuenta: 'CODIGO' | 'NIVEL' = 'CODIGO';

  /** Segun la configuracion actual, indica que elementos se importaran */
  private get notificationMessage() {
    if (this.task.puedeImportarEjercicio) {
      return this.incluirAsientos
        ? 'Se importarán las cuentas, el ejercicio y sus asientos'
        : 'Solo se importarán las cuentas';
    } else {
      return 'Solo se importarán las cuentas.<br />No se puede importar el ejercicio porque ya existe o se solapa con existentes.';
    }
  }

  /** Indica si la tarea de importacion esta permitida */
  private get permitido() {
    // La org actual tiene que se la del archivo a importar
    return this.organizacion.id === this.task.organizacion.id;
  }

  /** Formatea una fecha */
  private formatDate(date: Date) {
    return formatDate(date);
  }

  /** Handler cuando se confirma la importacion */
  private onImport() {
    this.$emit('import', {
      cuentas: this.modoCuenta,
      asientos: this.incluirAsientos && this.task.puedeImportarEjercicio
    });
  }

  /** Handler que cierra el panel para cancelar */
  private onClose() {
    this.$emit('close');
  }
}
</script>

<style lang="scss" scoped></style>
