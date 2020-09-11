<template>
  <CardComponentWithActions :title="'Version actual: ' + release.releaseVersion" list>
    <div>
      <!-- Fecha y hora -->
      Lanzada el {{ date }} a las {{ time }}
      <!-- <div><b-icon icon="calendar" />{{ date }} <b-icon icon="clock" />{{ time }}</div> -->

      <!-- Version del server -->
      <!-- <div><b-icon icon="server" />{{ release.serverVersion }}</div> -->
    </div>
  </CardComponentWithActions>
</template>

<script lang="ts">
/**
 * Muestra informacion de una release
 */
import { Vue, Component, Prop } from 'vue-property-decorator';
import { ReleaseInfo } from '@/model/admin/AdminModels';
import { formatTime, prettyFormatDate } from '../../../utils/date';

@Component
export default class ReleaseCard extends Vue {
  @Prop()
  release: ReleaseInfo;

  /** Fecha con formato de la release */
  private get date() {
    if (!this.release) return '';
    return prettyFormatDate(this.release.releaseDate);
  }
  /** Hora con formato de la release */
  private get time() {
    if (!this.release) return '';
    return formatTime(this.release.releaseDate);
  }
}
</script>

<style lang="scss" scoped></style>
