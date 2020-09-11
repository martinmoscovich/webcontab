<template>
  <div>
    <title-bar :title-stack="titleStack" />
    <hero-bar :has-right-visible="false">
      Dashboard
    </hero-bar>
    <section class="section is-main-section">
      <tiles>
        <card-widget class="tile is-child" type="is-primary" icon="account-multiple" :number="512" label="Clients" />
        <card-widget class="tile is-child" type="is-info" icon="cart-outline" :number="7770" prefix="$" label="Sales" />
        <card-widget
          class="tile is-child"
          type="is-success"
          icon="chart-timeline-variant"
          :number="256"
          suffix="%"
          label="Performance"
        />
      </tiles>

      <CardComponent title="Performance" @header-icon-click="1" icon="finance" header-icon="reload">
        <div v-if="false" class="chart-area"></div>
      </CardComponent>

      <CardComponent title="Clients" class="has-table has-mobile-sort-spaced">
        <!-- <clients-table-sample data-url="/data-sources/clients.json"/> -->
      </CardComponent>
    </section>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import Tiles from '@/components/dashboard/Tiles.vue';
import CardWidget from '@/components/dashboard/CardWidget.vue';
import TitleBar from '@/components/dashboard/TitleBar.vue';
import HeroBar from '@/components/dashboard/HeroBar.vue';

@Component({ components: { CardWidget, Tiles, TitleBar, HeroBar } })
export default class Home extends Vue {
  mounted() {
    this.$buefy.snackbar.open({
      message: 'Welcome back',
      queue: false
    });
  }

  get titleStack() {
    return ['Admin', 'Dashboard'];
  }
}
</script>
