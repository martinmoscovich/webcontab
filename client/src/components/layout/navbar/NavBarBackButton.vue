<template>
  <portal to="navbar-back-button">
    <a @click.prevent="$router.back()">
      <b-icon icon="arrow-left" />
    </a>
  </portal>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Muestra un boton de "Atras" en la toolbar usando portales.
 *
 * Por default hace back del navegador.
 *
 * Se usa mediante un componente aparte y portales (en lugar de una prop del toolbar),
 * para poder poner el handler del evento click en cualquier subcomponente y hacer logica custom.
 */
@Component
export default class NavBarBackButton extends Vue {
  /** Indica que se hace una logica custom en el click y NO debe usarse el back del browser */
  @Prop({ type: Boolean })
  custom: boolean;

  /** Handler cuando se hace click */
  private onClick() {
    this.$emit('click');
    if (!this.custom) this.$router.back();
  }
}
</script>
