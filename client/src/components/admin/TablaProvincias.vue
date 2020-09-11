<template>
  <b-table
    :row-class="() => 'imputacion-row'"
    :data="provincias"
    narrowed
    hoverable
    :loading="loading"
    @click="onSelected"
    v-click-outside="onClickOutside"
  >
    <template slot-scope="props">
      <b-table-column field="nombre" label="Nombre">
        <Field hideErrorMessage v-if="selected && isSelected(props.row)" :v="validation.allErrors('selected.nombre')">
          <b-input
            ref="txtNombre"
            v-model="selected.nombre"
            @keydown.native.enter="onSaveItem"
            @keydown.native.esc="onCancelItem"
          />
        </Field>
        <template v-else>
          {{ props.row.nombre }}
        </template>
      </b-table-column>

      <b-table-column field="percepcion" label="Percep." numeric>
        <b-input
          v-if="selected && isSelected(props.row)"
          v-model="selected.percepcion"
          @keydown.native.enter="onSaveItem"
          @keydown.native.esc="onCancelItem"
        />
        <template v-else> {{ props.row.percepcion }}</template>
      </b-table-column>
    </template>

    <template slot="empty">
      <section class="section">
        <div class="content has-text-grey has-text-centered">
          <p>
            <b-icon icon="emoticon-sad" size="is-large"> </b-icon>
          </p>
          <p>No hay Provincias</p>
        </div>
      </section>
    </template>
  </b-table>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Provincia } from '@/model/Provincia';
import { Focusable } from '../../utils/browser';
import { required, number } from '../../utils/validation';
import { notificationService } from '../../service';

@Component({
  validators: {
    'selected.nombre': required,
    'selected.percepcion': number({ required: true, min: 0 })
  }
})
export default class TablaProvincias extends Vue {
  @Prop({ type: Array })
  provincias: Provincia[];

  @Prop({ type: Boolean })
  loading: boolean;

  $refs: { txtNombre: Focusable & Vue };

  isSelected(provincia: Provincia) {
    return this.selected?.id === provincia.id;
  }

  selected: Provincia | null = null;

  onClickOutside() {
    this.onCancelItem();
  }

  async onSelected(row: Provincia) {
    if (this.isSelected(row)) return;
    this.selected = {
      id: row.id,
      nombre: row.nombre,
      percepcion: row.percepcion
    };
    await this.$nextTick();
    this.$refs.txtNombre.focus();
  }

  async onSaveItem() {
    if (await this.$validate()) {
      this.$emit('change', this.selected);
      this.selected = null;
    } else {
      notificationService.warn('Ingrese los campos correctamente');
    }
  }

  onCancelItem() {
    this.selected = null;
  }
}
</script>
