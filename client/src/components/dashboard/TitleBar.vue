<template>
  <section class="section is-title-bar">
    <div class="level">
      <div class="level-left">
        <div class="level-item">
          <ul>
            <li v-for="(title, index) in titleStack" :key="index">
              {{ title }}
            </li>
          </ul>
        </div>
      </div>
      <div class="level-right">
        <div class="level-item">
          <div class="buttons is-right">
            <a href="https://admin-one-vue-cli.justboil.me/" target="_blank" class="button is-primary">
              <b-icon icon="credit-card" custom-size="default" />
              <span>Premium Demo</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class TitleBar extends Vue {
  @Prop({ type: Array, default: () => [] })
  titleStack: string[];
}
</script>
