<template>
  <Field class="imputacion-field" :class="{ selected }">
    <Field :label="getLabel('Cuenta')" hideErrorMessage :v="validation.allErrors('imputacion.cuenta')">
      <CuentaSearch
        ref="inputCuenta"
        :value="cuenta"
        @input="onCuentaChange"
        placeholder="Ingrese cuenta"
        small
        :style="{ width: cuentaWidth + 'px' }"
        :disabled="readonly"
      />
    </Field>
    <Field :label="getLabel('Detalle')" hideErrorMessage :v="validation.allErrors('imputacion.detalle')">
      <b-input
        size="is-small"
        v-model="imputacion.detalle"
        @input="onDetalleChange"
        @keydown.native="onKeydown"
        :style="{ width: isMobile ? '100px' : '200px' }"
        :disabled="readonly"
      ></b-input>
    </Field>
    <Field
      v-if="isMobile"
      :label="getLabel('Importe')"
      hideErrorMessage
      :v="esDebe ? validation.allErrors('imputacion.importe') : []"
    >
      <InputImporte
        :value="imputacion.importe"
        :moneda="moneda"
        :allowNegative="true"
        :disabled="readonly"
        @input="onDebeChange"
        @keydown="onHaberKeydown"
      />
    </Field>
    <Field
      v-if="!isMobile"
      :label="getLabel('Debe')"
      hideErrorMessage
      :v="esDebe ? validation.allErrors('imputacion.importe') : []"
    >
      <InputImporte
        :value="debe"
        :moneda="moneda"
        @input="onDebeChange"
        @keydown="onDebeKeydown"
        :disabled="readonly"
      />
    </Field>
    <Field
      v-if="!isMobile"
      :label="getLabel('Haber')"
      hideErrorMessage
      :v="esHaber ? validation.allErrors('imputacion.importe') : []"
    >
      <InputImporte
        class="input-haber"
        :value="haber"
        :moneda="moneda"
        @input="onHaberChange"
        @keydown="onHaberKeydown"
        :disabled="readonly"
      />
    </Field>
    <Field :label="getLabel('Saldo')" class="field-saldo">
      <InputImporte readonly :value="item.saldo" :moneda="moneda" :disabled="readonly" />
    </Field>
    <Field v-if="!readonly" :label="getLabel('B')" class="fld-borrar">
      <b-button type="is-danger" size="is-small" tabindex="-1" icon-right="delete" @click="onDelete" />
    </Field>
  </Field>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import InputImporte from './InputImporte.vue';
import { focusOnNext, Focusable, focus } from '@/utils/browser';
import { ImputacionModel } from '@/model/Imputacion';
import { ImputacionSaldo } from '@/model/ImputacionDTO';
import { Cuenta } from '../../model/Cuenta';
import { required, number, field } from '../../utils/validation';
import { isDefined } from '../../utils/general';
import { ValidableVue } from '../../core/ui/elements';
import { monedaStore, uiStore } from '../../store';

/** Form de ABM de una imputacion */
@Component({
  components: { InputImporte },
  validators: {
    'imputacion.cuenta': required,
    'imputacion.detalle': required,
    valorAbsoluto: number({ greaterThan: 0, required: true }),
    // Solo se pone para reset, la validacion se hace con el de arriba
    'imputacion.importe': field
  }
})
export default class ImputacionItem extends Vue implements ValidableVue, Focusable {
  /**
   * Item a ver/editar.
   * Incluye la imputacion y el saldo parcial
   */
  @Prop()
  item: ImputacionSaldo<ImputacionModel>;

  /** Indica si es el primer item */
  @Prop({ type: Boolean })
  isFirst: boolean;

  /** Indica si es el ultimo item */
  @Prop({ type: Boolean })
  isLast: boolean;

  @Prop({ type: Boolean })
  readonly: boolean;

  $refs: { inputCuenta: Vue & Focusable };

  private async mounted() {
    if (this.selected) {
      // Si esta seleccionado, se scrollea para que sea visible
      this.$el.scrollIntoView();
      window.scrollBy({ top: -60 });
    }
  }

  /** Hace foco en el item */
  focus() {
    this.$refs.inputCuenta.focus();
  }

  /** Valida el form del item */
  validate() {
    return this.$validate();
  }

  /** Resetea el form del item */
  reset() {
    this.validation.reset();
  }

  /** Imputacion actual */
  private get imputacion() {
    return this.item?.imputacion;
  }

  /** Cuenta de la imputacion actual */
  private get cuenta() {
    return this.imputacion?.cuenta;
  }

  /** Moneda actual */
  private get moneda() {
    if (!this.cuenta) return '';
    return monedaStore.find(this.cuenta.monedaId)?.simbolo ?? '';
  }

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }

  private get cuentaWidth() {
    if (this.isMobile) return 150;
    return 250;
  }

  /**
   * Indica si este item es el seleccionado, segun la URL.
   * Se utiliza para destacarlo dentro de la lista
   */
  private get selected(): boolean {
    if (!this.$route.query?.imputacion) return false;
    return this.$route.query.imputacion === this.imputacion?.id?.toString();
  }

  /** Handler cuando se modifica la cuenta */
  private onCuentaChange(cuenta: Cuenta) {
    // Si esta readonly, ignorar cambios del autocomplete
    // (puede generar conflicto al borrar filas)
    if (this.readonly) return;
    this.imputacion.cuenta = cuenta;
    this.$emit('input', this.imputacion);
  }

  /**
   * Obtiene el label del input.
   * Solo la primer fila lo tiene, las demas no
   */
  private getLabel(str: string) {
    return this.isFirst ? str : null;
  }

  /** Valor absoluto de la imputacion (si es negativa, se invierte)  */
  private get valorAbsoluto() {
    if (!this.imputacion.importe) return this.imputacion.importe;
    return Math.abs(this.imputacion.importe);
  }

  /** Indica que la imputacion es DEBE */
  private get esDebe() {
    return this.imputacion.importe >= 0;
  }

  /** Indica que la imputacion es HABER */
  private get esHaber() {
    return !this.esDebe;
  }

  /** Obtiene el valor de la imputacion si es DEBE. Null en caso contrario */
  private get debe() {
    return this.esDebe ? this.imputacion.importe : null;
  }

  /** Obtiene el valor de la imputacion si es HABER. Null en caso contrario */
  private get haber() {
    return this.esHaber ? this.valorAbsoluto : null;
  }

  /** Handler para borrar una imputacion */
  private onDelete() {
    this.$emit('delete');
  }

  /** Handler cuando cambia el detalle de la imputacion */
  private onDetalleChange() {
    this.$emit('input', this.imputacion);
  }

  /** Handler cuando cambia el valor del DEBE */
  private onDebeChange(value: string) {
    if (isDefined(value)) {
      // Si esta definido, se parsea y asigna al item local
      const numValue = parseFloat(value);
      this.imputacion.importe = isNaN(numValue) ? 0 : numValue;
    }
    this.$emit('input', this.imputacion);
  }

  /** Handler cuando cambia el valor del HABER */
  private onHaberChange(value: string) {
    if (isDefined(value)) {
      // Si esta definido, se parsea y asigna al item local en negativo
      const numValue = parseFloat(value);
      this.imputacion.importe = (isNaN(numValue) ? 0 : numValue) * -1;
    }
    this.$emit('input', this.imputacion);
  }

  /**
   * Handler cuando se aprieta una tecla en cualquier input de texto.
   * Se utiliza para que el ENTER funcione como un TAB.
   */

  private onKeydown(e: KeyboardEvent) {
    if (e.keyCode === 13) {
      focusOnNext();
    }
  }

  /**
   * Handler cuando se aprieta una tecla en el input de DEBE.
   * Se utiliza para no permitir comas y para pasar al siguiente input al usar ENTER
   */
  private onDebeKeydown(event: KeyboardEvent) {
    if (event.key === ',') {
      event.preventDefault();
    } else {
      this.onKeydown(event);
    }
  }

  /**
   * Handler cuando se aprieta una tecla en el input de HABER.
   * Se utiliza para no permitir comas y para pasar al siguiente input al usar ENTER.
   * Ademas, hace una logica para crear un item nuevo o enfocar en el boton "Guardar" en ciertas situaciones
   */
  private async onHaberKeydown(event: KeyboardEvent) {
    if (event.key === ',') {
      event.preventDefault();
    } else if (
      // Enter
      event.keyCode === 13 ||
      // Tab pero sin Shift cuando saldo es distinto a cero
      (this.item.saldo !== 0 && !event.shiftKey && event.keyCode === 9)
    ) {
      if (this.isLast) {
        this.$emit('rowRequested');
        await this.$nextTick();
      }
      focusOnNext();
    } else if (!event.shiftKey && event.keyCode === 9 && this.isLast) {
      // Tab sin shift cuando es la ultima fila y saldo es cero
      const btnGuardar = document.querySelector('.asiento-save-button');
      if (btnGuardar) focus(btnGuardar as HTMLButtonElement);
      event.preventDefault();
    }
  }
}
</script>

<style lang="scss" scoped>
.imputacion-field {
  justify-content: center;
  margin-bottom: 3px;
  .field {
    margin-bottom: 0px;
    margin-right: 3px !important;
  }
}
</style>
<style lang="scss">
.imputacion-field .label {
  text-align: center;
}
.fld-borrar .label {
  color: transparent;
}
.imputacion-field.selected {
  .label-input,
  .input {
    background: #ffec05;
  }
}
.imputacion-field input[disabled] {
  cursor: text;
}

// Esconde el saldo en la imputacion cuando ya no entra hasta 1024
// De 1024 a 880 se quita la sidebar, entonces vuelve a entrar
// Menos de 880 otra vez ya no entra
@media (max-width: 900px) {
  .app.reduced .field-saldo {
    display: none;
  }
}
@media (max-width: 1080px) {
  .app:not(.reduced) .field-saldo {
    display: none;
  }
}
</style>
