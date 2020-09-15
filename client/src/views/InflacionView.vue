<template>
  <section class="section is-main-section">
    <!--Toolbar -->
    <PageNavBar title="Inflacion" cuentaSearch />

    <div class="columns is-centered">
      <div class="column is-two-thirds-desktop is-two-fifths-fullhd">
        <!-- Filtro -->
        <div class="columns is-centered">
          <div class="column">
            <b-field grouped>
              <PeriodoInput month :value="filter" @input="onNewPeriodo" />

              <b-field label="Moneda" label-position="on-border">
                <MonedaSelect :value="filter.moneda" @input="onNewMoneda" />
              </b-field>
            </b-field>
          </div>
        </div>

        <!-- Tabla -->
        <div class="columns is-centered">
          <div class="card column">
            <TablaInflacion :indices="indices" :loading="loading" @change="onChange" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import TablaInflacion from '@/components/admin/TablaInflacion.vue';
import { inflacionApi } from '@/api';
import { InflacionMes } from '@/model/InflacionMes';
import { notificationService, routerService } from '@/service';
import { logError } from '@/utils/log';
import { sessionStore, monedaStore } from '@/store';
import { Periodo } from '@/model/Periodo';
import { addYears, parseServerDate } from '@/utils/date';
import { toQuerystringDictionary } from '@/core/ajax/helpers';
import { parseQueryString } from '@/utils/browser';
import { toInt } from '@/utils/general';

/** Filtro para buscar indices de inflacion en el servidor */
interface InflacionFilter extends Periodo {
  moneda: number;
}

/**
 * Pagina de ABM de Indices de inflacion
 */
@Component({ components: { TablaInflacion } })
export default class InflacionView extends Vue {
  /** Indices cargados en este momento */
  private indices: InflacionMes[] = [];

  /** Indica si se esta cargando o guardando los indices del servidor */
  private loading = false;

  private mounted() {
    this.loadItems();
  }

  /**
   * Handler cuando cambia el filtro.
   * Esto puede ocurrir por un cambio en la URL o porque se cargaron las monedas
   */
  @Watch('filter')
  private onFilterChanged() {
    this.loadItems();
  }

  /**
   * Obtiene el filtro de periodo a utilizar.
   * Si "desde" y/o "hasta" estan definidos en la URL, se usan.
   *
   * Si alguno esta vacio:
   * - Si esta dentro de un ejercicio, se usa las fechas de inicio y/o fin del ejercicio.
   * - Si no, se busca hasta el mes actual y desde un anio atras que "hasta"
   */
  private get filter(): Partial<InflacionFilter> {
    // Se obtiene el filtro a partir de la URL
    const qs = parseQueryString(this.$route.query);
    let { desde, hasta, moneda }: Partial<InflacionFilter> = {
      desde: parseServerDate(qs.desde),
      hasta: parseServerDate(qs.hasta),
      moneda: toInt(qs.moneda)
    };

    // Si no hay moneda en la URL se usa la default
    if (!moneda) moneda = this.monedaDefault;

    if (desde && hasta) return { desde, hasta, moneda };

    // En caso de faltar alguna fecha, se completa
    if (sessionStore.ejercicio) {
      if (!desde) desde = sessionStore.ejercicio.inicio;
      if (!hasta) hasta = sessionStore.ejercicio.finalizacion;
    } else {
      if (!hasta) hasta = new Date();
      if (!desde) desde = addYears(hasta, -1);
    }

    return { desde, hasta, moneda };
  }

  /** Obtiene la moneda default */
  private get monedaDefault(): number | undefined {
    return monedaStore.lista.items.find(m => m.default)?.id;
  }

  /** Busca los indices de inflacion en el server */
  private async loadItems() {
    try {
      // Si no hay moneda, no hacer nada.
      // Esto ocurre si la app aun no cargo la default
      if (!this.filter.moneda) return;

      this.loading = true;
      this.indices = await inflacionApi.list({ filter: this.filter });
    } catch (e) {
      logError('buscando indices de inflacion', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }

  /** Handler de cuando cambia el periodo, se navega a la nueva URL */
  private onNewPeriodo(periodo: Periodo) {
    routerService.updateFilter(this.$route, toQuerystringDictionary({ ...periodo, moneda: this.filter.moneda }));
  }

  /** Handler de cuando cambia de moneda a buscar, se navega a la nueva URL */
  private onNewMoneda(moneda: number) {
    routerService.updateFilter(this.$route, toQuerystringDictionary({ ...this.filter, moneda }));
  }

  /** Handler cuando cambia algun indice, se guarda en el server */
  private async onChange(indice: InflacionMes) {
    try {
      this.loading = true;
      // Se guarda el indice
      await inflacionApi.save(indice);

      // Se refresca la lista
      await this.loadItems();
    } catch (e) {
      logError('guardando indice de inflacion', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }
}
</script>

<style lang="scss" scoped></style>
