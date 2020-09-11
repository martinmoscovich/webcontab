<template>
  <b-field>
    <!-- DESDE -->
    <b-field label="Desde" label-position="on-border">
      <b-datepicker
        placeholder="Seleccione..."
        :value="value.desde"
        @input="onDesdeInput"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="value.desde || value.hasta || realFocusedDate"
      >
      </b-datepicker>
    </b-field>

    <!-- HASTA -->
    <b-field label="Hasta" label-position="on-border">
      <b-datepicker
        placeholder="Seleccione..."
        :value="value.hasta"
        @input="onHastaInput"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="value.hasta || value.desde || realFocusedDate"
      >
      </b-datepicker>
    </b-field>
  </b-field>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Periodo } from '../../model/Periodo';

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

  /** Fecha que realmente se enfoca */
  private get realFocusedDate() {
    const now = this.focusedDate;
    // Si la fecha indicada supera la maxima, se enfoca la maxima
    if (this.maxDate && now.getTime() > this.maxDate.getTime()) return this.maxDate;
    // Si la fecha indicada es anterior a la minima, se enfoca la minina
    if (this.minDate && now.getTime() < this.minDate.getTime()) return this.minDate;

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
