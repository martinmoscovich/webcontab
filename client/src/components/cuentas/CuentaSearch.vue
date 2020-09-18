<template>
  <b-autocomplete
    ref="autocomplete"
    class="cuenta-search"
    :style="'--loader-height: ' + loaderHeight"
    :data="results.items"
    :value="textValue"
    :placeholder="placeholder"
    field="descripcion"
    :size="small ? 'is-small' : undefined"
    :clearable="!readonly && !disabled"
    keep-first
    :check-infinite-scroll="true"
    :clear-on-select="clearOnSelect"
    :loading="isFetching"
    :readonly="readonly"
    :disabled="disabled"
    :required="required"
    @blur="$emit('blur')"
    @typing="onType"
    @infinite-scroll="onMore"
    @select="onSelected"
    @input="onInput"
    @keydown.native="onKeydown"
  >
    <template v-slot="props">
      <!-- Item del Autocomplete -->
      <div class="media">
        <!-- Icono. Si el modo es todos, habra resultados de dos tipos, se usa el icono para diferenciar -->
        <div class="media-left icon-container" v-if="modo === 'todos'">
          <b-icon :icon="getIcon(props.option)" custom-size="mdi-24px" size="is-small" class="has-text-grey" />
        </div>
        <div class="media-content">
          <!-- Descripcion -->
          <span v-html="getHighlight(props.option.descripcion)" class="has-text-weight-semibold" />

          <!-- Codigo -->
          <small class="has-text-grey">&nbsp;&nbsp;[<span v-html="getHighlight(props.option.codigo)" />]</small>

          <!-- Alias (si matcheo) -->
          <template v-if="isAliasMatch(props.option)">
            <br />
            <span class="has-text-grey">ALIAS: <span v-html="getHighlight(props.option.alias)"/></span>
          </template>
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
    <template v-if="hasNextPage" slot="footer">
      <b-loading :is-full-page="false" :active="isFetchingPage" />
    </template>
  </b-autocomplete>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { CuentaOCategoria, Cuenta } from '@/model/Cuenta';
import { focusOnNext, Focusable } from '@/utils/browser';
import { cuentaStore } from '@/store';
import { cuentaApi, categoriaApi } from '@/api';
import { isNotNullOrUndefined } from '@/utils/general';
import Page, { createPage } from '@/core/Page';
import { InfiniteListState, getInitialInfiniteListState, infiniteListSuccess } from '@/core/ui/state/list';

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

  /** Ultimo texto buscado */
  private text: string | null = null;

  /** Items a mostrar, obtenidos en la ultima busqueda */
  private results: InfiniteListState<CuentaOCategoria> = getInitialInfiniteListState();

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

  /** Indica si hay siguiente pagina */
  private get hasNextPage() {
    return this.results.hasNext;
  }

  /**
   * Indica que se esta cargando una pagina (no la primera).
   * Util para mostrar el loader inferior.
   */
  private get isFetchingPage() {
    return this.isFetching && this.results.lastPage > 0;
  }

  /** Altura del loader. Solo tiene altura cuando se carga una nueva pagina */
  private get loaderHeight() {
    return this.isFetchingPage ? '42px' : '0px';
  }

  /** Obtiene las cuentas y categorias del server a partir del texto ingresado, cacheando los resultados */
  private async getAsyncData(text: string, pageNumber?: number) {
    this.timer = null;
    // Para menos de 2 caracteres no se busca
    if (text.length < 2) {
      this.results = getInitialInfiniteListState();
      return;
    }
    this.isFetching = true;
    try {
      // Si los parametros de busqueda son los de la ultima query cacheada, se usa esa,
      // si no, se vuelve a buscar
      const sameQuery = cuentaStore.lastSearch.query.text === text && cuentaStore.lastSearch.query.mode === this.modo;
      const samePage = cuentaStore.lastSearch.page.number === (pageNumber ?? 1);
      if (!sameQuery || !samePage) {
        // Si cambio la query, se reinicia la lista
        if (!sameQuery) this.results = getInitialInfiniteListState();

        // Se cachea el resultado en el store
        cuentaStore.searchSuccess({ query: { text, mode: this.modo }, page: await this.makeQuery(text, pageNumber) });
      }

      // Se guarda el texto buscado (por si hay paginacion)
      this.text = text;

      // Se usan los resultados del store (ya sea cacheada o recien buscada)
      const page = cuentaStore.lastSearch.page;

      // Se quitan las cuentas que estan dentro de las excluidas
      const items = this.exclude ? page.items.filter(cuentaId => !this.exclude.includes(cuentaId)) : page.items;

      // Actualiza el listado, agregando los nuevos items
      infiniteListSuccess(
        this.results,
        {
          ...page,
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          items: page.items.map(id => cuentaStore.find(id)!).filter(isNotNullOrUndefined),
          //Se actualiza el size por si se filtro la lista con exluidos
          size: items.length
        },
        'more'
      );
    } catch (e) {
      cuentaStore.searchError(e);
    } finally {
      this.isFetching = false;
    }
  }

  /** Realiza la query correspondiente en el servidor segun el modo activo */
  private makeQuery(text: string, page?: number): Promise<Page<CuentaOCategoria>> {
    switch (this.modo) {
      case 'cuenta':
        return cuentaApi.search(text, page);
      case 'todos':
        return cuentaApi.searchWithCategories(text, page);
      case 'categoria':
        return categoriaApi.search(text).then(createPage);
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

  /** Indica si el item indica matcheo por alias */
  private isAliasMatch(item: Cuenta) {
    if (!this.text) return false;
    return item.alias?.includes(this.text);
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

  /** Handler cuando se pide otra pagina */
  private onMore() {
    if (this.text && this.hasNextPage) {
      this.getAsyncData(this.text, this.results.lastPage + 1);
    }
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

<style scoped>
.cuenta-search >>> div.dropdown-item {
  height: var(--loader-height);
}
.cuenta-search >>> .loading-icon::after {
  border-color: #6d98ef;
  border-right-color: transparent;
  border-top-color: transparent;
}
</style>
<style lang="scss" scoped>
.cat-item:not(:first-child)::before {
  content: '≻';
}
.icon-container {
  margin-top: 4px;
}
</style>
