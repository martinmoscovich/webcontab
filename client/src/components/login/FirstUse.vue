<template>
  <div class="card">
    <div class="card-content">
      <div class="media">
        <div class="media-content ">
          <p class="title is-4 has-text-black">Bienvenido a WebContab</p>
          <p class="subtitle is-6 has-text-black">
            Registre al usuario Administrador
          </p>
        </div>
      </div>

      <!-- Formulario de registro de usuario -->
      <UserForm requirePassword :loading="saving" @save="onSave" />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import UserForm from './UserForm.vue';
import { UserWithPassword } from '../../model/User';
import { userApi } from '../../api';
import { notificationService } from '../../service';
import { logError } from '../../utils/log';
import { delay } from '../../utils/delay';

/**
 * Panel de registro de primer usuario, cuando no existe ninguno (primer uso).
 */
@Component({ components: { UserForm } })
export default class FirstUse extends Vue {
  /** Indica que se esta guardando el usuario */
  private saving = false;

  /** Handler cuando se guarda el usuario */
  private async onSave(user: UserWithPassword) {
    try {
      this.saving = true;

      // Se crea
      await userApi.createFirst(user);

      // Se notifica y se reinicia
      notificationService.success('Usuario creado, reiniciando...');
      await delay(1000);
      window.location.reload();
    } catch (e) {
      notificationService.error(e);
      logError('creando usuario', e);
    } finally {
      this.saving = false;
    }
  }
}
</script>

<style lang="stylus" scoped></style>
