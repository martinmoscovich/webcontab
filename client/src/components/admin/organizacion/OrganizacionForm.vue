<template>
  <section>
    <PageNavBar>
      <NavBarItem>
        <!-- Boton de Guardar (en Toolbar) -->
        <NavBarButton
          v-if="!disabled"
          label="Guardar"
          type="is-primary"
          icon="content-save"
          @click="onSaveClick"
          :disabled="disabled || validation.touchedRecords.length === 0"
        />
        <!-- Boton de Borrar (en Toolbar) -->
        <NavBarButton
          v-if="!disabled && !isNew"
          label="Borrar"
          class="ml-1"
          type="is-danger"
          icon="delete"
          :disabled="disabled || isNew"
          @click.stop="onDeleteClick"
        />
      </NavBarItem>
    </PageNavBar>
    <div class="columns">
      <div class="column">
        <!-- CUIT -->
        <Field label="CUIT" hideErrorMessage label-position="on-border" :v="validation.allErrors('org.cuit')">
          <b-input ref="inputCuit" placeholder="CUIT" name="cuit" v-model="org.cuit" :disabled="disabled" />
        </Field>

        <!-- Nombre -->
        <Field
          label="Nombre"
          expanded
          label-position="on-border"
          :v="validation.allErrors('org.nombre')"
          hideErrorMessage
        >
          <b-input v-model="org.nombre" :disabled="disabled" placeholder="Nombre" name="nombre" />
        </Field>
      </div>
    </div>
  </section>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { notificationService } from '@/service';
import { required } from '@/utils/validation';
import { Focusable } from '@/utils/browser';
import { Organizacion } from '@/model/Organizacion';

/** Form de Alta y modificacion de Organizaciones */
@Component({
  validators: {
    'org.cuit': required,
    'org.nombre': required
  }
})
export default class OrganizacionForm extends Vue {
  @Prop()
  value: Organizacion;

  @Prop({ type: Boolean })
  disabled: boolean;

  /** Organizacion temporal para el form */
  private org: Partial<Organizacion> = {};

  /** Indica si es alta o modificacion */
  private isNew = true;

  $refs: { inputCuit: Vue & Focusable };

  private mounted() {
    this.load();
    this.$refs.inputCuit.focus();
  }

  /** Handler cuando cambia la organizacion */
  @Watch('value', { deep: true })
  private onOrgChange() {
    this.load();
  }

  /** Carga la organizacion actual o una nueva */
  private load() {
    // Limpia validaciones
    this.validation.reset();
    if (this.value) {
      // Si es modificacion
      this.org = JSON.parse(JSON.stringify(this.value));
      this.isNew = false;
    } else {
      // Si es nueva
      this.org = {
        nombre: '',
        cuit: ''
      };
      this.isNew = true;
    }
  }

  /** Valida el form */
  private async validate() {
    if (!this.org) return { msg: 'Debe completar los datos' };
    try {
      if (await this.$validate()) {
        return { valid: true };
      }
      return { msg: 'Los datos ingresados no son válidos' };
    } catch {
      return { msg: 'Los datos ingresados no son válidos' };
    }
  }

  /** Handler cuando se guardan los cambios */
  private async onSaveClick() {
    // Valida
    const result = await this.validate();
    if (result.valid) {
      // Emite el evento
      this.$emit('save', this.org);
    } else {
      console.error('La Organización no es valida: %o', result);
      notificationService.error(result.msg ?? 'La Organización no es valida');
    }
  }

  /**
   * Handler cuando se quiere borrar la organizacion
   * Pide confirmacion
   */
  private onDeleteClick() {
    this.$buefy.dialog.confirm({
      title: 'Eliminar organización',
      type: 'is-danger',
      hasIcon: true,
      message: `¿Está seguro que desea <b>eliminar</b> la organzación ${this.org.nombre} (${this.org.cuit})?`,
      onConfirm: () => {
        this.$emit('delete', this.org);
      },
      cancelText: 'Cancelar',
      confirmText: 'Eliminar'
    });
  }
}
</script>

<style lang="scss" scoped></style>
