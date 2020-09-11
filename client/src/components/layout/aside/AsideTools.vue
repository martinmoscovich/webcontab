<template>
  <div class="aside-tools">
    <div class="aside-tools-label">
      <b-icon v-if="icon" :icon="icon" custom-size="default" />
      <slot />
    </div>
    <span style="flex-grow: 1" />
    <a v-if="actionIcon" @click="$emit('action')" class="mt-1 is-hidden-mobile has-text-white">
      <b-icon :icon="actionIcon" />
    </a>
  </div>
</template>

<script>
/** Seccion del Sidebar que incluye un icono, contenido y un icon button de accion */
export default {
  name: 'AsideTools',
  props: {
    icon: {
      type: String,
      default: null
    },
    label: {
      type: String,
      default: null
    },
    actionIcon: {
      type: String,
      default: null
    }
  }
};
</script>
