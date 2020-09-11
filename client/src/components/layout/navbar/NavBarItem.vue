<template>
  <div class="navbar-item" :class="{ dense }">
    <slot />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Item de la Toolbar (search, boton)
 */
@Component
export default class NavBarItem extends Vue {
  /** Elimina el padding */
  @Prop({ type: Boolean })
  dense: boolean;
}
</script>

<style lang="scss" scoped>
.dense {
  padding-left: 0 !important;
  padding-right: 0 !important;
}
</style>
