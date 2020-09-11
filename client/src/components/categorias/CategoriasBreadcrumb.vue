<template>
  <nav class="breadcrumb" aria-label="breadcrumbs">
    <ul>
      <!-- Raiz (icono casita) -->
      <li>
        <a v-if="!cuenta.path || cuenta.path.length > 0" @click="onItemClick(null)"><b-icon icon="home"/></a>
        <b-icon v-else icon="home" />
      </li>

      <!-- Nombres de las categorias del path -->
      <template v-if="cuenta.path">
        <li v-for="item in cuenta.path" :key="item.id">
          <a @click="onItemClick(item)">{{ item.name }}</a>
        </li>
      </template>

      <!-- Nombre (editable) de la categoria / cuenta final -->
      <li v-if="!isRoot" class="is-active">
        <span style="margin-left: 12px" />
        <span v-if="readonly">{{ cuenta.descripcion }}</span>
        <EditableLabel v-else :value="cuenta.descripcion" @input="onNameChange" />
      </li>
    </ul>
  </nav>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria } from '@/model/Cuenta';
import { IdNameModel } from '@/model/IdModel';
import EditableLabel from '@/components/common/EditableLabel';

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
