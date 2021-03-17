<template>
  <b-field>
    <!-- Switch entre modo "Fechas" y modo "Numeros" -->
    <b-checkbox v-model="dateMode" />

    <!-- Modo Fechas -->
    <template v-if="dateMode">
      <!-- Fecha Desde -->
      <b-field label="Desde" label-position="on-border">
        <DatePicker
          v-model="filter.desde"
          :min-date="minDate"
          :max-date="maxDate"
          :focused-date="filter.desde || filter.hasta || focusedDate"
        >
          <button class="button is-danger" @click="filter.desde = undefined">
            <b-icon icon="close"></b-icon>
            <span>Limpiar</span>
          </button>
        </DatePicker>
      </b-field>

      <!-- Fecha Hasta -->
      <b-field label="Hasta" label-position="on-border">
        <DatePicker
          v-model="filter.hasta"
          :min-date="minDate"
          :max-date="maxDate"
          :focused-date="filter.hasta || filter.desde || focusedDate"
        >
          <button class="button is-danger" @click="filter.hasta = undefined">
            <b-icon icon="close"></b-icon>
            <span>Limpiar</span>
          </button>
        </DatePicker>
      </b-field>
    </template>

    <!-- Modo "Numeros" -->
    <template v-else>
      <!-- Numero Min -->
      <b-field label="Min" label-position="on-border">
        <b-input type="number" v-model="filter.min" @keydown.native.enter="onButtonClick" />
      </b-field>

      <!-- Numero Max -->
      <b-field label="Max" label-position="on-border">
        <b-input type="number" v-model="filter.max" @keydown.native.enter="onButtonClick" />
      </b-field>
    </template>

    <!-- Boton -->
    <b-button type="is-primary" :icon-left="buttonIcon" @click="onButtonClick">{{ buttonLabel }}</b-button>
  </b-field>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator';
import { AsientosSearchFilter } from '@/api/AsientoApi';
import { notificationService } from '../../service';
import { isNullOrUndefined } from '../../utils/general';
import { isAfter, isBefore } from '@/utils/date';

/**
 * Filtro para asientos.
 * Se usa en todos los lugares donde se pueden filtrar asientos
 */
@Component
export default class FiltroAsientos extends Vue {
  /** Filtro actual */
  @Prop()
  value: AsientosSearchFilter;

  /** Fecha minima que se permite filtrar */
  @Prop({ type: Date })
  minDate: Date;

  /** Fecha maxima que se permite filtrar */
  @Prop({ type: Date })
  maxDate: Date;

  /** Icono del boton */
  @Prop({ default: 'filter' })
  buttonIcon: string;

  /** Texto del boton */
  @Prop({ default: 'Filtrar' })
  buttonLabel: string;

  /** Indica si el filtro esta en modo "Fechas" o en modo "Numeros" */
  private dateMode = true;

  /** Filtro temporal utilizado en el form */
  filter: AsientosSearchFilter = {};

  private mounted() {
    this.setFilter();
  }

  @Watch('value')
  private onValueChange() {
    this.setFilter();
  }

  /** Crea el filtro temporal usado a partir del que se especifica en la Prop */
  private setFilter() {
    this.dateMode = isNullOrUndefined(this.value.min) && isNullOrUndefined(this.value.max);
    this.filter = { ...this.value };
  }

  /** Calcula la fecha que debe estar seleccionada en el picker */
  private get focusedDate() {
    const now = new Date();
    // Si la fecha maxima es anterior a hoy, mostrar la fecha maxima
    if (this.maxDate && isAfter(now, this.maxDate)) return this.maxDate;
    // Si la fecha minima es posterior a hoy, mostrar la fecha minima
    if (this.minDate && isBefore(now, this.minDate)) return this.minDate;

    // Mostrar la fecha de hoy
    return now;
  }

  /** Handler cuando se hace click en el boton */
  private onButtonClick() {
    // Se valida el form
    const payload = this.dateMode ? this.validateDateFilter() : this.validateNumberFilter();

    if (!payload) return;
    this.$emit('input', payload);
  }

  /** Valida que el filtro por numeros sea valido */
  private validateNumberFilter() {
    // Si se completaron ambos campos y min es mayor que max, es un error
    if (this.filter.max && (this.filter.min ?? -1) > this.filter.max) {
      notificationService.warn("El número 'Min' no puede ser mayor que el número 'Max'");
      return null;
    }
    return {
      min: this.filter.min || undefined,
      max: this.filter.max || undefined
    };
  }

  /** Valida que el filtro por fechas sea valido */
  private validateDateFilter() {
    // Si se completaron ambos campos y desde es posterior que hasta, es un error
    if (this.filter.desde && this.filter.hasta && isAfter(this.filter.desde, this.filter.hasta)) {
      notificationService.warn("La fecha 'Desde' no puede ser mayor que la fecha 'Hasta'");
      return false;
    }
    return {
      desde: this.filter.desde || undefined,
      hasta: this.filter.hasta || undefined
    };
  }
}
</script>

<style lang="scss" scoped>
.field {
  align-items: center;
  margin: 5px 0;
  .field:not(:last-child) {
    margin-right: 0.75rem;
  }
}
</style>
