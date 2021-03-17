<template>
  <ListaImputacionesMobile
    v-if="isMobile"
    :imputacionesConSaldo="imputacionesConSaldo"
    :saldoAnterior="saldoAnterior"
    :loading="loading"
    :moneda="moneda"
    @itemClick="onItemClick"
  />
  <TablaImputaciones
    v-else
    :imputacionesConSaldo="imputacionesConSaldo"
    :saldoAnterior="saldoAnterior"
    :loading="loading"
    :moneda="moneda"
    @itemClick="onItemClick"
  />
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import TablaImputaciones from './TablaImputaciones.vue';
import ListaImputacionesMobile from './ListaImputacionesMobile.vue';
import Page from '@/core/Page';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { twoDecimals } from '@/utils/currency';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import { isDefined } from '@/utils/general';
import { Moneda } from '@/model/Moneda';

/**
 * Listado de Mayor.
 * Este componente se encarga de la logica de obtener los saldos parciales.
 */
@Component({
  components: { ListaImputacionesMobile, TablaImputaciones }
})
export default class ListaMayor extends Vue {
  /** Pagina actual de imputaciones */
  @Prop()
  page: Page<ImputacionDTO>;

  /** Saldo anterior a la primer imputacion de la pagina actual */
  @Prop()
  saldoAnterior: number;

  /** Indica si esta cargando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Moneda de la cuenta */
  @Prop()
  moneda: Moneda;

  @Prop()
  isMobile: boolean;

  /** Indica si se muestra el saldo anterior en la primera fila */
  private useSaldoAnterior = true;

  /** Recorre la pagina de imputaciones y genera el saldo parcial por cada una */
  private get imputacionesConSaldo(): ImputacionSaldo[] {
    if (this.page?.items?.length === 0) return [];

    // Si se usa el saldo anterior y esta definido, arranca de dicho saldo.
    // Si no, arranca de 0
    let saldo = this.useSaldoAnterior && this.saldoAnterior ? this.saldoAnterior : 0;

    // Se va acumulando el saldo en cada item y se genera un item con la imputacion y el saldo parcial
    const items = this.page.items.map(imputacion => {
      saldo = twoDecimals(saldo + (imputacion.importe ?? 0));
      return { imputacion, saldo };
    });

    return items;
  }

  /** Indica si hay saldo anterior */
  private get hasSaldoAnterior() {
    return isDefined(this.saldoAnterior) && this.saldoAnterior !== 0;
  }

  /** Handler cuando se selecciona una imputacion */
  private onItemClick(item: ImputacionDTO) {
    this.$emit('itemClick', item);
  }
}
</script>

<style lang="scss">
.imputacion-row {
  cursor: pointer;
}
</style>
