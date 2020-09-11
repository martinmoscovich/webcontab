<template>
  <CardComponentWithActions title="Cuentas" icon="file" dense>
    <template v-slot:actions>
      <!-- Autocomplete de categorias y cuentas -->
      <CuentaSearch modo="todos" clearOnSelect @input="onCuentaSelected" :exclude="cuentasIds" />
    </template>
    <!-- Lista de categorias y cuentas incluidas -->
    <SeleccionCuentasList :cuentas="cuentas" class="pa-2" @eliminar="onQuitar" />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import SeleccionCuentasList from './SeleccionCuentasList.vue';
import { CuentaOCategoria } from '@/model/Cuenta';

/**
 * Componente que permite manejar una lista editable de categorias y cuentas.
 *
 * Permite buscarlas con autocomplete y agregarlas a una lista.
 * Luego se pueden quitar de dicha lista
 */
@Component({
  components: { SeleccionCuentasList }
})
export default class CardSeleccionCuentas extends Vue {
  /** Categorias y cuentas incluidas */
  @Prop()
  cuentas: CuentaOCategoria[];

  /** Ids de las categorias y cuentas incluidas */
  private get cuentasIds() {
    return this.cuentas.map(c => c.id);
  }

  /** Handler cuando se quiere quitar una categoria o cuenta de la lista */
  private async onQuitar(item: CuentaOCategoria) {
    this.$emit('remove', item);
    // this.value = this.cuentas.filter(c => c.id !== item.id);
  }

  /** Handler cuando se selecciona una categoria o cuenta para agregar */
  private onCuentaSelected(item: CuentaOCategoria) {
    if (item) this.$emit('add', item);
  }
}
</script>
