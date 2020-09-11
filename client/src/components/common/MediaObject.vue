<template>
  <article class="media" :class="{ vcentered: verticalCenter }">
    <figure class="media-left">
      <slot name="left">
        <b-icon v-if="icon" size="is-large" :icon="icon" />
      </slot>
    </figure>
    <div class="media-content">
      <div class="content">
        <slot>
          {{ message }}
        </slot>
      </div>
    </div>
    <slot name="right"></slot>
  </article>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Media Object de Bulma
 */
@Component
export default class MediaObject extends Vue {
  /** Mensaje a mostrar */
  @Prop()
  message: string;

  /** Icono a mostrar */
  @Prop()
  icon: string;

  @Prop({ type: Boolean })
  verticalCenter: boolean;
}
</script>
<style lang="scss" scoped>
.vcentered {
  align-items: center;
}
</style>
