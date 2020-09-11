<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar cuentaSearch :title="title" />
    <div class="columns is-centered">
      <!-- Lista -->
      <div class="column column is-one-third-desktop is-one-quarter-fullhd">
        <MonedaList :items="monedas" @selected="onItemSelected" @new="onNewClick" />
      </div>

      <!-- Form ABM -->
      <div class="column is-expand">
        <CardComponentWithActions title="Detalles" icon="ballot" dense>
          <MonedaForm class="pa-3 " ref="form" :value="selected" :disabled="isList" :loading="loading" @save="onSave" />
        </CardComponentWithActions>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import MonedaList from '@/components/admin/MonedaList.vue';
import MonedaForm from '@/components/admin/MonedaForm.vue';
import { routerService } from '../service';
import { monedaStore } from '../store';
import { Moneda } from '../model/Moneda';

/** Pagina de ABM de monedas */
@Component({ components: { MonedaList, MonedaForm } })
export default class MonedasView extends Vue {
  private mounted() {
    this.loadMonedas();
  }

  @Watch('$route')
  private onRouteChange() {
    this.loadMonedas();
  }

  /** Carga las monedas del server */
  private loadMonedas() {
    monedaStore.list({ refresh: true });
  }

  /** Titulo del toolbar */
  private get title() {
    if (this.isList) return 'Monedas';
    if (this.isNew) return 'Nueva';
    return this.selected?.nombre ?? '';
  }

  /** Monedas cargadas */
  private get monedas() {
    return monedaStore.lista.items;
  }

  /** Indica si se estan cargando, guardando o eliminando monedas */
  private get loading() {
    return monedaStore.lista.status.loading;
  }

  /** Obtiene la moneda seleccionada (si existe), segun la URL */
  private get selected(): Moneda | undefined {
    if (this.isList || this.isNew) return undefined;
    return monedaStore.find(parseInt(this.$route.params.id, 10));
  }

  /** Indica que se esta creando una moneda, segun la URL */
  private get isNew() {
    return routerService.isCurrent(this.$route, routerService.monedaNueva());
  }

  /** Indica que no hay moneda seleccionada, segun la URL */
  private get isList() {
    return routerService.isCurrent(this.$route, routerService.monedas());
  }

  /** Handler de seleccion de una moneda */
  private onItemSelected(row: Moneda) {
    routerService.goToMoneda(row);
  }

  /** Handler de click en "Nuevo" */
  private onNewClick() {
    routerService.goToNuevaMoneda();
  }

  /** Handler de guardar la moneda */
  private async onSave(moneda: Moneda) {
    // Se guarda
    await monedaStore.save(moneda);

    // Si hubo error, no se hace nada
    if (monedaStore.lista.status.error) return;

    // Se recargan las monedas
    await monedaStore.list({ refresh: true });

    if (monedaStore.lastSaved) {
      if (this.isNew) {
        routerService.goToMoneda(monedaStore.lastSaved);
      }
    } else {
      routerService.goToMonedas();
    }
  }
}
</script>

<style lang="scss" scoped></style>
