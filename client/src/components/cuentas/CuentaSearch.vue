<template>
  <b-autocomplete
    ref="autocomplete"
    :data="results"
    :value="textValue"
    :placeholder="placeholder"
    field="descripcion"
    :size="small ? 'is-small' : undefined"
    :clearable="!readonly && !disabled"
    keep-first
    :clear-on-select="clearOnSelect"
    :loading="isFetching"
    :readonly="readonly"
    :disabled="disabled"
    :required="required"
    @blur="$emit('blur')"
    @typing="onType"
    @select="onSelected"
    @input="onInput"
    @keydown.native="onKeydown"
  >
    <template v-slot="props">
      <!-- Item del Autocomplete -->
      <div class="media">
        <!-- Icono. Si el modo es todos, habra resultados de dos tipos, se usa el icono para diferenciar -->
        <div class="media-left" v-if="modo === 'todos'">
          <b-icon :icon="getIcon(props.option)" custom-size="mdi-24px" size="is-small" />
        </div>
        <div class="media-content">
          <!-- Descripcion -->
          <span v-html="getHighlight(props.option.descripcion)" />

          <!-- Codigo -->
          <small class="has-text-grey-light">&nbsp;&nbsp;[<span v-html="getHighlight(props.option.codigo)" />]</small>
          <br />
          <!-- Path -->
          <small>
            <span
              v-for="cat in props.option.path"
              :key="cat.id"
              class="has-text-grey-light cat-item"
              v-html="getHighlight(cat.name)"
            >
            </span>
          </small>
        </div>
      </div>
    </template>
  </b-autocomplete>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria, sorterPorText } from '@/model/Cuenta';
import { focusOnNext, Focusable } from '@/utils/browser';
import { cuentaStore } from '@/store';
import { cuentaApi, categoriaApi } from '@/api';
import { isNotNullOrUndefined } from '@/utils/general';

const DEBOUNCE_TIME = 300;

/** Autocomplete de Categorias y/o Cuentas */
@Component
export default class CuentaSearch extends Vue {
  /** Categoria/Cuenta seleccionada */
  @Prop({ type: Object, default: () => null })
  value: CuentaOCategoria | null;

  /**
   * Modo.
   * Puede ser busqueda solo de cuentas, solo de categorias o de ambas
   */
  @Prop({ default: 'cuenta' })
  modo: 'cuenta' | 'categoria' | 'todos';

  /** Placeholder */
  @Prop({ default: 'Buscar' })
  placeholder: string;

  /** Estilo Compacto */
  @Prop({ type: Boolean })
  small: boolean;

  /** Limpiar el Select al seleccionar */
  @Prop({ type: Boolean })
  clearOnSelect: boolean;

  /** Indica si no debe poder editarse */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si es requerido */
  @Prop({ type: Boolean })
  required: boolean;

  /** Indica si esta deshabilitado */
  @Prop({ type: Boolean })
  disabled: boolean;

  /** Lista de ids a excluir en la busqueda */
  @Prop({ type: Array })
  exclude: number[];

  /** Timer utilizado para hacer Debounce */
  private timer: number | null = null;

  /** Items a mostrar, obtenidos en la ultima busqueda */
  private results: CuentaOCategoria[] = [];

  /** Indica si se estan cargando cuentas */
  private isFetching = false;

  $refs: { autocomplete: Vue & Focusable };

  /** Hace foco en el componente */
  focus() {
    this.$refs.autocomplete?.focus();
  }

  /** Obtiene el texto a mostrar en el Select cuando esta cerrado */
  private get textValue() {
    return this.value?.descripcion;
  }

  /** Obtiene las cuentas y categorias del server a partir del texto ingresado, cacheando los resultados */
  private async getAsyncData(text: string) {
    this.timer = null;
    // Si no hay texto, no hacer la query
    if (text === '' || text.length === 1) {
      this.results = [];
      return;
    }
    this.isFetching = true;
    try {
      // Si los parametros de busqueda son los de la ultima query cacheada, se usa esa,
      // si no, se vuelve a buscar
      if (cuentaStore.lastSearch.query.text !== text || cuentaStore.lastSearch.query.mode !== this.modo) {
        let results = await this.makeQuery(text);
        if (this.exclude) {
          // Se quitan las cuentas que estan dentro de las excluidas
          results = results.filter(cuenta => !this.exclude.includes(cuenta.id));
        }

        // Se cachea el resultado en el store
        cuentaStore.searchSuccess({
          query: { text, mode: this.modo },
          results
        });
      }

      // Se usan los resultados del store (ya sea cacheada o recien buscada)
      this.results = cuentaStore.lastSearch.results
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        .map(id => cuentaStore.find(id)!)
        .filter(isNotNullOrUndefined)
        .sort(sorterPorText(text));
    } catch (e) {
      cuentaStore.searchError(e);
    } finally {
      this.isFetching = false;
    }
  }

  /** Realiza la query correspondiente en el servidor segun el modo activo */
  private makeQuery(text: string): Promise<CuentaOCategoria[]> {
    switch (this.modo) {
      case 'cuenta':
        return cuentaApi.search(text);
      case 'todos':
        return cuentaApi.searchWithCategories(text);
      case 'categoria':
        return categoriaApi.search(text);
    }
  }

  /** Obtiene el icono a mostrar en caso de mezclar cuentas y categorias */
  private getIcon(item: CuentaOCategoria) {
    return item.imputable ? 'file' : 'folder';
  }

  /** Genera un codigo HTML que resalta dentro del texto, el termino buscado */
  private getHighlight(text: string) {
    const query = cuentaStore.lastSearch.query.text;
    if (!query) return text;

    // Regex que genera el HTML resaltado
    const r = text.replace(new RegExp(query, 'gi'), match => `<strong class="has-text-danger">${match}</strong>`);
    return r;
  }

  /**
   * Handler cuando se tipea en el select.
   *
   * Aplica la logica de Debounce con un Timer, lo que hace que nunca se ejecuten 2 queries en el tiempo indicado
   */
  private onType(text: string) {
    // Si existe el timer, se cancela
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }

    // Se genera un nuevo timer con el tiempo indicado
    this.timer = setTimeout(() => this.getAsyncData(text), DEBOUNCE_TIME);
  }

  /** Handler cuando se selecciona una cuenta o Categoria */
  private onSelected(option: CuentaOCategoria | null) {
    this.$emit('input', option);
  }

  /** Handler cuando se modifica el valor buscado en el Autocomplete */
  private onInput(value: string) {
    if (!value || value === '') this.onSelected(null);
  }

  /**
   * Handler cuando se presiona una tecla en el Autocomplete.
   * Se usa para usar el ENTER como si fuera TAB
   */
  private onKeydown(e: KeyboardEvent) {
    if (e.keyCode === 13) {
      if (this.value) focusOnNext();
    }
  }
}
</script>

<style lang="scss" scoped>
.cat-item:not(:first-child)::before {
  content: '≻';
}
</style>
