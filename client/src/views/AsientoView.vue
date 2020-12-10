<template>
  <section class="section is-main-section">
    <PageNavBar cuentaSearch :title="title">
      <NavBarItem>
        <!-- Boton de Guardar -->
        <NavBarButton
          v-if="!readonly"
          label="Guardar"
          :disabled="loading || loadingImputaciones"
          buttonClass="asiento-save-button"
          type="is-primary"
          icon="content-save"
          @click="onSaveClick"
          @keydown="onKeyDownGuardar"
        />
        <!-- Boton de Borrar -->
        <NavBarButton
          v-if="!readonly && !isNew"
          label="Borrar"
          :disabled="loading || loadingImputaciones"
          buttonClass="ml-1"
          type="is-danger"
          icon="delete"
          @click="onDeleteClick"
        />
      </NavBarItem>
    </PageNavBar>
    <!-- Form de ABM de Asiento -->
    <AsientoForm
      ref="form"
      :value="asiento"
      :loading="loading"
      :loadingImputaciones="loadingImputaciones"
      @save="onAsientoSave"
      @change="onChange"
      :readonly="readonly"
      :min-date="minDate"
      :max-date="maxDate"
      :suggested-date="suggestedDate"
    />
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import AsientoForm from '@/components/asientos/AsientoForm.vue';
import { AsientoDTO, UpstreamAsientoDTO } from '../model/AsientoDTO';
import { asientoStore, cuentaStore, sessionStore } from '../store';
import { routerService } from '../service';
import { isNotFound } from '../core/ajax/error';
import { focus } from '../utils/browser';
import { Route } from 'vue-router';
import { asientoApi } from '@/api';
import { isBefore } from '@/utils/date';

/**
 * Guard que detecta cuando cambia la url, ya sea para cambiar de pagina o para ver otro asiento
 */
function onRouteChange(this: AsientoView, to: Route, from: Route, next: (v: false | void) => void) {
  this.onLeave(next);
}

/**
 * Pagina de ABM de Asiento
 */
@Component({
  components: { AsientoForm },
  beforeRouteUpdate: onRouteChange,
  beforeRouteLeave: onRouteChange
})
export default class AsientoView extends Vue {
  /** Indica si hay datos sin guardar */
  private dirty = false;

  private suggestedDate: Date | null = null;

  $refs: { form: AsientoForm };

  @Watch('$route')
  private onRouteChange() {
    this.loadAsiento();
  }

  private mounted() {
    this.loadAsiento();

    // Se inicia el ping
    sessionStore.startPing();
  }

  /** Handler cuando se destruye el componente  */
  private destroyed() {
    // Se detiene el ping
    sessionStore.stopPing();
  }

  /** Carga el asiento indicado segun la URL */
  private async loadAsiento() {
    // Si se intenta crar uno nuevo pero esta en modo readonly, volver a la pagina de categorias
    if (this.isNew && this.readonly) {
      this.$router.replace(routerService.categoria());
      return;
    }

    // Se limpia la fecha sugerida
    this.suggestedDate = null;

    // Si es modificacion, se carga el asiento con sus imputaciones y las cuentas asociadas
    if (!this.isNew) {
      try {
        await asientoStore.findById({
          id: parseInt(this.$route.params.id, 10),
          imputaciones: true,
          refresh: true
        });
        if (this.asiento) {
          cuentaStore.findByIds({
            ids: this.asiento.imputaciones.map(i => i.cuenta.id)
          });
        }
      } catch (error) {
        // Si no se encuentra el asiento, volver a la pagina de categorias
        if (isNotFound(error)) {
          this.$router.replace(routerService.categoria());
          return;
        }
      }
    } else {
      // Si es nuevo, se busca la ultima fecha de asiento para ofrecerla como predeterminado
      // Si no hay ultimo asiento, se usa la de inicio del ejercicio
      this.suggestedDate = (await asientoApi.getUltimaFechaDeAsiento()) ?? this.minDate ?? null;
    }

    // Se resetea el modelo y las validaciones
    this.$refs.form.reset();
    // Se resetea el flag dirty
    this.$nextTick(() => (this.dirty = false));
  }

  /** Obtiene el DTO del asiento segun el ID de la URL */
  private get asiento(): AsientoDTO | null {
    if (this.isNew) return null;
    return asientoStore.find(parseInt(this.$route.params.id, 10));
  }

  /** Indica si es alta o modificacion */
  private get isNew() {
    return routerService.isCurrent(this.$route, routerService.nuevoAsiento());
  }

  /**
   * Indica si el usuario actual no puede modificar el asiento.
   * Debe tener permisos y el ejercicio debe estar abierto.
   * Ademas, si es una modificacion o baja, NO debe ser un asiento anterior a la fecha confirmada (si existe).
   */
  private get readonly() {
    // Si el usuario no puede modificar asientos en el ejercicio, devolver true
    if (sessionStore.asientosReadonly) return true;

    // Si puede modificar y es nuevo, puede crear (aunque solo a posterior a la fecha confirmada)
    if (this.isNew) return false;

    // Si aun no se cargo el asiento, no hay nada que editar
    if (!this.asiento) return true;

    // Si puede modificar y esta editando uno existente, solo puede hacerlo si no esta dentro de los confirmados
    const fechaConfirmada = sessionStore.ejercicio?.fechaConfirmada;

    // Si no hay fecha confirmada, puede editar cualquiera
    if (!fechaConfirmada) return false;

    // Es readonly si la fecha del asiento es anterior a la confirmada.
    return isBefore(this.asiento.fecha, fechaConfirmada);
  }

  /** Indica si se esta cargando el asiento */
  private get loading() {
    return asientoStore.asientos.status.loading;
  }

  /** Indica si se estan cargando las cuentas asociadas */
  private get loadingImputaciones() {
    return cuentaStore.cuentas.status.loading;
  }

  private get minDate() {
    if (!sessionStore.ejercicio) return undefined;

    // Si hay fecha confirmada, la minima sera el dia dicha fecha
    // Si no hay, sera la de inicio del ejercicio
    return sessionStore.ejercicio.fechaConfirmada ?? sessionStore.ejercicio.inicio;
  }

  private get maxDate() {
    return sessionStore.ejercicio?.finalizacion;
  }

  /** Modifica el titulo segun si es alta o modificacion */
  private get title() {
    if (this.isNew) return 'Nuevo';
    return this.asiento ? '#' + this.asiento.numero : '';
  }

  /** Maneja el foco para cuando se usa TAB o Shift+TAB */
  private onKeyDownGuardar(e: KeyboardEvent) {
    // Si es modo readonly, se deja el foco como esta
    if (this.readonly) return;

    if (e.keyCode === 9) {
      if (e.shiftKey) {
        // Shift Key debe dirigir a la ultima imputacion si existe
        const input = document.querySelector(
          '.imputacion-field:last-child .input-haber .label-input'
        ) as HTMLInputElement | null;
        if (input) {
          e.preventDefault();
          focus(input);
          // input.select();
        }
      } else {
        e.preventDefault();
        this.$refs.form.focus();
      }
    }
  }

  private onChange() {
    this.dirty = true;
  }

  /** Handler para boton de Save */
  private onSaveClick() {
    // Llama a Save del Form que se encarga de validar y armar el DTO
    this.$refs.form.save();
  }

  /** Handler para borrar asiento */
  private onDeleteClick() {
    this.$buefy.dialog.confirm({
      message: '¿Está seguro que desea borrar el asiento?',
      cancelText: 'No',
      confirmText: 'Sí',
      onConfirm: async () => {
        if (this.asiento) {
          await asientoStore.delete(this.asiento);
          this.dirty = false;
          routerService.goToCategoria();
        }
      }
    });
  }

  /** Handler para guardar asiento */
  private async onAsientoSave(asiento: UpstreamAsientoDTO) {
    await asientoStore.save(asiento);
    if (asientoStore.asientos.status.error) return;

    // Deja de estar dirty
    this.dirty = false;

    // Si era Alta, se limpia para crear otro
    if (this.isNew) this.loadAsiento();
  }

  /**
   * Handler de cuando cambia la URL.
   * Se valida si hay cambios pendientes y pide confirmacion
   */
  onLeave(next: (v: false | void) => void) {
    if (!this.dirty) return next();

    this.$buefy.dialog.confirm({
      icon: 'help',
      hasIcon: true,
      message: 'Se perderán los cambios<br>¿Está seguro?',
      onConfirm: () => next(),
      onCancel: () => next(false),
      cancelText: 'No',
      confirmText: 'Sí'
    });
  }
}
</script>

<style lang="scss" scoped></style>
