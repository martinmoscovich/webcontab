<template>
  <LogViewer :log="content" loading />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { adminApi } from '@/api';
import { notificationService } from '@/service';
import { logError } from '@/utils/log';
import { Timer } from '@/core/Timer';
import LogViewer from '@femessage/log-viewer';

/** Numero de lineas a traer */
const NUM_LINES = 150;

/** Cada cuanto tiempo se actualiza el log  */
const LOG_REFRESH_INTERVAL = 2000;

/**
 * Tab de Logs
 */
@Component({ components: { LogViewer } })
export default class LogView extends Vue {
  @Prop({ type: Boolean })
  private active: boolean;

  /** Contenido actual del log */
  private log = '';

  /** Indica que se esta cargando o guardando el backup */
  private loading = false;

  /** Timer para actualizar el recurrentemente  */
  private timer: Timer = new Timer({ interval: LOG_REFRESH_INTERVAL, type: 'rate', onTime: () => this.readLog() });

  private mounted() {
    this.readLog();
    this.timer.start();
  }

  private destroyed() {
    this.timer.stop();
  }

  @Watch('active')
  private onActiveChange() {
    if (this.active) {
      this.timer.start();
    } else {
      this.timer.stop();
    }
  }

  /**
   * Obtiene el texto a mostrar pintandolo de color segun el tipo de log
   */
  private get content() {
    if (!this.log) return '';
    return this.log
      .split('\n')
      .map(l => this.getColor(l) + l)
      .join('\n');
  }

  /** Obtiene el codigo ANSI del color de la linea segun el tipo de log  */
  private getColor(line: string) {
    if (line.includes('ERROR')) return '\u001b[31m';
    if (line.includes('WARN')) return '\u001b[33m';
    if (line.includes('INFO')) return '\u001b[36m';
    // if (line.includes('DEBUG')) return '\u001b[32m';
    return '';
  }

  /** Carga las ultimas n lineas de log */
  private async readLog() {
    try {
      this.loading = true;
      this.log = await adminApi.logRead(NUM_LINES);
    } catch (e) {
      logError('leer log', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }
}
</script>

<style lang="scss" scoped></style>
