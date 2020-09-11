import { Component, Prop, Vue } from 'vue-property-decorator';
import { CreateElement } from 'vue/types/umd';

/**
 * Field para un Input que maneja los errores de validacion
 */
@Component
export default class Field extends Vue {
  /** Lista de errores de validacion */
  @Prop({ required: false, default: () => [] })
  v: string[] | null;

  /** Indica si se deben mostrar los errores o solo poner en rojo el campo */
  @Prop({ type: Boolean })
  hideErrorMessage: boolean;

  onBlur() {
    this.$emit('input', this.$el.textContent);
  }

  render(h: CreateElement) {
    const props = { ...this.$props };

    // Se muestra solo el primer error
    if (this.v?.[0]) {
      props.message = this.hideErrorMessage ? null : this.v?.[0];
    }

    return h(
      'b-field',
      {
        props,
        on: this.$listeners,
        attrs: this.$attrs,
        class: { 'has-error': this.v?.[0] }
      },
      this.$slots.default
    );
  }
}
