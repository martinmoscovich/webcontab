<template>
  <div>
    <b-loading :is-full-page="true" :active="loading" />
    <ItemAsientoMobile
      v-for="item in page.items"
      :key="item.id"
      :item="item"
      :expand="expandAll"
      @click="onItemClick"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import Page from '@/core/Page';
import ItemAsientoMobile from './ItemAsientoMobile.vue';
import { AsientoDTO } from '@/model/AsientoDTO';
import { routerService } from '@/service';

/** Lista de asientos (diario) para Mobile */
@Component({ components: { ItemAsientoMobile } })
export default class ListaAsientoseMobile extends Vue {
  /** Pagina de asientos actual */
  @Prop()
  page: Page<AsientoDTO>;

  /** Indica que se estan cargando asientos */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Indica si se debe expandir todos los asientos (mostrar sus imputaciones) */
  @Prop({ type: Boolean })
  expandAll: boolean;

  /** Handler cuando se selecciona un asiento */
  private onItemClick(item: AsientoDTO) {
    routerService.goToAsiento(item);
  }
}
</script>

<style lang="scss"></style>
