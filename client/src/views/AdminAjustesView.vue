<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Mantenimiento" cuentaSearch />

    <!-- Tabs -->
    <div class="columns is-centered">
      <div
        class="column"
        :class="isLogTab ? 'is-10-fullhd is-12-desktop' : 'is-two-fifths-fullhd is-two-thirds-desktop'"
      >
        <b-tabs @change="onChange">
          <b-tab-item label="Actualización">
            <ReleaseView />
          </b-tab-item>

          <b-tab-item label="Backup">
            <BackupView />
          </b-tab-item>

          <b-tab-item label="Log">
            <LogView :active="isLogTab" />
          </b-tab-item>
        </b-tabs>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ReleaseView from '@/components/admin/mantenimiento/ReleaseView.vue';
import BackupView from '@/components/admin/mantenimiento/BackupView.vue';
import LogView from '@/components/admin/mantenimiento/LogView.vue';

/**
 * Pagina de Ajustes
 */
@Component({ components: { ReleaseView, BackupView, LogView } })
export default class AdminAjustesView extends Vue {
  private isLogTab = false;
  onChange(index: number) {
    this.isLogTab = index === 2;
  }
}
</script>

<style lang="scss" scoped></style>
