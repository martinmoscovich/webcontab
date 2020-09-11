<template>
  <section>
    <div class="columns">
      <div class="column">
        <!-- Input Numero (readonly) -->
        <Field label="Número" v-if="readonly || !isNew">
          <span class="">{{ cuenta.codigo }}</span>
        </Field>

        <!-- Input Numero (modificable) -->
        <Field
          v-else
          label="Número"
          hideErrorMessage
          label-position="on-border"
          :v="validation.allErrors('cuenta.numero')"
        >
          <p class="control">
            <span class="button is-static">{{ codigoPadre }}</span>
          </p>
          <b-input
            :placeholder="readonly ? '' : 'N°'"
            name="numero"
            v-model="cuenta.numero"
            type="number"
            min="1"
            max="9999"
            @keydown.native="onNumeroKeydown"
          />
        </Field>

        <!-- Input Descripcion -->
        <Field label="Descripción" v-if="readonly">
          <span class="">{{ cuenta.descripcion }}</span>
        </Field>
        <Field v-else label="Descripción" expanded :v="validation.allErrors('cuenta.descripcion')" hideErrorMessage>
          <b-input v-model="cuenta.descripcion" :placeholder="readonly ? '' : 'Descripción'" name="descripcion" />
        </Field>

        <!-- Moneda -->
        <Field label="Moneda" v-if="readonly">
          <span class="">{{ moneda ? moneda.codigo : '' }}</span>
        </Field>
        <Field v-else :v="validation.allErrors('cuenta.moneda')" hideErrorMessage>
          <MonedaSelect v-model="cuenta.monedaId" />
        </Field>
      </div>

      <!-- Flags -->
      <div class="column is-narrow">
        <!-- Activa -->
        <Field v-if="!isNew" class="mt-1">
          <b-switch v-model="cuenta.activa" :disabled="readonly">Activa</b-switch>
        </Field>

        <!-- Individual -->
        <Field class="mt-1">
          <b-switch v-model="cuenta.individual" :disabled="readonly">Individual</b-switch>
        </Field>

        <!-- Ajustable -->
        <Field class="mt-1">
          <b-switch v-model="cuenta.ajustable" :disabled="readonly">Ajustable</b-switch>
        </Field>

        <!-- Balancea Resultados -->
        <Field class="mt-1">
          <b-switch v-model="cuenta.balanceaResultados" :disabled="readonly">Balancea Result.</b-switch>
        </Field>
      </div>
    </div>

    <!-- Botonera -->
    <b-field style="justify-content: flex-end">
      <b-field class="mr-1">
        <b-button
          v-if="!readonly"
          type="is-success"
          icon-left="content-save"
          @click="onSave"
          :disabled="validation.touchedRecords.length === 0"
        >
          Guardar
        </b-button>
      </b-field>
      <b-field>
        <b-button
          v-if="!readonly && !isNew"
          class="ml-1"
          type="is-danger"
          icon-left="delete"
          @click="onDelete"
        ></b-button>
      </b-field>
    </b-field>
  </section>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Cuenta, Categoria, getMaxNumeroHijo } from '@/model/Cuenta';
import { notificationService } from '@/service';
import { integer, required, field } from '@/utils/validation';
import { monedaStore } from '../../store';
import { isNullOrUndefined } from '../../utils/general';
import { FormValidation } from '@/model/FormValidation';

/**
 * Form ABM de Cuentas
 */
@Component({
  validators: {
    'cuenta.numero': integer({ min: 1, max: 9999, required: true }),
    'cuenta.descripcion': required,
    'cuenta.monedaId': integer({ min: 1, required: true }),
    'cuenta.individual': field,
    'cuenta.ajustable': field,
    'cuenta.activa': field,
    'cuenta.balanceaResultados': field
  }
})
export default class CuentaForm extends Vue {
  /** Cuenta a ver/editar */
  @Prop()
  value: Cuenta;

  /** Categoria Padre (en caso de ser una nueva cuenta) */
  @Prop()
  parent: Categoria;

  /** Indica si no se debe poder modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Cuenta usada temporalmente por el form */
  private cuenta: Partial<Cuenta> = {};

  /** Indica si es una cuenta nueva o una a modificar */
  private isNew = true;

  /** Variable donde se guarda el codigo del padre */
  private codigoPadre = '';

  private mounted() {
    this.reset();
  }

  /** Valida el form */
  async validate(): Promise<FormValidation> {
    if (!this.cuenta) return { msg: 'Debe completar los datos' };
    try {
      if (await this.$validate()) {
        return { valid: true };
      }
      return { msg: 'Los datos ingresados no son válidos' };
    } catch {
      return { msg: 'Los datos ingresados no son válidos' };
    }
  }

  /** Handler cuando cambia la cuenta a editar */
  @Watch('value', { deep: true })
  private onCuentaChange() {
    this.reset();
  }

  /** Handler cuando cambia la categoria padre */
  @Watch('parent')
  private onParentChange() {
    if (!this.parent) return;
    this.cuenta.categoriaId = this.parent.id;
    if (this.isNew) {
      this.codigoPadre = this.parent?.codigo;
      this.cuenta.numero = this.calcularNuevoNumero();
    }
  }

  /** Se resetea el form y se carga segun la categoria actual */
  private reset() {
    this.validation.reset();

    if (this.value) {
      // Cuenta a modificar
      this.cuenta = JSON.parse(JSON.stringify(this.value));
      this.codigoPadre = this.calcularCodigoPadre();
      this.isNew = false;
    } else {
      // Nueva cuenta
      this.cuenta = {
        categoriaId: this.parent?.id,
        activa: true,
        descripcion: '',
        monedaId: monedaStore.lista.items.find(m => m.default)?.id,
        ajustable: false,
        individual: false,
        balanceaResultados: false,
        numero: this.calcularNuevoNumero()
      };
      this.codigoPadre = this.parent?.codigo;
      this.isNew = true;
    }
  }

  /** Calcula el numero de una cuenta nueva en base a su padre */
  private calcularNuevoNumero() {
    const n = getMaxNumeroHijo(this.parent);
    return isNullOrUndefined(n) ? undefined : n + 1;
  }

  /** Obtiene el codigo del padre en base al codigo de la cuenta que se esta modificando */
  private calcularCodigoPadre() {
    if (!this.cuenta || !this.cuenta.codigo) return '';

    // Se obtienen las partes del codigo
    const parts = this.cuenta.codigo.split('.');

    // Si es root, no tiene codigo de padre
    if (parts.length === 1) return '';

    // Se quita la ultima parte y se vuelve a unir, obteniendo el codigo del padre
    parts.pop();
    return parts.join('.');
  }

  /** Obtiene la moneda a partir del Id */
  private get moneda() {
    if (!this.cuenta.monedaId) return null;
    return monedaStore.find(this.cuenta.monedaId);
  }

  /**
   * Handler para cuando se aprieta una tecla en el input de numero.
   * Solo permite teclas numericas y de navegacion
   */
  private onNumeroKeydown(e: KeyboardEvent) {
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
      // Backspace and Tab and Enter
      key == 8 ||
      key == 9 ||
      key == 13 ||
      // Home and End
      key == 35 ||
      key == 36 ||
      // arrows
      key == 37 ||
      key == 38 ||
      key == 39 ||
      key == 40 ||
      // Del and Ins
      key == 46 ||
      key == 45
    ) {
      this.$emit('keydown', e);
      return true;
    }

    e.preventDefault();
  }

  /** Handler para el save del form */
  private async onSave() {
    const result = await this.validate();
    if (result.valid) {
      this.cuenta.imputable = true;

      // Lanza el evento
      this.$emit('save', this.cuenta);
    } else {
      console.error('La Cuenta no es valida: %o', result);
      notificationService.error(result.msg ?? 'La Cuenta no es valida');
    }
  }

  /** Handler del boton de Borrar. Pide confirmacion */
  private onDelete() {
    this.$buefy.dialog.confirm({
      message: '¿Está seguro que desea borrar la cuenta?',
      // Lanza el evento
      onConfirm: () => this.$emit('delete', this.cuenta),
      cancelText: 'No',
      confirmText: 'Sí'
    });
  }
}
</script>

<style lang="scss" scoped></style>
