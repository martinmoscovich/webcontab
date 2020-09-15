<template>
  <section>
    <!-- Periodo cuando no se puede editar -->
    <Field label="Periodo" v-if="readonly || !isNew">
      <span class="">{{ periodoLabel }}</span>
    </Field>

    <!-- Input Periodo -->
    <Field v-else hideErrorMessage label-position="on-border" :v="periodoErrors">
      <PeriodoInput :value="periodoValue" :minDate="minDate" :focusedDate="minDate" @input="onPeriodoInput" />
    </Field>

    <!-- Opcion de cerrar o no el anterior -->
    <b-field grouped v-if="isNew && !isFirst" style="margin-top: 5px" class="column is-centered">
      <p class="control mt-2">
        ¿Cerrar Anterior?
      </p>
      <Field>
        <b-radio-button v-model="ejercicio.cerrarAnterior" :native-value="true" :disabled="readonly">
          <b-icon icon="check"></b-icon>
          <span>Sí</span>
        </b-radio-button>
        <b-radio-button v-model="ejercicio.cerrarAnterior" :native-value="false" :disabled="readonly">
          <b-icon icon="close"></b-icon>
          <span>No</span>
        </b-radio-button>
      </Field>
    </b-field>

    <!-- Botonera -->
    <b-field style="justify-content: center" class="mt-3">
      <b-field style="margin-right: 5px">
        <b-button
          type="is-success"
          icon-left="content-save"
          @click="onSave"
          :disabled="readonly || validation.touchedRecords.length === 0"
        >
          Guardar
        </b-button>
      </b-field>
      <b-field>
        <b-button
          type="is-danger"
          icon-left="close"
          @click="onCancel"
          :disabled="!isModal && validation.touchedRecords.length === 0"
        >
          {{ isModal ? 'Cerrar' : 'Cancelar' }}
        </b-button>
      </b-field>
    </b-field>
  </section>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { notificationService } from '@/service';
import { required } from '@/utils/validation';
import { Ejercicio } from '../../model/Ejercicio';
import { formatDate, isBefore } from '../../utils/date';
import { isNullOrUndefined } from '../../utils/general';
import { Periodo } from '../../model/Periodo';
import { FormValidation } from '@/model/FormValidation';

/** Form para crear ejercicios */
@Component({
  validators: {
    'ejercicio.inicio': required,
    'ejercicio.finalizacion': required,
    'ejercicio.cerrarAnterior': required
  }
})
export default class EjercicioForm extends Vue {
  /** Fecha minima posible para iniciar el ejercicio */
  @Prop({ type: Date, required: false })
  minDate: Date | null;

  /** Indica si es el primer ejercicio (en cuyo caso no mostrara la opcion de cerrar anterior) */
  @Prop({ type: Boolean })
  isFirst: boolean;

  /** Indica si no se puede modificar (por ahora no se usa) */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si el form se esta mostrando en un modal */
  @Prop({ type: Boolean })
  isModal: boolean;

  /** Instancia temporal usada en el form */
  private ejercicio: Partial<Ejercicio & { cerrarAnterior: boolean }> = {};

  /** Indica si es nuevo o modificacion (por ahora siempre es nuevo) */
  private isNew = true;

  private mounted() {
    this.reset();
  }

  /** Resetea el form */
  private reset() {
    // Se limpian las validaciones
    this.validation.reset();
    this.ejercicio = {
      inicio: undefined,
      finalizacion: undefined,
      cerrarAnterior: undefined
    };
    this.isNew = true;
  }

  /** Muestra el periodo del ejercicio con formato */
  private get periodoLabel() {
    if (!this.ejercicio.inicio && !this.ejercicio.finalizacion) return '';
    const i = this.ejercicio.inicio ? formatDate(this.ejercicio.inicio) : '';
    const f = this.ejercicio.finalizacion ? formatDate(this.ejercicio.finalizacion) : '';
    return `${i} - ${f}`;
  }

  /** Mensajes de errores de validacion del periodo  */
  private get periodoErrors() {
    return [...this.validation.allErrors('ejercicio.inicio'), ...this.validation.allErrors('ejercicio.finalizacion')];
  }

  /** Value del periodo actual */
  private get periodoValue() {
    return {
      desde: this.ejercicio?.inicio,
      hasta: this.ejercicio?.finalizacion
    };
  }

  /** Handler cuando cambia el periodo */
  private onPeriodoInput(p: Periodo) {
    this.ejercicio.inicio = p.desde;
    this.ejercicio.finalizacion = p.hasta;
  }

  /** Valida el form */
  private async validate(): Promise<FormValidation> {
    if (!this.ejercicio) return { msg: 'Debe completar los datos' };
    if (!this.ejercicio.inicio || !this.ejercicio.finalizacion) return { msg: 'Ingrese el periodo' };
    if (!isBefore(this.ejercicio.inicio, this.ejercicio.finalizacion)) {
      return { msg: 'El inicio debe ser anterior a la finalizacion' };
    }
    if (!this.isFirst && isNullOrUndefined(this.ejercicio.cerrarAnterior)) {
      return { msg: 'Indique si desea cerrar el ejercicio anterior' };
    }
    return { valid: true };
  }

  /** Handler cuando se quiere guardar el ejercicio */
  private async onSave() {
    if (this.isFirst) this.ejercicio.cerrarAnterior = false;

    const result = await this.validate();
    if (result.valid) {
      this.$emit('save', this.ejercicio);
    } else {
      console.error('El ejercicio no es valido: %o', result);
      notificationService.error(result.msg ?? 'La ejercicio no es valido');
    }
  }

  /** Handler cuando se hace click en Cancelar */
  private onCancel() {
    this.reset();
    if (this.isModal) this.$emit('close');
  }

  // DESCOMENTAR SI SE IMPLEMENTA MODIFICACION DE EJERCICIO

  // @Prop()
  // value: Ejercicio | null;
  //
  // /** Handler cuando cambia el ejercicio */
  // @Watch('value', { deep: true })
  // private onEjercicioChange() {
  //   this.reset();
  // }
  // /** Resetea el form */
  // private load() {
  //   // Se limpian las validaciones
  //   this.validation.reset();
  //   if (this.value) {
  //     // Si es modificacion
  //     this.ejercicio = JSON.parse(JSON.stringify(this.value));
  //     this.isNew = false;
  //   } else {
  //     // Si es nuevo
  //     this.ejercicio = {
  //       inicio: undefined,
  //       finalizacion: undefined,
  //       cerrarAnterior: undefined
  //     };
  //     this.isNew = true;
  //   }
  // }
}
</script>

<style lang="scss" scoped></style>
