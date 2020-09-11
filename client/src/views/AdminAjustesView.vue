<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Mantenimiento" cuentaSearch />

    <!-- Tabs -->
    <div class="columns is-centered">
      <div class="column is-two-thirds-desktop is-two-fifths-fullhd">
        <b-tabs>
          <b-tab-item label="Actualización">
            <ReleaseView />
          </b-tab-item>

          <b-tab-item label="Backup">
            <BackupView />
          </b-tab-item>

          <b-tab-item label="Log">
            Nada aún
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

/**
 * Pagina de Ajustes
 */
@Component({ components: { ReleaseView, BackupView } })
export default class AdminAjustesView extends Vue {}
</script>

<style lang="scss" scoped></style>
