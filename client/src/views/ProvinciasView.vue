<template>
  <section class="section is-main-section">
    <PageNavBar title="Provincias" cuentaSearch />
    <div class="columns is-centered">
      <div class="card column is-two-thirds-desktop is-two-fifths-fullhd">
        <TablaProvincias :provincias="provincias" :loading="loading" @change="onChange" />
      </div>
      <!-- <div class="card column">
        <TablaProvincias :provincias="provincias" :loading="loading" />
      </div> -->
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import TablaProvincias from '@/components/admin/TablaProvincias.vue';
import { provinciaStore } from '../store';
import { Provincia } from '../model/Provincia';

@Component({ components: { TablaProvincias } })
export default class ProvinciasView extends Vue {
  mounted() {
    provinciaStore.list({ refresh: true });
  }

  get provincias() {
    return provinciaStore.lista.items;
  }

  get loading() {
    return provinciaStore.lista.status.loading;
  }

  onChange(prov: Provincia) {
    provinciaStore.save(prov);
  }
}
</script>

<style lang="scss" scoped></style>
