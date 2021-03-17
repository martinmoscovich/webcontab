<template>
  <div>
    <article class="media card pa-2 selectable card-list-item" @click="onClick" :class="{ expanded: isExpanded }">
      <!-- Cuenta  -->
      <div class="media-content ml-1">
        <div class="content">
          <div class="asiento-info mb-1">
            <p class="vertical-center mb-0">
              <strong>{{ item.numero }}.</strong> <small class="ml-1">{{ item.detalle }}</small>
            </p>
            <div class="has-text-right vertical-center">
              <small>{{ date }}</small>
              <a href="" class="pa-1" @click.stop.prevent="onExpand"><b-icon :icon="expandIcon" class="mr-1 ml-1"/></a>
            </div>
          </div>

          <template v-if="isExpanded">
            <ItemImputacionMobile
              v-for="imputacion in item.imputaciones"
              :key="imputacion.id"
              :item="imputacion"
              :cuenta="imputacion.cuenta"
            />
          </template>
        </div>
      </div>
    </article>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { AsientoDTO } from '@/model/AsientoDTO';
import { formatDate } from '@/utils/date';
import ItemImputacionMobile from './ItemImputacionMobile.vue';

/** Item de asiento para mobile */
@Component({ components: { ItemImputacionMobile } })
export default class ItemAsientoMobile extends Vue {
  @Prop()
  item: AsientoDTO;

  @Prop()
  expand: boolean;

  isExpanded = false;

  /** Handler cuando cambia la Prop de expand */
  @Watch('expand')
  private onExpandChange() {
    this.isExpanded = this.expand;
  }

  private get expandIcon() {
    return this.isExpanded ? 'chevron-down' : 'chevron-right';
  }

  /** Handler cuando se hace click en un item */
  private onClick() {
    this.$emit('click', this.item);
  }

  /** Fecha formateada */
  private get date() {
    return formatDate(this.item.fecha);
  }

  private onExpand() {
    this.isExpanded = !this.isExpanded;
  }
}
</script>

<style lang="scss" scoped>
.expanded {
  background-color: #d1dfe6;
}
.content {
  margin-bottom: 2px !important;
}
.asiento-info {
  display: flex;
  justify-content: space-between;
}
.vertical-center {
  display: flex;
  align-items: center;
}
</style>
