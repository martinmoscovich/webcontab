<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Mayor" cuentaSearch />

    <div class="columns is-centered">
      <!-- Mensaje para informar que el mayor de una cuenta se muestra en la pantalla de Cuenta -->
      <div class="column is-half-fullhd is-three-quarters-widescreen is-four-fifths-tablet form-card">
        <b-message type="is-info">
          <div>
            Aquí puede generar un Excel con los mayores de una o más cuentas.
          </div>
          <div>
            Para ver el mayor de una sola cuenta dentro del sistema, utilice el buscador de arriba.
          </div>
        </b-message>

        <!-- Filtro -->
        <div class="columns">
          <div class="column">
            <FiltroAsientos
              :value="filter"
              @input="onNewFilter"
              buttonLabel="Generar"
              buttonIcon="microsoft-excel"
              :minDate="ejercicio ? ejercicio.inicio : null"
              :maxDate="ejercicio ? ejercicio.finalizacion : null"
            />
          </div>
        </div>

        <!-- Cuentas a incluir -->
        <div class="columns">
          <div class="column">
            <CardSeleccionCuentas :cuentas="cuentas" @add="onAdd" @remove="onRemove" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import FiltroAsientos from '@/components/asientos/FiltroAsientos.vue';
import { AsientosSearchFilter } from '../api/AsientoApi';
import { sessionStore } from '../store';
import CardSeleccionCuentas from '@/components/cuentas/CardSeleccionCuentas.vue';
import { informeApi } from '../api';
import { CuentaOCategoria } from '../model/Cuenta';
import { byId } from '../utils/array';
import { notificationService } from '../service';

/**
 * Pagina de informe Mayor (imputaciones por cuenta).
 * Permite generar un reporte Excel
 */
@Component({
  components: { FiltroAsientos, CardSeleccionCuentas }
})
export default class InformeMayorView extends Vue {
  /** Indica que se esta cargando la pagina de asientos */
  private loading = false;

  /** */
  private filter: AsientosSearchFilter = {};

  /** Categorias y cuentas a incluir en el reporte */
  private cuentas: CuentaOCategoria[] = [];

  private get ejercicio() {
    return sessionStore.ejercicio;
  }

  /** Handler cuando se incluye una cuenta o categoria en el reporte */
  private onAdd(item: CuentaOCategoria) {
    if (!this.cuentas.find(byId(item.id))) this.cuentas.push(item);
  }

  /** Handler cuando se quita una cuenta o categoria del reporte */
  private onRemove(item: CuentaOCategoria) {
    this.cuentas = this.cuentas.filter(c => c.id !== item.id);
  }

  /** Handler se hace click en "Generar" */
  private onNewFilter(filter: AsientosSearchFilter) {
    this.filter = filter;

    // Ids de categorias
    const categorias: number[] = this.cuentas.filter(c => !c.imputable).map(c => c.id);

    // Ids de cuentas
    const cuentas: number[] = this.cuentas.filter(c => c.imputable).map(c => c.id);

    if (cuentas.length === 0 && categorias.length === 0) {
      notificationService.warn('Debe seleccionar al menos una cuenta');
      return;
    }

    // Abre el reporte
    window.open(informeApi.getExportarMayorUrl({ cuentas, categorias }, this.filter));
  }
}
</script>

<style lang="scss" scoped></style>
