<template>
  <section class="section is-main-section">
    <!--Toolbar -->
    <PageNavBar title="Inflacion" cuentaSearch />

    <div class="columns is-centered">
      <div class="column is-two-thirds-desktop is-two-fifths-fullhd">
        <template v-if="monedasAjustables.length > 0">
          <!-- Filtro -->
          <div class="columns is-centered">
            <div class="column">
              <b-field grouped>
                <PeriodoInput month :value="filter" @input="onNewPeriodo" />

                <b-field label="Moneda" label-position="on-border">
                  <MonedaSelect :value="filter.moneda" :filterFn="monedaFilter" @input="onNewMoneda" />
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
        </template>

        <!-- Mensaje cuando no hay monedas ajustables -->
        <CardComponent v-else title="Indices de Inflación">
          <div class="has-text-centered is-size-5">No hay monedas ajustables por inflación</div>
        </CardComponent>
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
import { addYears, parseServerDate, isEqualOrBefore, addMonths } from '@/utils/date';
import { toQuerystringDictionary } from '@/core/ajax/helpers';
import { parseQueryString } from '@/utils/browser';
import { toInt } from '@/utils/general';
import { Moneda } from '@/model/Moneda';
import { OptionalId } from '@/core/TypeHelpers';
import { ListItemPredicate } from '@/utils/array';

/** Filtro para buscar indices de inflacion en el servidor */
interface InflacionFilter extends Periodo {
  // Se marca opcional porque si no esta en la URL y aun no se cargo la default, no hay moneda
  // Es temporal hasta que se cargue la default
  moneda?: number;
}

/** Indice de inflacion con el id opcional (segun si esta persistido o no) */
type InflacionIndice = OptionalId<InflacionMes>;

/**
 * Pagina de ABM de Indices de inflacion
 */
@Component({ components: { TablaInflacion } })
export default class InflacionView extends Vue {
  /** Indices cargados en este momento */
  private indices: InflacionIndice[] = [];

  /**
   * Filtro para seleccionar solo las monedas ajustables
   * Se usa en el select de monedas
   */
  private monedaFilter: ListItemPredicate<Moneda> = (moneda: Moneda) => moneda.ajustable;

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
  private get filter(): InflacionFilter {
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

  /** Devuelve las monedas ajustables */
  private get monedasAjustables(): Moneda[] {
    return monedaStore.lista.items.filter(this.monedaFilter);
  }

  /** Obtiene la moneda default */
  private get monedaDefault(): number | undefined {
    // Si no hay ninguna, devuelve undefined
    if (this.monedasAjustables.length === 0) return undefined;

    // Entre las ajustable, devuelve la default si esta o, si no, la primera.
    return (this.monedasAjustables.find(m => m.default) ?? this.monedasAjustables[0]).id;
  }

  /** Busca los indices de inflacion en el server */
  private async loadItems() {
    try {
      // Si no hay moneda, no hacer nada.
      // Esto ocurre si la app aun no cargo la default
      if (!this.filter.moneda) return;

      this.loading = true;
      this.indices = this.completarIndicesFaltantes(await inflacionApi.list({ filter: this.filter }));
    } catch (e) {
      logError('buscando indices de inflacion', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }

  /**
   * Recorre los indice obtenidos del server y completa los "huecos" con meses sin indice,
   * para que el usuario pueda completarlos
   */
  private completarIndicesFaltantes(indices: InflacionMes[]): InflacionIndice[] {
    if (!this.filter.moneda) return indices;

    const result: InflacionIndice[] = [];

    // Se recorren los meses, completando los huecos
    let mes = this.filter.desde;
    while (isEqualOrBefore(mes, this.filter.hasta)) {
      const indice = indices.find(item => item.mes.getTime() === mes.getTime() && item.monedaId === this.filter.moneda);
      result.push(indice ?? { mes, monedaId: this.filter.moneda });

      // Se pasa al siguiente mes
      mes = addMonths(mes, 1);
    }
    return result;
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
  private async onChange(indice: InflacionIndice) {
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
