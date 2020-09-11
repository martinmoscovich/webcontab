<template>
  <b-modal :active="show" @update:active="onHide" trap-focus has-modal-card>
    <CardComponent :title="'Nueva ' + tipo">
      <!-- Form de Cuenta -->
      <CuentaForm v-if="tipo === 'cuenta'" :parent="categoria" isModal @save="onSave" @close="onHide" />

      <!-- Form de Categoria -->
      <CategoriaForm v-if="tipo === 'categoria'" :parent="categoria" isModal @save="onSave" @close="onHide" />
    </CardComponent>
  </b-modal>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { Categoria, CuentaOCategoria } from '@/model/Cuenta';
import CategoriaForm from '@/components/categorias/CategoriaForm.vue';
import CuentaForm from '@/components/cuentas/CuentaForm.vue';

/**
 * Modal para crear una categoria o cuenta
 *
 */
@Component({ components: { CategoriaForm, CuentaForm } })
export default class ModalNuevaCuenta extends Vue {
  /** Indica si se debe mostrar el modal */
  @Prop()
  show: boolean;

  /** Tipo de Entidad a crear */
  @Prop()
  tipo: 'categoria' | 'cuenta';

  /** Categoria padre */
  @Prop()
  categoria: Categoria;

  /** Handler cuando se guarda el Form */
  private onSave(c: CuentaOCategoria) {
    this.$emit('save', c);
  }

  /** Se llama para ocultar el modal */
  private onHide() {
    this.$emit('hide');
  }
}
</script>

<style lang="stylus" scoped></style>
