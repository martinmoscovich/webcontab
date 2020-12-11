<template>
  <b-field style="justify-content: center;">
    <!-- DESDE -->
    <b-field label="Desde" label-position="on-border">
      <DatePicker
        placeholder="Seleccione..."
        :type="month ? 'month' : undefined"
        :value="value.desde"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="value.desde || value.hasta || realFocusedDate"
        @input="onDesdeInput"
      >
      </DatePicker>
    </b-field>

    <!-- HASTA -->
    <b-field label="Hasta" label-position="on-border" class="ml-2">
      <DatePicker
        placeholder="Seleccione..."
        :type="month ? 'month' : undefined"
        :value="value.hasta"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="value.hasta || value.desde || realFocusedDate"
        @input="onHastaInput"
      >
      </DatePicker>
    </b-field>
  </b-field>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Periodo } from '../../model/Periodo';
import { isBefore, isAfter } from '@/utils/date';

/** Input que permite ver / modificar un periodo, o sea una fecha de inicio y una de fin */
@Component
export default class PeriodoInput extends Vue {
  @Prop({ default: () => ({}) })
  value: Partial<Periodo>;

  /** Fecha minima permitida */
  @Prop({ type: Date })
  minDate: Date;

  /** Fecha maxima permitida */
  @Prop({ type: Date })
  maxDate: Date;

  /** Fecha en la cual mostrar el foco */
  @Prop({ type: Date, default: () => new Date() })
  focusedDate: Date;

  /** Indica si es solo seleccion de mes o de fecha completa */
  @Prop({ type: Boolean })
  month: boolean;

  /** Fecha que realmente se enfoca */
  private get realFocusedDate() {
    const now = this.focusedDate;
    // Si la fecha indicada supera la maxima, se enfoca la maxima
    if (this.maxDate && isAfter(now, this.maxDate)) return this.maxDate;
    // Si la fecha indicada es anterior a la minima, se enfoca la minina
    if (this.minDate && isBefore(now, this.minDate)) return this.minDate;

    // Se enfoca la indicada
    return now;
  }

  /** Handler cuando cambia el input Desde */
  private onDesdeInput(d: Date) {
    this.$emit('input', { desde: d, hasta: this.value?.hasta });
  }

  /** Handler cuando cambia el input Hasta */
  private onHastaInput(d: Date) {
    this.$emit('input', { hasta: d, desde: this.value?.desde });
  }
}
</script>
