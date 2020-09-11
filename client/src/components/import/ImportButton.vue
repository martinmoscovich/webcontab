<template>
  <!-- Input de Upload -->
  <b-upload ref="upload" accept=".mdb" :loading="loadingImport" @input.native="onImportClick">
    <!-- Boton -->
    <a class="button ml-1">
      <b-icon icon="upload"></b-icon>
    </a>

    <!-- Modal de info y confirmacion -->
    <b-modal :active.sync="showModalImport" has-modal-card :canCancel="!loadingImport">
      <CardComponent title="Importar">
        <ImportTaskCard
          :task="task"
          :loading="loadingImport"
          :organizacion="organizacionActual"
          @import="onImport"
          @close="onImportClose"
        />
      </CardComponent>
    </b-modal>
  </b-upload>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { ImportTask } from '@/model/admin/ImportTask';
import { importApi } from '@/api';
import { logError } from '@/utils/log';
import { notificationService } from '@/service';
import ImportTaskCard from './ImportTaskCard.vue';
import { delay } from '@/utils/delay';
import { sessionStore, organizacionStore } from '@/store';

/**
 * Boton "Importar ejercicio" que permite hacer upload y muestra el modal para
 * confirmar la importacion
 *
 */
@Component({ components: { ImportTaskCard } })
export default class ImportButton extends Vue {
  /** Flag que muestra / oculta el modal */
  private showModalImport = false;

  /** Indica que esta procesando el archivo en el server */
  private loadingImport = false;

  /** Tarea de importacion actual, si existe (con la info a mostrar) */
  private task: ImportTask | null = null;

  $refs: { upload: Vue };

  /** Referencia al File Upload Input */
  private get fileInput() {
    return this.$refs.upload.$el.querySelector('input') as HTMLInputElement;
  }

  /** Organizacion actual */
  private get organizacionActual() {
    return sessionStore.organizacion;
  }

  /**
   * Chequea y actualiza el estado de la tarea de importacion.
   * Se llama cada 2 segundos para pollear el estado.
   */
  private async check() {
    if (!this.task) return;

    // Obtiene el estado de la tarea del server
    this.task = await importApi.get(this.task.uuid);

    if (this.task.status === 'PENDING' || this.task.status === 'IMPORTING') {
      // Si esta en proceso, esperar y volver a actualizar el estado
      await delay(2000);
      this.check();
    } else {
      // Si termino, mostrar resultado
      this.loadingImport = false;
      if (this.task.status === 'FINISHED') {
        this.onImportFinish();
      }
    }
  }

  /**
   * Handler cuando el usuario confirma la importacion. Inicia el proceso.
   * Contiene los parametros de importacion a enviar.
   */
  private async onImport(params: { cuentas: 'CODIGO' | 'NIVEL'; asientos: boolean }) {
    if (!this.task) return;
    try {
      // Inicia el proceso y activa el polleo
      this.loadingImport = true;
      await importApi.run(this.task.uuid, params);
      this.check();
    } catch (e) {
      this.loadingImport = false;
      logError('importar MDB', e);
      notificationService.error(e, { duration: 4000 });
    }
  }

  /**
   * Handler cuando se selecciono un archivo a importar.
   * Obtiene la ref al archivo y lo sube al server para obtener la info
   */
  private async onImportClick() {
    this.loadingImport = true;
    try {
      // Si no hay archivo, no hacer nada
      if (!this.fileInput.files || this.fileInput.files.length === 0) return;

      // Sube el archivo y obtiene la info de la tarea
      this.task = await importApi.create(this.fileInput.files[0], {
        enActual: true
      });

      // Muestra el modal con la info para confirmar
      this.showModalImport = true;
    } catch (e) {
      logError('subir MDB', e);
      notificationService.error(e);
    } finally {
      this.fileInput.value = '';
      this.loadingImport = false;
    }
  }

  /** Handler se pide cerrar el modal */
  private onImportClose() {
    this.task = null;
    this.showModalImport = false;
  }

  /** Handler cuando termina la importacion */
  private onImportFinish() {
    if (this.task && this.task.organizacion) {
      // Se actualizan los ejercicios por si se creo uno
      organizacionStore.findEjercicios(this.task.organizacion);
    }
  }
}
</script>

<style lang="stylus" scoped></style>
