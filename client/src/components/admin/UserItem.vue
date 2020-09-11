<template>
  <div>
    <article class="media card pa-2 selectable card-list-item" @click="onClick">
      <figure class="media-left">
        <!-- Icono -->
        <p class="image is-32x32">
          <b-icon icon="account" size="is-medium"> </b-icon>
        </p>
      </figure>
      <div class="media-content ml-1">
        <!-- Datos del usuario -->
        <div class="content">
          <p>
            <strong>{{ user.name }}<span class="has-text-success" v-if="current"> (vos)</span></strong>
            <br />
            <small>{{ user.username }}</small>
            <br />
            <small>{{ user.email }}</small>
          </p>
        </div>
      </div>
    </article>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { User } from '@/model/User';

/** Item usuario del listado */
@Component
export default class UserItem extends Vue {
  @Prop()
  user: User;

  /** Indica que este usuario es el autenticado en esta sesion */
  @Prop()
  current: boolean;

  /** Handler cuando se selecciona un usuario para ver/editar */
  private async onClick() {
    this.$emit('click', this.user);
  }
}
</script>

<style lang="scss" scoped>
.card-list-item {
  align-items: center;
}
</style>
