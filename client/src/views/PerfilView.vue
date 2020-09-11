<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar title="Perfil" cuentaSearch />

    <!-- Form ABM -->
    <div class="columns is-centered">
      <div class="column column is-one-third-desktop is-one-quarter-fullhd">
        <CardComponentWithActions title="Información" icon="ballot" dense>
          <UserForm class="pa-3 " ref="form" :value="usuario" @save="onSave" />
        </CardComponentWithActions>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import UserForm from '@/components/login/UserForm.vue';
import { sessionStore } from '../store';
import { userApi } from '../api';
import { logError } from '../utils/log';
import { notificationService } from '../service';
import { UserWithPassword } from '../model/User';

/** Pagina de edicion de Perfil del usuario actual */
@Component({ components: { UserForm } })
export default class PerfilView extends Vue {
  /** Indica que se estan guardando los datos */
  private loading = false;

  /** Datos del usuario actual */
  private get usuario() {
    return sessionStore.user;
  }

  /** Handler que guarda los cambios */
  private async onSave(user: UserWithPassword) {
    try {
      this.loading = true;
      if (!user.id) return;

      // Guarda los cambios y actualiza la sesion con los nuevos datos, para que se reflejen en toda la app
      sessionStore.updateUserInfo(await userApi.updateCurrent(user));

      notificationService.success('Cambios guardados');
    } catch (e) {
      logError('guardando usuario', e);
      notificationService.error(e);
    } finally {
      this.loading = false;
    }
  }
}
</script>

<style lang="scss" scoped></style>
