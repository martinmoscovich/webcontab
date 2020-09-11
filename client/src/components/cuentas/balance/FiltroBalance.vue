<template>
  <b-field grouped>
    <!-- Input Desde -->
    <b-field label="Desde" label-position="on-border">
      <b-datepicker
        placeholder="Seleccione..."
        v-model="filter.desde"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="filter.desde || filter.hasta || focusedDate"
      >
        <button class="button is-danger" @click="filter.desde = undefined">
          <b-icon icon="close"></b-icon>
          <span>Limpiar</span>
        </button>
      </b-datepicker>
    </b-field>

    <!-- Input Hasta -->
    <b-field label="Hasta" label-position="on-border">
      <b-datepicker
        placeholder="Seleccione..."
        v-model="filter.hasta"
        :min-date="minDate"
        :max-date="maxDate"
        :focused-date="filter.hasta || filter.desde || focusedDate"
      >
        <button class="button is-danger" @click="filter.hasta = undefined">
          <b-icon icon="close"></b-icon>
          <span>Limpiar</span>
        </button>
      </b-datepicker>
    </b-field>

    <!-- Input Categoria -->
    <b-field label="Categoría" label-position="on-border">
      <CuentaSearch modo="categoria" :value="categoria" placeholder="Buscar" @input="onCategoriaSelect" />
    </b-field>

    <!-- Switch "Incluir Cero" -->
    <b-switch v-model="filter.cero" class="mr-2">Cero</b-switch>

    <!-- Boton -->
    <b-button type="is-primary" icon-left="filter" @click="onSearchClick">Filtrar</b-button>
  </b-field>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator';
import { BalanceFilter } from '@/api/InformeApi';
import { notificationService } from '@/service';
import { Categoria } from '@/model/Cuenta';

/** Filtro para balance */
@Component
export default class FiltroBalance extends Vue {
  /** Filtro actual */
  @Prop()
  value: BalanceFilter;

  /** Fecha minima permitida */
  @Prop({ type: Date })
  minDate: Date;

  /** Fecha maxima permitida */
  @Prop({ type: Date })
  maxDate: Date;

  /** Categoria en la cual buscar */
  @Prop({ required: false })
  categoria: Categoria | undefined;

  /** Filtro temporal que se usa en el form */
  private filter: BalanceFilter = {};

  mounted() {
    this.setFilter();
  }

  /** Handler cuando cambia el valor actual */
  @Watch('value')
  private onValueChange() {
    this.setFilter();
  }

  /** Hace una copia del filtro indicado al temporal */
  setFilter() {
    this.filter = { ...this.value };
  }

  /** Valida las fechas del filtro */
  private validateDateFilter() {
    // Si se completaron ambos campos y "desde" es mayor que "hasta", es un error
    if (this.filter.hasta && (this.filter.desde?.getTime() ?? 0) > this.filter.hasta.getTime()) {
      notificationService.warn("La fecha 'Desde' no puede ser mayor que la fecha 'Hasta'");
      return false;
    }
    return {
      desde: this.filter.desde,
      hasta: this.filter.hasta
    };
  }

  /** Obtiene la fecha enfocada */
  private get focusedDate() {
    const now = new Date();
    if (this.maxDate && now.getTime() > this.maxDate.getTime()) return this.maxDate;
    if (this.minDate && now.getTime() < this.minDate.getTime()) return this.minDate;
    return now;
  }

  /** Handler cuando se hace click en el boton */
  private onSearchClick() {
    const payload: BalanceFilter | boolean = this.validateDateFilter();
    if (!payload) return;

    if (this.filter.categoria) payload.categoria = this.filter.categoria;
    if (this.filter.cero) payload.cero = this.filter.cero;
    this.$emit('input', payload);
  }

  /** Handler cuando se selecciona una categoria */
  private onCategoriaSelect(categoria: Categoria) {
    this.filter.categoria = categoria?.id;
  }
}
</script>

<style lang="scss" scoped>
.field {
  &.is-grouped {
    align-items: center;
    margin: 5px 0;
  }
  margin-bottom: 0;
}
</style>
