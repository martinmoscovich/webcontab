<template>
  <div v-if="status">
    <!-- Info de la nueva release -->
    <NewReleaseNotification />

    <!-- Info de la release actual -->
    <ReleaseCard :release="status.currentRelease" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ReleaseCard from './ReleaseCard.vue';
import NewReleaseNotification from './NewReleaseNotification.vue';
import { updateStore } from '@/store';

/**
 * Tab de Update
 */
@Component({ components: { ReleaseCard, NewReleaseNotification } })
export default class ReleaseView extends Vue {
  private mounted() {
    this.loadStatus();
  }

  /**
   * Estado del Updater
   */
  private get status() {
    return updateStore.update;
  }

  private async loadStatus() {
    updateStore.refresh(false);
  }
}
</script>

<style lang="scss" scoped></style>
