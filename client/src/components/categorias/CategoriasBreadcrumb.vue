<template>
  <nav class="breadcrumb" aria-label="breadcrumbs">
    <ul>
      <!-- 
        Raiz (icono casita)
        Si estamos en el primer nivel no se muestra 
      -->
      <li :class="{ 'is-invisible': isRoot }">
        <a @click="onItemClick(null)"><b-icon icon="home"/></a>
      </li>

      <!-- Nombres de las categorias del path -->
      <template v-if="cuenta.path">
        <li v-if="isMobile && pathToLong">
          <span style="padding: 0 0.5em;">...</span>
        </li>

        <li v-for="item in path" :key="item.id">
          <a @click="onItemClick(item)">{{ item.name }}</a>
        </li>
      </template>

      <!-- Nombre (editable) de la categoria / cuenta final -->
      <li v-if="!isRoot" class="is-active">
        <span style="margin-left: 12px" />
        <span v-if="readonly">{{ cuenta.descripcion }}</span>
        <EditableLabel v-else :value="cuenta.descripcion" @input="onNameChange" />

        <!-- Si es una cuenta imputable se agrega el codigo al final -->
        <small v-if="cuenta.imputable" class="has-text-grey-light is-size-6 ml-2"> [{{ cuenta.codigo }}]</small>
      </li>
    </ul>
  </nav>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria } from '@/model/Cuenta';
import { IdNameModel } from '@/model/IdModel';
import EditableLabel from '@/components/common/EditableLabel';
import { uiStore } from '@/store';

/** Breadcrumb de Categoria / Cuenta */
@Component({
  components: { EditableLabel }
})
export default class CategoriasBreadcrumb extends Vue {
  /** Categoria / Cuenta */
  @Prop()
  cuenta: CuentaOCategoria;

  /** Indica si no se puede modificar el nombre */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }

  private get pathToLong() {
    if (!this.cuenta.path) return false;
    return this.cuenta.path.length >= 2;
  }

  private get path() {
    if (!this.cuenta.path || this.cuenta.path.length === 0) return [];
    if (!this.isMobile || this.cuenta.path.length < 2) return this.cuenta.path;

    return [this.cuenta.path[this.cuenta.path.length - 1]];
  }

  /** Indica si la categoria es raiz */
  private get isRoot() {
    return this.cuenta?.id === null;
  }

  /** Handler cuando se selecciona una categoria del breadcrumb */
  private onItemClick(item: IdNameModel | null) {
    this.$emit('categoriaSelected', item);
  }

  /** Handler cuando se modifica el nombre de la categoria / cuenta */
  onNameChange(value: string) {
    this.$emit('edit', value);
  }
}
</script>

<style lang="scss"></style>
