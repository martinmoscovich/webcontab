<template>
  <router-link :to="link">
    <article class="media card pa-2 selectable card-list-item" @click="onClick">
      <!-- Cuenta  -->
      <div class="media-content ml-1">
        <div class="content">
          <p>
            <router-link :to="cuentaLink"
              ><strong>{{ cuenta.codigo }}</strong></router-link
            >
            <router-link :to="cuentaLink">
              <small class="ml-1">{{ cuenta.descripcion }}</small>
            </router-link>
            <br />
            {{ item.detalle }}
          </p>
        </div>
        <div class="has-text-right" :class="{ 'has-text-danger': item.importe < 0 }">{{ formattedSaldo }}</div>
      </div>
    </article>
  </router-link>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { ImputacionDTO } from '@/model/ImputacionDTO';
import { Cuenta } from '@/model/Cuenta';
import { formatCurrency } from '@/utils/currency';
import { monedaStore } from '@/store';
import { routerService } from '@/service';

/** Item de imputacion para mobile */
@Component
export default class ItemImputacionMobile extends Vue {
  @Prop()
  item: ImputacionDTO;

  @Prop()
  cuenta: Cuenta;

  /** Handler cuando se hace click en un item */
  private onClick() {
    this.$emit('click', this.item);
  }

  /** Devuelve el link a la pagina para ver la imputacion indicada */
  private get link() {
    return routerService.imputacion(this.item);
  }

  /** Devuelve el link a la pagina para ver la cuenta indicada */
  private get cuentaLink() {
    return routerService.cuenta(this.cuenta);
  }

  /** Formatea un monto */
  private get formattedSaldo() {
    return formatCurrency(this.item.importe, monedaStore.find(this.cuenta.monedaId)?.simbolo);
  }
}
</script>

<style lang="scss" scoped>
.content {
  margin-bottom: 2px !important;
}
</style>
