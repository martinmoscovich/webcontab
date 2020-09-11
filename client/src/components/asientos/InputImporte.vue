<template>
  <div class="field">
    <currency-input
      class="input is-small input-currency"
      :class="{ 'has-text-danger': negative }"
      :style="'width: ' + width + 'px'"
      :value="valueText"
      :readonly="readonly"
      :disabled="disabled"
      :tabindex="readonly ? -1 : undefined"
      :currency="{ prefix, suffix: '' }"
      :allow-negative="allowNegative"
      :distraction-free="{
        hideNegligibleDecimalDigits: false,
        hideCurrencySymbol: false,
        hideGroupingSymbol: false
      }"
      @input="onInput"
      @keydown.native="onKeydown"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Input de Importe.
 */
@Component
export default class InputImporte extends Vue {
  /** Valor del importe */
  @Prop()
  value: number;

  /** Indica si no se puede modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si esta deshabilitado */
  @Prop({ type: Boolean })
  disabled: boolean;

  /** Simbolo de la moneda  */
  @Prop({ default: '' })
  moneda: string;

  /** Ancho del input */
  @Prop({ default: 105 })
  width: number;

  @Prop({ type: Boolean })
  allowNegative: boolean;

  /** Texto a mostrar.
   * En caso de ser 0 no muestra nada
   */
  private get valueText() {
    if (this.value === undefined || this.value === null || this.value === 0) {
      return null;
    }
    return this.value;
  }

  /** Prefijo a mostrar */
  private get prefix() {
    if (this.moneda === '') return '';
    return this.moneda + ' ';
  }

  private get negative() {
    return this.value < 0;
  }

  /** Handler cuando cambia el valor del input */
  private onInput(v: string) {
    this.$emit('input', v ?? '0');
  }

  /**
   * Handler cuando se aprieta una tecla en el input.
   * Se usa para notificar solo determinadas teclas, asociadas a numeros o movimientos
   */
  private onKeydown(e: KeyboardEvent) {
    const key = e.keyCode;

    if (
      (!e.shiftKey &&
        !e.altKey &&
        !e.ctrlKey &&
        // numbers
        key >= 48 &&
        key <= 57) ||
      // Numeric keypad
      (key >= 96 && key <= 105) ||
      // comma, period and minus, . on keypad
      key == 190 ||
      key == 188 ||
      // key == 109 ||
      key == 110 ||
      // Backspace and Tab and Enter
      key == 8 ||
      key == 9 ||
      key == 13 ||
      // Home and End
      key == 35 ||
      key == 36 ||
      // left and right arrows
      key == 37 ||
      key == 39 ||
      // Del and Ins
      key == 46 ||
      key == 45 ||
      // minus (if allowed)
      (this.allowNegative && key == 109)
    ) {
      this.$emit('keydown', e);
      return true;
    }

    e.preventDefault();
  }
}
</script>
<style lang="scss">
input.input-currency {
  text-align: right;
}
input.input-currency [disabled] {
  cursor: text;
}
</style>
