<template>
  <section class="section is-title-bar" style="padding: 0; border: none;">
    <div class="level">
      <div class="level-left">
        <div class="level-item">
          <ul>
            <!-- Items del breadcrumb -->
            <li v-for="(title, index) in titleStack" :key="index">
              {{ title }}
            </li>
            <li>
              <!-- Item custom al final -->
              <portal-target name="navbar-title"></portal-target>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Breadcrumb que se muestra como titulo de la Toolbar
 * Permite pasar una lista de items a colocar en el toolbar y tambien
 * un elemento custom al final usando el componente <NavBarTitle>
 */
@Component
export default class NavBarBreadcrumb extends Vue {
  /** Array de items a mostrar en el toolbar */
  @Prop({ type: Array, default: () => [] })
  titleStack: string[];
}
</script>
