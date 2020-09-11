<template>
  <nav class="panel">
    <!-- Item -->
    <a class="panel-block" v-for="item in organizaciones" :key="item.id" @click="onItemClick(item)">
      <span class="panel-icon">
        <b-icon icon="office-building" custom-size="mdi-24px" size="is-small" />
      </span>
      {{ item.nombre }}
    </a>

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </nav>
</template>
<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import { Organizacion } from '../../model/Organizacion';
import OrganizacionItem from '@/components/admin/organizacion/OrganizacionItem.vue';
import { organizacionStore } from '../../store';

/** Seleccion de organizacion para la sesion */
@Component({ components: { OrganizacionItem } })
export default class OrganizacionesList extends Vue {
  private mounted() {
    this.load();
  }

  /** Carga la lista de organizaciones para el usuario */
  private load() {
    organizacionStore.list({ refresh: true });
  }

  /** Indica que se estan cargando las organizaciones */
  private get loading() {
    return organizacionStore.lista.status.loading;
  }

  /** Lista de organizaciones */
  private get organizaciones() {
    return organizacionStore.lista.items;
  }

  /** Handler cuando selecciona una organizacion */
  private onItemClick(item: Organizacion) {
    this.$emit('selected', item);
  }
}
</script>

<style lang="scss"></style>
