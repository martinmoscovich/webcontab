<template>
  <CardComponentWithActions title="Organizaciones" icon="ballot" dense list>
    <template v-slot:actions>
      <!-- Agregar -->
      <b-tooltip label="Nueva organizacion" position="is-bottom" type="is-info">
        <b-button icon-left="plus" @click="onNewClick" />
      </b-tooltip>
    </template>

    <!-- Item -->
    <OrganizacionItem
      v-for="org in items"
      :key="org.id || org.cuit"
      :organizacion="org"
      @click="onSelected"
      @selected="onEnter"
    />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import OrganizacionItem from './OrganizacionItem.vue';
import { Organizacion } from '@/model/Organizacion';

/**
 * Lista de organizaciones
 */
@Component({ components: { OrganizacionItem } })
export default class OrganizacionesList extends Vue {
  @Prop()
  items: Organizacion[];

  /** Handler cuando se hace click en new */
  private onNewClick() {
    this.$emit('new');
  }

  /** Handler cuando se selecciona una organizacion */
  private onSelected(organizacion: Organizacion) {
    this.$emit('selected', organizacion);
  }
  /** Handler cuando se desea entrar en una organizacion */
  private onEnter(organizacion: Organizacion) {
    this.$emit('enter', organizacion);
  }
}
</script>

<style lang="stylus" scoped></style>
