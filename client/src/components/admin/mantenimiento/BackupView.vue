<template>
  <nav class="level">
    <!-- Ultimo backup -->
    <div class="level-left">
      <div class="level-item">
        <p class="is-5">
          Ultimo backup: <strong>{{ formattedDateTime }}</strong>
        </p>
      </div>
    </div>

    <!-- Boton para crear nuevo -->
    <div class="level-right">
      <p class="level-item">
        <b-button :loading="loading" icon-left="content-save" @click="onCreateClick">Guardar</b-button>
      </p>
    </div>
  </nav>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import { BackupItem } from '@/model/admin/AdminModels';
import { formatTime, prettyFormatDate } from '../../../utils/date';
import { adminApi } from '../../../api';
import { notificationService } from '../../../service';
import { logError } from '../../../utils/log';

/**
 * Tab de Backup de Base de datos
 */
@Component({ components: {} })
export default class BackupView extends Vue {
  /** Ultimo backup realizado, o null si no hay backups */
  private lastBackup: BackupItem | null = null;

  /** Indica que se esta cargando o guardando el backup */
  private loading = false;

  private mounted() {
    this.loadLast();
  }

  /** Fecha con formato del ultimo backup */
  private get formattedDateTime() {
    if (!this.lastBackup) return 'Ninguno';
    return prettyFormatDate(this.lastBackup.ts) + ' - ' + formatTime(this.lastBackup.ts);
  }

  /** Carga el ultimo backup realizado */
  private async loadLast() {
    try {
      this.loading = true;
      this.lastBackup = await adminApi.dbBackupGetLast();
    } catch (e) {
      logError('obtener ultimo backup', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }

  /** Crea un nuevo bakcup */
  private async onCreateClick() {
    try {
      this.loading = true;
      this.lastBackup = await adminApi.createDbBackup();
    } catch (e) {
      logError('crear backup', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }
}
</script>

<style lang="scss" scoped></style>
