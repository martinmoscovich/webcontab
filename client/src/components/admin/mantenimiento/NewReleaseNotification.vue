<template>
  <div class="card">
    <MediaObject
      icon="information"
      verticalCenter
      class="mb-0"
      style="padding: 24px; background: #167df0; color: white; margin-bottom: 0"
    >
      <!-- Loader cuando esta ocupado -->
      <template v-slot:left v-if="store.status.loading || store.downloading || store.updating || store.restarting">
        <div class="wc-is-loading" style="margin: 0 auto"></div>
      </template>

      <!-- No hay version nueva -->
      <span v-if="store.idle">
        Esta utilizando la última versión disponible
      </span>

      <!-- Hay nueva version disponible para descargar -->
      <span v-else-if="store.downloadAvailable">
        Nueva versión {{ store.availableToDownload }} disponible para descargar.
      </span>

      <!-- Hay nueva version descargada y lista para actualizar -->
      <span v-else-if="store.pending && update.targetRelease">
        Nueva versión {{ update.targetRelease.releaseVersion }} lista para instalar.
        <span v-if="update.targetRelease.requiresRestart">(Requiere reinicio)</span>
      </span>

      <!-- Se esta descargando la nueva version -->
      <span v-else-if="store.downloading">
        Descargando nueva versión
        <span v-if="update.targetRelease"> {{ update.targetRelease.releaseVersion }} ({{ releaseSize }})</span>.
      </span>

      <!-- Se esta actualizando la version -->
      <span v-else-if="store.updating && update.targetRelease">
        Actualizando a nueva versión {{ update.targetRelease.releaseVersion }}
        <span v-if="update.targetRelease.requiresRestart">(Requiere reinicio)</span></span
      >

      <!-- Se esta reiniciando el server con la nueva version -->
      <span v-else-if="store.restarting">
        Reiniciando Servidor. Por favor espere...
      </span>

      <!-- Boton de accion, segun el estado -->
      <template v-slot:right v-if="!store.status.loading">
        <!-- Boton de check cuando no hay nada que hacer -->
        <b-tooltip v-if="store.idle" position="is-bottom" label="Buscar">
          <a @click="onCheckClick" style="color: white"><b-icon size="is-medium" icon="magnify"/></a>
        </b-tooltip>

        <!-- Boton de descarga -->
        <b-tooltip v-else-if="store.downloadAvailable" position="is-bottom" label="Descargar">
          <a @click="onDownloadClick" style="color: white"><b-icon size="is-medium" icon="download"/></a>
        </b-tooltip>

        <!-- Boton de Actualizar -->
        <b-tooltip v-else-if="store.pending" position="is-bottom" label="Actualizar">
          <a @click="onUpdateClick" style="color: white"><b-icon size="is-medium" icon="update"/></a>
        </b-tooltip>
      </template>
    </MediaObject>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import { formatFileSize } from '@/utils/file';
import { updateStore } from '../../../store';
import { UpdateStatus } from '@/model/admin/AdminModels';

/**
 * Muestra informacion de la nueva release si existe, permite actualizar y muestra el estado
 * de la actualizacion.
 */
@Component
export default class NewReleaseNotification extends Vue {
  private mounted() {
    if (this.store.idle) this.store.check();
  }

  private get store() {
    return updateStore;
  }

  /** Informacion de la actualizacion */
  private get update(): UpdateStatus {
    return this.store.update;
  }

  /** Tamanio coon formato de la actualizacion */
  private get releaseSize() {
    if (!this.update.targetRelease?.fileSize) return '? KB';
    return formatFileSize(this.update.targetRelease?.fileSize);
  }

  /** Chequea si hay actualizaciones */
  private onCheckClick() {
    this.store.check();
  }

  /** Descarga al server la ultima actualizacion */
  private onDownloadClick() {
    this.store.download();
  }

  /** Realiza la actualizacion de la aplicacion */
  private async onUpdateClick() {
    this.store.applyUpdate();
  }
}
</script>

<style lang="scss" scoped>
.card-content {
  border-radius: 0;
  margin-bottom: 0;
}
.card-footer,
.card-footer-item {
  border: none;
}
</style>
