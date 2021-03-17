<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Balance" cuentaSearch>
      <ToolbarExcelButton v-if="!loading && items.items.length > 0" :url="exportUrl" />
    </PageNavBar>

    <div class="columns is-vcentered">
      <!-- Filtro -->
      <div class="column">
        <FiltroBalance
          :value="query.filter"
          :categoria="categoriaFiltro"
          @input="onNewFilter"
          :minDate="ejercicio ? ejercicio.inicio : null"
          :maxDate="ejercicio ? ejercicio.finalizacion : null"
        />
      </div>

      <!-- Saldo Balance (si es distinto de 0) -->
      <div class="column has-text-right" v-if="query.filter.categoria">
        <span style="margin-right: 15px">Saldo</span>
        <span
          v-for="(item, index) in totales"
          class="title"
          :key="index"
          :class="{
            'has-text-danger': item.saldo < 0,
            'has-text-success': item.saldo === 0
          }"
          style="margin-bottom: 0; margin-left: 20px"
          >{{ item.formattedSaldo }}</span
        >
      </div>

      <div>
        <!-- Paginacion en la parte superior -->
        <b-pagination
          :class="{ mobile: isMobile }"
          v-if="items.number > 1 || items.next"
          :simple="isMobile"
          :total="items.total"
          :current="items.number"
          :per-page="PAGE_SIZE"
          @change="onPageRequest"
        />
      </div>
    </div>

    <!-- Tabla de balance -->
    <div class="card">
      <ListaBalanceMobile
        v-if="isMobile"
        :page="items"
        :loading="loading"
        :pageSize="PAGE_SIZE"
        @selected="onCuentaSelected"
      />
      <TablaBalanceCuentas
        v-else
        :page="items"
        :loading="loading"
        :pageSize="PAGE_SIZE"
        @selected="onCuentaSelected"
        @request="onPageRequest"
      />
    </div>

    <!-- Paginacion en la parte superior -->
    <b-pagination
      v-if="isMobile && (items.number > 1 || items.next)"
      class="mobile"
      simple
      :total="items.total"
      :current="items.number"
      :per-page="PAGE_SIZE"
      @change="onPageRequest"
    />
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import TablaBalanceCuentas from '@/components/cuentas/balance/TablaBalanceCuentas.vue';
import ListaBalanceMobile from '@/components/cuentas/balance/ListaBalanceMobile.vue';
import FiltroBalance from '@/components/cuentas/balance/FiltroBalance.vue';
import { informeApi } from '../api';
import Page, { emptyPage } from '@/core/Page';
import { parseServerDate } from '../utils/date';
import { sessionStore, monedaStore, cuentaStore, uiStore } from '../store';
import { routerService, notificationService } from '../service';
import { BalanceFilter, BalanceSearchOptions } from '../api/InformeApi';
import { BalanceItem, BalanceTotal } from '../model/Balance';
import { toInt, isDefined } from '../utils/general';
import { EntityState } from '../core/ui/state/item';
import { formatCurrency } from '../utils/currency';
import { toQuerystringDictionary } from '@/core/ajax/helpers';
import { Categoria } from '@/model/Cuenta';

/**
 * Pagina de informe de balance (saldos por cuenta)
 */
@Component({
  components: { FiltroBalance, TablaBalanceCuentas, ListaBalanceMobile }
})
export default class InformeBalanceView extends Vue {
  loading = false;

  /** Pagina de items de balance. Cada item es una cuenta y su saldo */
  items: Page<BalanceItem> = emptyPage();

  /** Saldos totales del balance */
  saldos: EntityState<BalanceTotal> = { item: {}, status: {} };

  /**
   * Guarda el ultimo filtro usado para determinar si solo cambio la paginacion
   * Esto permite evitar pedir los saldos totales y el count de items nuevamente en cada pagina,
   * ya que no cambian a menos que cambie el filtro
   */
  lastFilter: BalanceFilter | null = null;

  PAGE_SIZE = 25;

  private mounted() {
    this.search();
  }

  @Watch('$route')
  private onRouteChange() {
    this.search();
  }

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }

  private get ejercicio() {
    return sessionStore.ejercicio;
  }

  /** URL donde se obtiene el Excel del balance */
  private get exportUrl() {
    return informeApi.getExportarBalanceUrl(this.query.filter);
  }

  /** Genera el query string para el balance a partir de la URL actual */
  private get query(): BalanceSearchOptions {
    return routerService.queryFromUrl(this.$route, {
      defaultPageSize: this.PAGE_SIZE,
      parse: qs => ({
        filter: {
          desde: parseServerDate(qs.desde),
          hasta: parseServerDate(qs.hasta),
          categoria: toInt(qs.categoria),
          cero: qs.cero === 'true'
        }
      })
    });
  }

  /** Obtiene la categoria a utilizar en el filtro desde la cache (a partir del id) */
  private get categoriaFiltro(): Categoria | null {
    if (!this.query.filter.categoria) return null;
    return cuentaStore.findCategoria(this.query.filter.categoria);
  }

  /** Obtiene los saldos totales por moneda. Cada item tiene el valor numerico y el formateado */
  private get totales() {
    if (this.saldos.status.loading || !this.saldos.item) return [];
    const saldos = Object.entries(this.saldos.item);
    if (saldos.length === 0) {
      return [{ formattedSaldo: formatCurrency(0, '$'), saldo: 0 }];
    }

    return saldos.map(([monedaId, saldo]) => {
      const moneda = monedaStore.find(toInt(monedaId))?.simbolo ?? '';
      return {
        formattedSaldo: formatCurrency(saldo, moneda),
        saldo
      };
    });
  }

  /** Determina si el filtro actual es igual al ultimo utilizado */
  private get isSameFilter() {
    if (!this.lastFilter) return false;

    return (
      this.lastFilter.categoria === this.query.filter.categoria &&
      this.lastFilter.cero === this.query.filter.cero &&
      this.lastFilter.desde?.getTime() === this.query.filter.desde?.getTime() &&
      this.lastFilter.hasta?.getTime() === this.query.filter.hasta?.getTime()
    );
  }

  /** Busca en el server los saldos totales del balance pedido */
  private async searchTotales() {
    try {
      // Si se aplica el mismo filtro (por ej se pide otra pagina), los totales no cambian
      if (this.isSameFilter) return;

      this.saldos.status = { loading: true };
      if (isDefined(this.query.filter.categoria)) {
        this.saldos.item = await informeApi.getBalanceTotales(this.query.filter);
      } else {
        this.saldos.item = {};
      }
    } catch (e) {
      this.saldos.status = { error: true };
      notificationService.error(e.description);
    } finally {
      this.saldos.status = {};
    }
  }

  /** Carga la categoria a utilizar en el filtro */
  private loadCategoriaFiltro() {
    if (!this.categoriaFiltro && this.query.filter.categoria) {
      cuentaStore.findCategoriaById({
        id: this.query.filter.categoria,
        path: false,
        children: false
      });
    }
  }

  /** Busca un balance a partir del filtro */
  private async search() {
    try {
      // Carga la categoria
      this.loadCategoriaFiltro();
      this.loading = true;

      // Carga los saldos totales
      this.searchTotales();

      let query, count;

      if (this.isSameFilter) {
        // Si se usa el mismo filtro (por ej otra pagina), no se pide el count (ya lo tenemos)
        query = this.query;
        count = this.items.total;
      } else {
        // Si es otro filtro, se pide el count para calcular cuantas paginas son
        query = { ...this.query, count: true };
      }

      this.items = await informeApi.getBalance(query);

      // Se guarda el filtro
      this.lastFilter = this.query.filter;

      // Si no vino el count, es porque no se lo pidio, se usa el que ya teniamos
      if (!this.items.total) this.items.total = count;

      window.scrollTo(0, 0);
    } catch (e) {
      notificationService.error(e.description);
    } finally {
      this.loading = false;
    }
  }

  /** Handler cuando cambia el filtro */
  private onNewFilter(filter: BalanceFilter) {
    routerService.updateFilter(this.$route, toQuerystringDictionary(filter));
  }

  /** Handler cuando cambia la paginacion */
  private onPageRequest(page: number) {
    routerService.updatePagination(this.$route, page);
  }

  /** Handler cuando se pide una cuenta */
  private onCuentaSelected(cuenta: BalanceItem) {
    routerService.goToCuenta(cuenta);
  }
}
</script>

<style lang="scss">
.pagination.mobile .info {
  display: none;
}
</style>
