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
  >
    <template v-slot="props">
      <!-- Item usuario -->
      <div class="media">
        <div class="media-left">
          <b-icon icon="account" custom-size="mdi-24px" size="is-small" />
        </div>
        <div class="media-content">
          {{ props.option.name }}
          <br />
          <small>
            <span class="has-text-grey-light usuario-item">
              {{ props.option.username }}
            </span>
          </small>
        </div>
      </div>
    </template>
  </b-autocomplete>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Focusable } from '@/utils/browser';
import { userApi } from '@/api';
import { User } from '../../model/User';
import { notificationService } from '../../service';
import { logError } from '../../utils/log';

const DEBOUNCE_TIME = 300;

/** Autocomplete de busqueda de usuarios */
@Component
export default class UsuarioSearch extends Vue {
  /** Usuario seleccionado */
  @Prop({ type: Object, default: () => null })
  value: User | null;

  @Prop({ default: 'Buscar' })
  placeholder: string;

  /** Input reducido */
  @Prop({ type: Boolean })
  small: boolean;

  /** Limpiar luego de seleccionar */
  @Prop({ type: Boolean })
  clearOnSelect: boolean;

  /** No permitir edicion */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Campo requerido */
  @Prop({ type: Boolean })
  required: boolean;

  @Prop({ type: Boolean })
  disabled: boolean;

  /** Lista de Ids de usuarios a excluir de los resultados */
  @Prop({ type: Array })
  exclude: number[];

  /** Resultado de la ultima busqueda */
  private results: User[] = [];

  /** Indica que se estan buscando usuario en el server */
  private isFetching = false;

  /** Timer utilizado para Debounce */
  private timer: number | null = null;

  $refs: { autocomplete: Vue & Focusable };

  /** Pone el foco en el autocomplete */
  focus() {
    this.$refs.autocomplete?.focus();
  }

  /** Texto a mostrar en el autocomplete (el nombre del usuario) */
  private get textValue() {
    return this.value?.name;
  }

  /**
   * Handler cuando se tipea en el autocomplete
   * Contiene logica de debounce, esperando 300ms antes de realizar la busqueda
   */
  private onType(text: string) {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => this.search(text), DEBOUNCE_TIME);
  }

  /** Realiza la busqueda en el server */
  private async search(text: string) {
    // Si no hay texto o tiene 1 solo caracter, no buscar nada
    if (text === '' || text.length === 1) {
      this.results = [];
      return;
    }
    this.isFetching = true;
    try {
      // Se usan los resultados del store (ya sea cacheada o recien buscada)
      const results = await userApi.search(text);
      if (this.exclude) {
        // Se filtran los ids excluidos
        this.results = results.filter(user => !this.exclude.includes(user.id));
      }
    } catch (e) {
      logError('buscando usuarios', e);
      notificationService.error(e);
    } finally {
      this.isFetching = false;
    }
  }

  /** Handler cuando se selecciona un usuario */
  private onSelected(option: User | null) {
    this.$emit('input', option);
  }

  /** Handler cuando cambia el contenido del autocomplete */
  private onInput(value: string) {
    if (!value || value === '') this.onSelected(null);
  }
}
</script>

<style lang="scss" scoped>
.usuario-item:not(:first-child)::before {
  content: '≻';
}
</style>
