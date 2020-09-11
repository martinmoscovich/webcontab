<template>
  <div class="container" v-if="asiento">
    <!-- <PageNavBar>
      <NavBarItem :dense="true">
        <b-tooltip label="Copiar a Excel" position="is-bottom" type="is-info">
          <a @click="onExcelCopy"><b-icon icon="microsoft-excel"/></a>
        </b-tooltip>
      </NavBarItem>
    </PageNavBar> -->

    <!-- Informacion basica del asiento -->
    <AsientoBasicInfo
      ref="basicInfo"
      :asiento="asiento"
      :readonly="readonly"
      :min-date="minDate"
      :max-date="maxDate"
      :loadingDate="loadingDate"
      @input="onFormChange"
    />

    <!-- Imputaciones -->
    <ListaImputaciones
      ref="list"
      :imputaciones="asiento.imputaciones"
      :saldos="asiento.saldos"
      :readonly="readonly"
      :loading="loadingImputaciones"
      @input="onFormChange"
    />

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import ListaImputaciones from '@/components/asientos/ListaImputaciones.vue';
import { AsientoModel } from '@/model/Asiento';
import AsientoBasicInfo from './AsientoBasicInfo.vue';
import { notificationService } from '@/service';
import { ValidableVue } from '@/core/ui/elements';
import { AsientoDTO } from '@/model/AsientoDTO';
import { FormValidation } from '@/model/FormValidation';

/**
 * Formulatio de ABM de Asientos.
 * Incluye informacion basica y las imputaciones.
 */
@Component({
  components: {
    ListaImputaciones,
    AsientoBasicInfo
  }
})
export default class AsientoForm extends Vue {
  /** Asiento a mostrar/editar */
  @Prop()
  value: AsientoDTO;

  /** Indica si no se permiten modificaciones */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Indica si se esta cargando o guardando */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Indica si se estan cargando las imputaciones */
  @Prop({ type: Boolean })
  loadingImputaciones: boolean;

  /** Fecha minima permitida */
  @Prop({ type: Date })
  minDate: Date;

  /** Fecha maxima permitida */
  @Prop({ type: Date })
  maxDate: Date;

  /** Fecha inicial del asiento */
  @Prop({ type: Date, required: false })
  suggestedDate: Date;

  $refs: {
    basicInfo: AsientoBasicInfo;
    list: ValidableVue;
  };

  /** Asiento temporal usado en el form */
  private asiento: AsientoModel | null = null;

  private mounted() {
    this.onAsientoChange();
  }

  /**
   * Handler cuando cambia la fecha sugerida
   * Se necesita porque puede pasar que la fecha se setee despues que el "value"
   */
  @Watch('suggestedDate')
  onSuggestedDateChange() {
    // Si es un alta de asiento y hay fecha sugerida, utilizarla
    if (this.asiento && this.asiento.id === null && this.suggestedDate) {
      this.asiento.fecha = this.suggestedDate;
    }
  }

  /** Handler cuando cambia el asiento a editar */
  @Watch('value', { deep: true })
  private onAsientoChange() {
    if (this.value) {
      // Si se especifica un asiento a editar
      this.asiento = new AsientoModel();
      this.asiento.setId(this.value.id);
    } else {
      // Si es un alta de asiento
      const asiento = new AsientoModel();
      asiento.setId(null);

      // Si hay fecha sugerida, utilizarla
      if (this.suggestedDate) asiento.fecha = this.suggestedDate;

      this.asiento = asiento;
    }
  }

  /**
   * Hace foco en el form.
   * Hace foco en el campo de fecha del asiento
   */
  focus() {
    this.$refs.basicInfo.focus();
  }

  /** Indica que esta cargando la fecha sugerida */
  private get loadingDate() {
    return !this.suggestedDate && this.asiento && !this.asiento.id;
  }

  /**
   * Valida el form.
   * Valida tanto los datos basicos como las imputaciones
   */
  async validate(): Promise<FormValidation> {
    if (!this.asiento) return { msg: 'Debe completar los datos' };

    // Llama a validar el form basico y el form de cada imputacion
    const r = await Promise.all([this.$refs.basicInfo.validate(), this.$refs.list.validate()]);

    // Si son validos individualmente, se valida el asiento en su conjunto
    const result = this.asiento.validar();
    if (!result.valid) return result;
    if (!r.every(v => v)) {
      return { valid: false, msg: 'Los datos ingresados no son válidos' };
    }
    return { valid: true };
  }

  /**
   * Si el form es valido, genera el DTO para enviar al server a partir del Form.
   * Luego dispara el evento "save".
   * Si no es valido, muestra un error
   */
  async save() {
    if (!this.asiento) return;
    await this.removeEmptyImputaciones();

    const result = await this.validate();
    if (result.valid) {
      // Genera DTO y emite evento  si es valido
      this.$emit('save', this.asiento.toDTO(true));
    } else {
      // Muestra error al usuario si no lo es
      console.error('El asiento no es valido: %o', result);
      notificationService.error(result.msg ?? 'El asiento no es valido');
    }
  }

  /** Limpia el form */
  reset() {
    this.onAsientoChange();
    this.$refs.basicInfo?.reset();
    this.$refs.list?.reset();
  }

  /** Handler cuando hay cualquier tipo de cambio en el asiento o sus imputaciones */
  private onFormChange() {
    this.$emit('change');
  }

  /** Remueve las imputaciones sin datos antes de guardar */
  private async removeEmptyImputaciones() {
    if (!this.asiento) return;
    // Se quitan las filas vacias automaticamente
    this.asiento.imputaciones = this.asiento.imputaciones.filter(i => {
      // Si tiene ID, ya existe en el server, no se quita
      if (i.id) return true;

      // Se dejan solo las que tiene algun dato completado
      return i.cuenta?.id || i.detalle || i.importe;
    });

    // Si el asiento quedo vacio, se genera al menos una imputacion vacia.
    if (this.asiento.imputaciones.length === 0) this.asiento.newImputacion();

    // Permito que Vue actualice la lista para que el validador no tire error en las filas que se acaban de borrar
    await this.$nextTick();
  }

  // onExcelCopy() {
  //   if (!this.asiento) return;
  //   const rows = this.asiento.imputaciones
  //     .map(i => {
  //       const debe =
  //         i.importe >= 0 ? i.importe.toString().replace(".", ",") : "";
  //       const haber =
  //         i.importe < 0
  //           ? Math.abs(i.importe)
  //               .toString()
  //               .replace(".", ",")
  //           : "";
  //       return `<tr><td>${i.cuenta?.descripcion ?? ""}</td><td>${
  //         i.detalle
  //       }</td><td>${debe}</td><td>${haber}</td></tr>`;
  //     })
  //     .join("");

  //   const table =
  //     "<table><thead><tr><th>Cuenta</th><th>Detalle</th><th>Debe</th><th>Haber</th></tr></thead><tbody>" +
  //     rows +
  //     "</tr></tfoot></table>";

  //   navigator.clipboard.writeText(table);
  // }
}
</script>

<style lang="scss" scoped></style>
