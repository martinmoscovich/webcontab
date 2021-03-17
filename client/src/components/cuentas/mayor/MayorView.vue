<template>
  <div>
    <div class="columns ma-0" style="align-items: center">
      <div class="column py-0">
        <!-- Filtro de asientos -->
        <FiltroAsientos
          :value="filter"
          @input="onNewFilter"
          :minDate="ejercicio ? ejercicio.inicio : null"
          :maxDate="ejercicio ? ejercicio.finalizacion : null"
        />
      </div>
      <div :class="{ 'mb-2': isMobile }">
        <!-- Paginacion en la parte superior -->
        <b-pagination
          v-if="imputaciones.number > 1 || imputaciones.next"
          :class="{ mobile: isMobile }"
          :total="imputaciones.total"
          :current="imputaciones.number"
          :per-page="pageSize"
          @change="onPageRequest"
        />
      </div>
    </div>

    <div class="card">
      <!-- Imputaciones -->
      <ListaMayor
        :isMobile="isMobile"
        :page="imputaciones"
        :saldoAnterior="imputaciones.saldoAnterior"
        :moneda="moneda"
        :loading="loading"
        @itemClick="onImputacionClick"
      />
    </div>
    <!-- Paginador -->
    <b-pagination
      v-if="imputaciones.number > 1 || imputaciones.next"
      :class="{ mobile: isMobile }"
      :total="imputaciones.total"
      :current="imputaciones.number"
      :per-page="pageSize"
      @change="onPageRequest"
    />
  </div>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { AsientosSearchFilter } from '@/api/AsientoApi';
import { Ejercicio } from '@/model/Ejercicio';
import { Moneda } from '@/model/Moneda';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import { ImputacionesCuenta } from '@/model/ImputacionesCuenta';
import ListaMayor from './ListaMayor.vue';
import FiltroAsientos from '@/components/asientos/FiltroAsientos.vue';
import { uiStore } from '@/store';

/**
 * Componente que muestra el Mayor de una cuenta (Imputaciones)
 */
@Component({
  components: { FiltroAsientos, ListaMayor }
})
export default class MayorView extends Vue {
  /** Ejercicio actual */
  @Prop()
  ejercicio: Ejercicio;

  /** Pagina actual de imputaciones y saldo anterior */
  @Prop()
  imputaciones: ImputacionesCuenta;

  /** Filtro de asientos actual */
  @Prop()
  filter: AsientosSearchFilter;

  /** Moneda de la cuenta */
  @Prop()
  moneda: Moneda;

  /** Indica si esta cargando */
  @Prop()
  loading: boolean;

  /** Tamanio de la pagina, utilizado por el paginador */
  @Prop()
  pageSize: number;

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }

  /** Handler cuando se pide otra pagina */
  private onPageRequest(page: number) {
    this.$emit('pageRequest', page);
  }

  /** Handler cuando se selecciona una imputacion */
  private onImputacionClick(item: ImputacionDTO) {
    this.$emit('imputacionClick', item);
  }

  /** Handler cuando se modifica el Filtro */
  private onNewFilter(filter: AsientosSearchFilter) {
    this.$emit('filterChange', filter);
  }
}
</script>

<style lang="scss" scoped>
.card {
  padding: 5px;
}
</style>
