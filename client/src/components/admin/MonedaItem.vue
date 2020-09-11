<template>
  <div>
    <article class="media card pa-2 selectable card-list-item" @click="onClick">
      <figure class="media-left">
        <!-- Simbolo -->
        <p class="image is-32x32">
          {{ moneda.simbolo }}
        </p>
      </figure>

      <!-- Nombre y codigo -->
      <div class="media-content ml-1">
        <div class="content">
          <p>
            <strong>{{ moneda.nombre }}</strong>
            <br />
            <small>{{ moneda.codigo }}</small>
          </p>
        </div>
      </div>
    </article>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Moneda } from '../../model/Moneda';

/** Item moneda de la lista */
@Component
export default class MonedaItem extends Vue {
  @Prop()
  moneda: Moneda;

  /** Handler cuando se hace click en una moneda para editar */
  private async onClick() {
    this.$emit('click', this.moneda);
  }
}
</script>
