<template>
  <!--Tablet y Desktop (boton con texto ) -->
  <b-button
    v-if="!isMobile"
    :disabled="disabled"
    :class="buttonClass"
    :type="type"
    :icon-left="icon"
    @click="$emit('click', $event)"
    @keydown.native="$emit('keydown', $event)"
  >
    <slot v-if="$slots.default" />
    <template v-else>{{ label }}</template>
  </b-button>

  <!-- Mobile (solo icono) -->
  <b-tooltip v-else :label="label" position="is-bottom">
    <b-button
      :disabled="disabled"
      :class="buttonClass"
      :type="type"
      :icon-left="icon"
      @click="$emit('click', $event)"
      @keydown.native="$emit('keydown', $event)"
    >
    </b-button>
  </b-tooltip>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { uiStore } from '@/store';

/**
 * Item de la Toolbar (search, boton)
 */
@Component
export default class NavBarButton extends Vue {
  @Prop({ type: Boolean })
  disabled: boolean;

  @Prop()
  buttonClass: string;

  @Prop()
  type: string;

  @Prop()
  icon: string;

  @Prop()
  label: string;

  /** Indica si es ancho mobile */
  private get isMobile() {
    return uiStore.isMobile;
  }
}
</script>

<style lang="scss" scoped></style>
