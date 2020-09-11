<template>
  <section>
    <PageNavBar>
      <NavBarItem>
        <!-- Boton Guardar (en la Toolbar) -->
        <NavBarButton
          v-if="!disabled"
          label="Guardar"
          type="is-primary"
          icon="content-save"
          @click="onSaveClick"
          :disabled="disabled || validation.touchedRecords.length === 0"
        />
      </NavBarItem>
    </PageNavBar>
    <div class="columns">
      <div class="column">
        <!-- Nombre -->
        <Field label="Nombre" hideErrorMessage label-position="on-border" :v="validation.allErrors('moneda.nombre')">
          <b-input ref="inputNombre" placeholder="Nombre" name="nombre" v-model="moneda.nombre" :disabled="disabled" />
        </Field>

        <!-- Codigo -->
        <Field
          label="Código"
          expanded
          label-position="on-border"
          :v="validation.allErrors('moneda.codigo')"
          hideErrorMessage
        >
          <b-input v-model="moneda.codigo" :disabled="disabled" placeholder="Código" name="codigo" />
        </Field>

        <!-- Simbolo -->
        <Field
          label="Símbolo"
          expanded
          label-position="on-border"
          :v="validation.allErrors('moneda.simbolo')"
          hideErrorMessage
        >
          <b-input v-model="moneda.simbolo" :disabled="disabled" placeholder="Símbolo" name="simbolo" />
        </Field>
      </div>

      <!-- Switch Default -->
      <div class="column is-narrow">
        <Field style="margin-top: 5px">
          <b-switch v-model="moneda.default">Default</b-switch>
        </Field>
      </div>
    </div>
  </section>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { notificationService } from '@/service';
import { required, field } from '@/utils/validation';
import { Focusable } from '@/utils/browser';
import { Moneda } from '@/model/Moneda';

/** Formulario de Alta y Modificacion de monedas */
@Component({
  validators: {
    'moneda.nombre': required,
    'moneda.codigo': required,
    'moneda.simbolo': required,
    'moneda.default': field
  }
})
export default class MonedaForm extends Vue {
  @Prop()
  value: Moneda;

  /** Indica que no se debe poder editar, solo ver */
  @Prop({ type: Boolean })
  disabled: boolean;

  /** Instancia temporal para usar en el form */
  private moneda: Partial<Moneda> = {};

  /** Indica si es un alta o modificacion */
  private isNew = true;

  $refs: { inputNombre: Vue & Focusable };

  private mounted() {
    this.load();
    this.$refs.inputNombre.focus();
  }

  /** Handler cuando cambia la moneda a editar */
  @Watch('value', { deep: true })
  private onMonedaChange() {
    this.load();
  }

  /** Carga la moneda seleccionada o una nueva en el form */
  private load() {
    // Resetea la validacion
    this.validation.reset();
    if (this.value) {
      // Modificacion
      this.moneda = JSON.parse(JSON.stringify(this.value));
      this.isNew = false;
    } else {
      // Nueva
      this.moneda = {
        codigo: '',
        nombre: '',
        simbolo: '',
        default: false
      };
      this.isNew = true;
    }
  }

  /** Valida el form */
  private async validate() {
    if (!this.moneda) return { msg: 'Debe completar los datos' };
    try {
      if (await this.$validate()) {
        return { valid: true };
      }
      return { msg: 'Los datos ingresados no son válidos' };
    } catch {
      return { msg: 'Los datos ingresados no son válidos' };
    }
  }

  /** Handler cuando se desea guardar la moneda */
  private async onSaveClick() {
    // Valida
    const result = await this.validate();
    if (result.valid) {
      // Emite evento
      this.$emit('save', this.moneda);
    } else {
      console.error('La Moneda no es valida: %o', result);
      notificationService.error(result.msg ?? 'La Moneda no es valida');
    }
  }
}
</script>

<style lang="scss" scoped></style>
