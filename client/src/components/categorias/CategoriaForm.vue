<template>
  <section>
    <div class="columns">
      <div class="column">
        <!-- Input Numero (readonly) -->
        <Field label="Número" v-if="readonly || !isNew">
          <span class="">{{ categoria.codigo }}</span>
        </Field>

        <!-- Input Numero (modificable) -->
        <Field
          v-else
          label="Número"
          hideErrorMessage
          label-position="on-border"
          :v="validation.allErrors('categoria.numero')"
        >
          <p class="control" v-if="!isRoot">
            <span class="button is-static">{{ codigoPadre }}</span>
          </p>
          <b-input
            :placeholder="readonly ? '' : 'N°'"
            name="numero"
            v-model="categoria.numero"
            type="number"
            min="1"
            :max="maxNumeroPosible"
            @keydown.native="onNumeroKeydown"
          />
        </Field>

        <!-- Input Descripcion -->
        <Field label="Descripción" v-if="readonly">
          <span class="">{{ categoria.descripcion }}</span>
        </Field>
        <Field v-else label="Descripción" expanded :v="validation.allErrors('categoria.descripcion')" hideErrorMessage>
          <b-input
            ref="inputDescripcion"
            v-model="categoria.descripcion"
            :placeholder="readonly ? '' : 'Descripción'"
            name="descripcion"
          />
        </Field>
      </div>

      <!-- Flags -->
      <div class="column is-narrow">
        <!-- Activa -->
        <Field v-if="!isNew" class="mt-1">
          <b-switch v-model="categoria.activa" :disabled="readonly">Activa</b-switch>
        </Field>

        <!-- De Resultado -->
        <Field class="mt-1">
          <b-switch v-model="categoria.resultado" :disabled="readonly">De Resultados</b-switch>
        </Field>
      </div>
    </div>

    <!-- Botonera -->
    <b-field style="justify-content: flex-end">
      <b-field class="mr-1">
        <b-button
          v-if="!readonly"
          type="is-success"
          icon-left="content-save"
          @click="onSave"
          :disabled="validation.touchedRecords.length === 0"
        >
          Guardar
        </b-button>
      </b-field>
      <b-field>
        <b-button
          v-if="!readonly && !isNew"
          class="ml-1"
          type="is-danger"
          icon-left="delete"
          @click="onDelete"
        ></b-button>
      </b-field>
    </b-field>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Categoria, getMaxNumeroHijo } from '@/model/Cuenta';
import { notificationService } from '@/service';
import { required, field } from '@/utils/validation';
import { isNullOrUndefined } from '../../utils/general';
import { Focusable } from '../../utils/browser';
import SimpleVueValidation from 'simple-vue-validator';
import { FormValidation } from '@/model/FormValidation';
const Validator = SimpleVueValidation.Validator;

/**
 * Formulario ABM de Categoria
 */
@Component({
  validators: {
    'categoria.numero': function(this: CategoriaForm, value: string) {
      // El numero debe estar entre 1 y el maximo permitido para el nivel
      return Validator.value(value ?? null)
        .integer()
        .greaterThanOrEqualTo(1)
        .lessThanOrEqualTo(this.maxNumeroPosible);
    },
    'categoria.descripcion': required,
    'categoria.activa': field,
    'categoria.resultado': field
  }
})
export default class CategoriaForm extends Vue {
  /** Categoria a ver/editar */
  @Prop()
  value: Categoria;

  /** Categoria Padre (solo utilizado al crear una nueva) */
  @Prop()
  parent: Categoria;

  /** Indica si no se puede modificar */
  @Prop({ type: Boolean })
  readonly: boolean;

  /** Categoria temporal usada en el form */
  private categoria: Partial<Categoria> = {};

  /** Indica si es una nueva categoria o una modificacion */
  private isNew = true;

  /** Variable donde se guarda el codigo del padre */
  private codigoPadre = '';

  $refs: { inputDescripcion: Vue & Focusable };

  private mounted() {
    this.reset();
    this.$refs.inputDescripcion.focus();
  }

  /** Valida el form */
  async validate(): Promise<FormValidation> {
    if (!this.categoria) return { msg: 'Debe completar los datos' };
    if (!this.categoria.categoriaId && !this.isRoot) return { msg: 'Error en el guardado' };
    try {
      if (await this.$validate()) {
        return { valid: true };
      }
      return { msg: 'Los datos ingresados no son válidos' };
    } catch {
      return { msg: 'Los datos ingresados no son válidos' };
    }
  }

  /** Handler cuando se cambia la categoria a ver/editar */
  @Watch('value', { deep: true })
  private onCategoriaChange() {
    this.reset();
  }

  /** Handler cuando cambia el Padre (se recalcula el codigo y numero) */
  @Watch('parent')
  private onParentChange() {
    if (!this.parent) return;
    this.categoria.categoriaId = this.parent.id;
    if (this.isNew) {
      this.codigoPadre = this.parent?.codigo;
      this.categoria.numero = this.calcularNuevoNumero();
    }
  }

  /** Se resetea el form y se carga segun la categoria actual */
  private reset() {
    this.validation.reset();

    if (this.value) {
      // Categoria a modificar
      this.categoria = JSON.parse(JSON.stringify(this.value));
      this.codigoPadre = this.calcularCodigoPadre();
      this.isNew = false;
    } else {
      // Nueva Categoria
      this.categoria = {
        categoriaId: this.parent?.id,
        activa: true,
        resultado: false,
        descripcion: '',
        numero: this.calcularNuevoNumero()
      };
      this.codigoPadre = this.parent?.codigo;
      this.isNew = true;
    }
  }

  /** Calcula el numero de una categoria nueva en base a su padre */
  private calcularNuevoNumero() {
    const n = getMaxNumeroHijo(this.parent);
    return isNullOrUndefined(n) ? undefined : n + 1;
  }

  /** Obtiene el codigo del padre en base al codigo de la categoria que se esta modificando */
  private calcularCodigoPadre() {
    if (!this.categoria || !this.categoria.codigo) return '';

    // Se obtienen las partes del codigo
    const parts = this.categoria.codigo.split('.');

    // Si es root, no tiene codigo de padre
    if (parts.length === 1) return '';

    // Se quita la ultima parte y se vuelve a unir, obteniendo el codigo del padre
    parts.pop();
    return parts.join('.');
  }

  /**
   * Obtiene el numero de categoria maximo que se permite.
   *
   * Los primeros dos niveles solo pueden tener hasta 99, los siguientes hasta 9999
   */
  private get maxNumeroPosible() {
    // Si hay parent (nueva cat), y no es root (al menos un ancestro) estamos al menos en el 3er nivel
    if (this.parent?.path && this.parent.path.length >= 1) return 9999;

    // Si no hay parent (cat existente), y el path tiene al menos 2 elementos, estamos al menos en el 3er nivel
    if (this.value?.path && this.value.path.length >= 2) return 9999;

    return 99;
  }

  /** Indica que la categoria actual es root */
  private get isRoot() {
    return isNullOrUndefined(this.parent?.id);
  }

  /**
   * Handler para cuando se aprieta una tecla en el input de numero.
   * Solo permite teclas numericas y de navegacion
   */
  private onNumeroKeydown(e: KeyboardEvent) {
    const key = e.keyCode;
    if (
      (!e.shiftKey &&
        !e.altKey &&
        !e.ctrlKey &&
        // numbers
        key >= 48 &&
        key <= 57) ||
      // Numeric keypad
      (key >= 96 && key <= 105) ||
      // Backspace and Tab and Enter
      key == 8 ||
      key == 9 ||
      key == 13 ||
      // Home and End
      key == 35 ||
      key == 36 ||
      // arrows
      key == 37 ||
      key == 38 ||
      key == 39 ||
      key == 40 ||
      // Del and Ins
      key == 46 ||
      key == 45
    ) {
      this.$emit('keydown', e);
      return true;
    }

    e.preventDefault();
  }

  /** Handler para el save del form */
  private async onSave() {
    const result = await this.validate();
    if (result.valid) {
      this.categoria.imputable = false;

      // Lanza el evento
      this.$emit('save', this.categoria);
    } else {
      console.error('La Categoría no es valida: %o', result);
      notificationService.error(result.msg ?? 'La Categoría no es valida');
    }
  }

  /** Handler del boton de Borrar. Pide confirmacion */
  private onDelete() {
    this.$buefy.dialog.confirm({
      message: '¿Está seguro que desea borrar la categoria?',
      // Lanza el evento
      onConfirm: () => this.$emit('delete', this.categoria),
      cancelText: 'No',
      confirmText: 'Sí'
    });
  }
}
</script>

<style lang="scss" scoped></style>
