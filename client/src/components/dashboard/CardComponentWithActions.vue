<template>
  <div class="card" :class="{ dense, 'card-list': list }">
    <header class="card-header level">
      <div class="level-left is-flex">
        <p class="card-header-title">
          <b-icon v-if="icon" :icon="icon" custom-size="default" />
          {{ title }}
        </p>
        <slot name="title-actions" />
        <a
          v-if="headerIcon"
          href="#"
          class="card-header-icon"
          aria-label="more options"
          @click.prevent="headerIconClick"
        >
          <b-icon :icon="headerIcon" custom-size="default" />
        </a>
      </div>
      <div class="level-right" style="padding: 0 15px; margin-top: 0">
        <slot name="actions" />
      </div>
    </header>
    <div class="card-content" :class="{ 'add-padding-bottom': paddingBottom }">
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  name: 'CardComponent',
  props: {
    title: {
      type: String,
      default: null
    },
    icon: {
      type: String,
      default: null
    },
    headerIcon: {
      type: String,
      default: null
    },
    dense: {
      type: Boolean
    },
    list: {
      type: Boolean
    },
    paddingBottom: {
      type: Boolean
    }
  },
  methods: {
    headerIconClick() {
      this.$emit('header-icon-click');
    }
  }
};
</script>
<style lang="scss" scoped>
.dense {
  .card-content {
    padding: 0;
  }
  .card-header {
    margin-bottom: 10px;
  }
  .card-header-title {
    padding: 10px;
  }
}
.card-list {
  .card-header {
    margin-bottom: 0;
  }
}
.add-padding-bottom {
  padding-bottom: 20px;
}
</style>
