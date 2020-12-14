<template>
  <b-modal :active="!!ejercicio" has-modal-card>
    <CardComponent title="Confirmar y renumerar asientos de ejercicio">
      <template v-if="quedanFechas">
        <!-- Instrucciones -->
        <article class="mb-3">
          <p>Indique la fecha hasta la cual se confirman los asientos.</p>
          <p><strong>No</strong> se podrán crear, modificar ni eliminar asientos anteriores a esa fecha</p>
          <p>Se renumerarán <strong>TODOS</strong> los asientos del ejercicio.</p>
        </article>

        <!-- Input Fecha -->
        <Field hideErrorMessage label-position="on-border">
          <DatePicker
            inline
            class="has-text-centered"
            :value="actualValue"
            :min-date="minDate"
            :max-date="maxDate"
            @input="value = $event"
          />
        </Field>
      </template>
      <template v-else>
        <article class="mb-3">
          <p>La fecha confirmada es la de finalización ({{ fechaConfirmacionFormateada }}).</p>
          <p><strong>No</strong> se puede modificar.</p>
        </article>
      </template>

      <!-- Botonera -->
      <b-field style="justify-content: center" class="mt-3">
        <b-field v-if="quedanFechas" style="margin-right: 5px">
          <b-button type="is-success" icon-left="check" @click="onConfirm" :disabled="!actualValue">
            Confirmar
          </b-button>
        </b-field>
        <b-field class="has-text-centered">
          <b-button type="is-danger" icon-left="close" @click="onClose">Cerrar</b-button>
        </b-field>
      </b-field>
    </CardComponent>
  </b-modal>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import { Ejercicio } from '@/model/Ejercicio';
import { formatDate, addDays, isEqualOrBefore } from '@/utils/date';

/**
 * Modal para confirmar la renumeracion, ingresando la fecha de confirmacion.
 */
@Component({ components: {} })
export default class ModalRenumerarEjercicio extends Vue {
  @Prop()
  ejercicio: Ejercicio;

  /** Fecha seleccionada */
  private value: Date | null = null;

  /**
   * Fecha minima posible.
   * Se usa la confirmada si existe y si no la de inicio del ejercicio
   */
  private get minDate() {
    return this.ejercicio?.fechaConfirmada ?? this.ejercicio?.inicio;
  }

  /** Fecha maxima posible (la de finalizacion del ejercicio) */
  private get maxDate(): Date {
    return this.ejercicio?.finalizacion;
  }

  /**
   * Valor a usar.
   * Si no esta seleccionado, se usa la fecha minima
   */
  private get actualValue() {
    return this.value ?? this.minDate;
  }

  /**
   * Indica si queda alguna fecha disponible para seleccionar.
   * Sera false cuando la fecha de confirmacion sea la de finalizacion.
   */
  private get quedanFechas() {
    if (!this.minDate || !this.maxDate) return true;
    return isEqualOrBefore(this.minDate, this.maxDate);
  }

  /** Fecha de confirmacion con formato "dd/mm/yyyy" */
  private get fechaConfirmacionFormateada() {
    if (!this.ejercicio?.fechaConfirmada) return '';
    return formatDate(this.ejercicio?.fechaConfirmada);
  }

  /** Handler cuando confirma */
  private onConfirm() {
    if (this.actualValue) {
      this.$emit('confirm', this.actualValue);
      this.value = null;
    }
  }

  /** Handler cuando cierra el modal */
  private onClose() {
    this.$emit('close');
    this.value = null;
  }
}
</script>

<style lang="stylus" scoped></style>
