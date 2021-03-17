<template>
  <section class="section is-main-section" v-if="cuenta">
    <!-- Toolbar -->
    <PageNavBar cuentaSearch :title="cuenta.descripcion">
      <ToolbarExcelButton v-if="cuentaId && !loading && imputaciones.items.length > 0" :url="exportUrl" />
    </PageNavBar>

    <!-- Breadcrumb de categorias -->
    <div class="columns">
      <div class="column">
        <CategoriaHeader :item="cuenta" />
      </div>

      <!-- Tabs para cambiar entre mayor y ABM -->
      <div class="tabs column is-right" v-if="mostrarMayor">
        <ul style="border: none">
          <li :class="{ 'is-active': activeSection === 'mayor' }">
            <a @click="activeSection = 'mayor'">Imputaciones</a>
          </li>
          <li :class="{ 'is-active': activeSection === 'form' }">
            <a @click="activeSection = 'form'">
              {{ readonly ? 'Datos' : 'Editar' }}
            </a>
          </li>
        </ul>
      </div>
    </div>

    <!-- Mayor de la cuenta -->
    <MayorView
      v-if="activeSection === 'mayor'"
      :ejercicio="ejercicio"
      :imputaciones="imputaciones"
      :moneda="moneda"
      :filter="query.filter"
      :loading="loading"
      :pageSize="PAGE_SIZE"
      @pageRequest="onPageRequest"
      @imputacionClick="onImputacionClick"
      @filterChange="onNewFilter"
    />

    <!-- Form de Modificacion y Baja -->
    <div v-else-if="activeSection === 'form'" class="columns is-centered">
      <CuentaForm
        :value="cuenta"
        :readonly="readonly"
        @save="onCuentaSave"
        @delete="onCuentaDelete"
        class="column is-half-fullhd is-three-quarters-widescreen is-four-fifths-tablet form-card "
      />
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import { Cuenta } from '@/model/Cuenta';
import { informeApi } from '@/api';
import { emptyPage } from '../core/Page';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import CuentaForm from '@/components/cuentas/CuentaForm.vue';
import MayorView from '@/components/cuentas/mayor/MayorView.vue';
import CategoriaHeader from '@/components/categorias/CategoriaHeader.vue';
import { routerService, notificationService } from '../service';
import { sessionStore, cuentaStore, monedaStore } from '../store';
import { AsientosSearchFilter, AsientosSearchOptions } from '../api/AsientoApi';
import { parseServerDate } from '../utils/date';
import { toInt } from '../utils/general';
import { ImputacionesCuenta } from '../model/ImputacionesCuenta';
import { toQuerystringDictionary } from '@/core/ajax/helpers';

/**
 * Pagina de Cuenta.
 * Incluye el mayor y el form ABM
 */
@Component({
  components: {
    CategoriaHeader,
    MayorView,
    CuentaForm
  }
})
export default class CuentaView extends Vue {
  /**
   * Indica si se esta leyendo la cuenta del server.
   * Se usa para que la carga pueda incluir tambien al mayor (son 2 requests).
   */
  readingCuenta = false;

  /** Id extraido de la URL */
  cuentaId: number | null = null;

  /** Imputaciones de la cuenta (para el mayor) */
  imputaciones: ImputacionesCuenta = { ...emptyPage(), saldoAnterior: 0 };

  /** Indica si se muestra el mayor o el form de ABM */
  activeSection: 'mayor' | 'form' = this.mostrarMayor ? 'mayor' : 'form';

  PAGE_SIZE = 40;

  private mounted() {
    this.loadCuenta();
  }

  @Watch('$route')
  private onRouteChange() {
    this.loadCuenta();
  }

  /** Indica si se esta cargando una cuenta y su mayor o bien si se esta actualizando o borrando la cuenta */
  private get loading() {
    return cuentaStore.cuentas.status.loading || this.readingCuenta;
  }

  /** Indica si el usuario solo tiene rol de lectura (no puede modificar) */
  private get readonly() {
    return sessionStore.readonly;
  }

  /** Indica si se debe mostrar el mayor */
  private get mostrarMayor() {
    return sessionStore.enEjercicio;
  }

  private get ejercicio() {
    return sessionStore.ejercicio;
  }

  /** DTO de la cuenta, obtenida de la cache */
  private get cuenta() {
    return this.cuentaId ? cuentaStore.findCuenta(this.cuentaId) : null;
  }

  /** DTO de la moneda, obtenida de la cache */
  private get moneda() {
    if (!this.cuenta || !this.cuenta.monedaId) return undefined;
    return monedaStore.find(this.cuenta.monedaId);
  }

  /** URL del Reporte Mayor de la cuenta en Excel */
  private get exportUrl() {
    if (!this.cuentaId) return '';
    return informeApi.getExportarMayorUrl({ cuentas: [this.cuentaId] }, this.query.filter);
  }

  /** Carga una cuenta del server a partir del ID de la URL */
  private async loadCuenta() {
    try {
      this.readingCuenta = true;

      // Carga la cuenta
      this.cuentaId = parseInt(this.$route.params.id, 10);
      if (this.cuentaId) {
        cuentaStore.findCuentaById({
          id: this.cuentaId,
          refresh: true,
          path: true
        });

        // Si se debe mostrar el mayor, se cargan las imputaciones
        if (this.mostrarMayor) {
          this.imputaciones = await informeApi.getMayor(this.cuentaId, this.query);
          window.scrollTo(0, 0);
        }
      }
    } finally {
      this.readingCuenta = false;
    }
  }

  /** Handler de cuando cambia el filtro, se navega a la nueva URL */
  private onNewFilter(filter: AsientosSearchFilter) {
    routerService.updateFilter(this.$route, toQuerystringDictionary(filter));
  }

  /** Handler de cuando cambia la pagina, se navega a la nueva URL */
  private onPageRequest(page: number) {
    routerService.updatePagination(this.$route, page);
  }

  /**
   * Obtiene el query string a mandar al server a partir de la URL actual
   */
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

  /** Handler de cuando se selecciona una imputacion */
  onImputacionClick(item: ImputacionDTO) {
    routerService.goToImputacion(item);
  }

  /** Handler cuando se actualiza una cuenta */
  private async onCuentaSave(cuenta: Cuenta) {
    if (!cuenta) return;
    await cuentaStore.actualizar(cuenta);
    notificationService.info('La cuenta fue guardada');
  }

  /** Handler cuando se borra una cuenta */
  private async onCuentaDelete(cuenta: Cuenta) {
    if (!cuenta) return;

    // Se guarda el id del padre antes de borrarla
    const categoria = cuenta.categoriaId;

    // Se llama al server
    await cuentaStore.borrar(cuenta);

    // Se navega al padre
    routerService.goToCategoria(categoria ? { id: categoria } : null);
  }
}
</script>

<style lang="scss" scoped>
.form-card {
  background-color: #f2f2f2;
  padding: 1em 2em;
  margin-top: 30px;
}
</style>
