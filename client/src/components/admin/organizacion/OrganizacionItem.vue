<template>
  <div>
    <article class="media card pa-2 selectable card-list-item" @click="onClick">
      <!-- Icono -->
      <figure class="media-left">
        <p class="image is-32x32">
          <b-icon icon="office-building" size="is-large" />
        </p>
      </figure>

      <!-- Nombre y CUIT  -->
      <div class="media-content ml-1">
        <div class="content">
          <p>
            <strong>{{ organizacion.nombre }}</strong>
            <br />
            <small>{{ organizacion.cuit }}</small>
          </p>
        </div>
      </div>

      <!-- Boton para ingresar -->
      <b-button class="ml-1" size="is-small" type="is-success" icon-right="share" @click.stop="onEnter"></b-button>
    </article>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Organizacion } from '@/model/Organizacion';

/** Item de organizacion dentro de la lista */
@Component
export default class OrganizacionItem extends Vue {
  @Prop()
  organizacion: Organizacion;

  /** Handler cuando se hace click en un item para editar */
  private onClick() {
    this.$emit('click', this.organizacion);
  }

  /** Handler cuando se desea ingresar a una organizacion */
  private onEnter() {
    this.$emit('selected', this.organizacion);
  }
}
</script>
