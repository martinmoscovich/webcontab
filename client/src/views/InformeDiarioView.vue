<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Diario" cuentaSearch>
      <ToolbarExcelButton v-if="!loading && asientos.items.length > 0" :url="exportUrl" />
    </PageNavBar>

    <div class="columns ma-0" style="align-items: center">
      <div class="column py-0">
        <!-- Filtro -->
        <FiltroAsientos
          :value="query.filter"
          @input="onNewFilter"
          :minDate="ejercicio ? ejercicio.inicio : null"
          :maxDate="ejercicio ? ejercicio.finalizacion : null"
        />
      </div>
      <div>
        <!-- Paginacion en la parte superior -->
        <b-pagination
          v-if="asientos.number > 1 || asientos.next"
          :class="{ mobile: isMobile }"
          :total="asientos.total"
          :current="asientos.number"
          :per-page="PAGE_SIZE"
          @change="onPageRequest"
        />
      </div>
    </div>

    <!-- Tabla de Asientos -->
    <div class="card">
      <ListaAsientosMobile v-if="isMobile" :page="asientos" :loading="loading" />
      <TablaAsientos v-else :page="asientos" :loading="loading" :pageSize="PAGE_SIZE" @request="onPageRequest" />
    </div>
    <!-- Paginacion en la parte inferior para mobile -->
    <b-pagination
      v-if="isMobile && (asientos.number > 1 || asientos.next)"
      class="mobile"
      :total="asientos.total"
      :current="asientos.number"
      :per-page="PAGE_SIZE"
      @change="onPageRequest"
    />
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import TablaAsientos from '@/components/asientos/TablaAsientos.vue';
import ListaAsientosMobile from '@/components/asientos/ListaAsientosMobile.vue';
import FiltroAsientos from '@/components/asientos/FiltroAsientos.vue';
import { asientoApi, informeApi } from '@/api';
import { AsientoDTO } from '@/model/AsientoDTO';
import Page, { emptyPage } from '@/core/Page';
import { parseServerDate } from '@/utils/date';
import { AsientosSearchFilter, AsientosSearchOptions } from '@/api/AsientoApi';
import { sessionStore, uiStore } from '@/store';
import { routerService } from '@/service';
import { toInt } from '@/utils/general';
import { toQuerystringDictionary } from '@/core/ajax/helpers';

/**
 * Pagina de informe de diario (asientos con sus imputaciones)
 */
@Component({
  components: { TablaAsientos, FiltroAsientos, ListaAsientosMobile }
})
export default class InformeDiarioView extends Vue {
  /** Indica que se esta cargando la pagina de asientos */
  private loading = false;

  /** Pagina de asientos actual */
  asientos: Page<AsientoDTO> = emptyPage();

  PAGE_SIZE = 25;

  @Watch('$route')
  private onRouteChange() {
    this.search();
  }

  private mounted() {
    this.search();
  }

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }

  private get ejercicio() {
    return sessionStore.ejercicio;
  }

  /** URL donde se obtiene el Excel del diario */
  private get exportUrl() {
    return informeApi.getExportarDiarioUrl(this.query.filter);
  }

  /** Handler cuando cambia el filtro, se navega a la nueva URL */
  private onNewFilter(filter: AsientosSearchFilter) {
    routerService.updateFilter(
      this.$route,
      toQuerystringDictionary(filter)
      // asientoApi.filterToQuerystring(filter)
    );
  }

  /** Handler cuando se pide una nueva pagina */
  private onPageRequest(page: number) {
    routerService.updatePagination(this.$route, page);
  }

  /** Genera el query string para el balance a partir de la URL actual */
  private get query(): AsientosSearchOptions {
    return routerService.queryFromUrl(this.$route, {
      defaultPageSize: this.PAGE_SIZE,
      parse: qs => ({
        imputaciones: true,
        filter: {
          desde: parseServerDate(qs.desde),
          hasta: parseServerDate(qs.hasta),
          min: toInt(qs.min),
          max: toInt(qs.max)
        }
      })
    });
  }

  /** Busca la pagina de asientos e imputaciones segun el filtro y la paginacion */
  async search() {
    try {
      this.loading = true;
      this.asientos = await asientoApi.search(this.query);
      window.scrollTo(0, 0);
      // } catch (_) {
    } finally {
      this.loading = false;
    }
  }
}
</script>

<style lang="scss" scoped></style>
