<template>
  <div class="mb-2">
    <b-field grouped class="fields mb-0">
      <!-- Fecha -->
      <Field label="Fecha" label-position="on-border" :v="validation.allErrors('asiento.fecha')">
        <b-datepicker
          ref="inputFecha"
          v-model="asiento.fecha"
          @input="onInput"
          required
          placeholder="Ingrese una fecha..."
          icon="calendar-today"
          :loading="loadingDate"
          :date-parser="parseDate"
          :editable="!readonly"
          :disabled="readonly"
          :min-date="minDate"
          :max-date="maxDate"
        >
        </b-datepicker>
      </Field>

      <!-- Detalle -->
      <Field label="Detalle" label-position="on-border" expanded>
        <b-input
          v-model="asiento.detalle"
          @input="onInput"
          :placeholder="readonly ? '' : 'Detalle'"
          name="detalle"
          :disabled="readonly"
        />
      </Field>
    </b-field>
    <div>
      <Field expanded class="has-text-right mb-0">
        <AuditableLabel :model="asiento" />
      </Field>
    </div>
  </div>
</template>
R
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { parseDate } from '@/utils/date';
import { required } from '@/utils/validation';
import { ValidableVue } from '../../core/ui/elements';
import { AsientoModel } from '../../model/Asiento';
import { Focusable } from '../../utils/browser';

/** Form de datos basicos del asiento  */
@Component({
  validators: {
    'asiento.fecha': required
  }
})
export default class AsientoBasicInfo extends Vue implements ValidableVue {
  /** Asiento a mostrar / editar */
  @Prop()
  asiento: AsientoModel;

  /** Indica que no se permiten modificaciones */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica que esta cargando la fecha sugerida */
  @Prop({ type: Boolean })
  loadingDate: boolean;

  /** Fecha minima permitida */
  @Prop({ type: Date })
  minDate: Date;

  /** Fecha maxima permitida */
  @Prop({ type: Date })
  maxDate: Date;

  $refs: { inputFecha: Vue & Focusable };

  /** Titulo del asiento segun si es nuevo o no */
  private get title() {
    if (this.asiento.numero) return 'Asiento #' + this.asiento.numero;
    return 'Nuevo Asiento';
  }

  /** Hace foco en el form (en el input de fecha) */
  focus() {
    this.$refs.inputFecha?.focus();
  }

  /** Valida el form */
  validate(): Promise<boolean> {
    return this.$validate();
  }

  /** Resetea el form */
  reset() {
    this.validation.reset();
  }

  /** Handler cuando hay cambios en el form */
  private onInput() {
    this.$emit('input', this.asiento);
  }

  /** Metodo que parsea la fecha del Date Picker */
  private parseDate(str: string) {
    return parseDate(str);
  }
}
</script>

<style lang="scss" scoped>
.fields {
  padding-top: 5px;
}
</style>
