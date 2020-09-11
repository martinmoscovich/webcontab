<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar cuentaSearch :title="title" />

    <div class="columns is-centered">
      <div class="column column is-one-third-desktop is-two-fifths-fullhd">
        <!-- Lista de usuarios -->
        <CardComponentWithActions title="Usuarios" icon="ballot" dense list>
          <template v-slot:actions>
            <!-- Boton para crear nuevo -->
            <b-tooltip label="Nuevo Usuario" position="is-bottom" type="is-info">
              <b-button icon-left="plus" @click="onNewClick" />
            </b-tooltip>
          </template>

          <!-- Item -->
          <UserItem
            v-for="user in users"
            :key="user.id || user.username"
            :user="user"
            :current="isCurrent(user)"
            @click="onItemSelected"
          />
        </CardComponentWithActions>
      </div>

      <!-- Form ABM de usuario -->
      <div class="column is-expand">
        <CardComponentWithActions title="Detalles" icon="ballot" dense>
          <UserForm
            class="pa-3 "
            ref="form"
            :value="selected"
            :disabled="isList"
            :loading="loading"
            :requirePassword="isNew"
            @save="onSave"
          />
        </CardComponentWithActions>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import UserItem from '@/components/admin/UserItem.vue';
import UserForm from '@/components/login/UserForm.vue';
import { routerService, notificationService } from '../service';
import { User, UserWithPassword } from '../model/User';
import { userApi } from '../api';
import { byId } from '../utils/array';
import { logError } from '../utils/log';
import { sessionStore } from '../store';

/** Pagina de ABM de usuarios (para admin) */
@Component({ components: { UserItem, UserForm } })
export default class UsuariosView extends Vue {
  /** Lista de usuarios */
  private users: User[] = [];

  /** Indica si esta cargando o guardando */
  private loading = false;

  private mounted() {
    this.loadUsers();
  }

  @Watch('$route')
  private onRouteChange() {
    this.loadUsers();
  }

  /** Carga los usuarios del server */
  private async loadUsers() {
    this.users = await userApi.list();
  }

  /** Titulo del Toolbar */
  private get title() {
    if (this.isList) return 'Usuarios';
    if (this.isNew) return 'Nuevo';
    return this.selected?.name ?? '';
  }

  /** Indica si un usuario es el autenticado */
  private isCurrent(user: User) {
    return user.id === sessionStore.user?.id;
  }

  /** Usuario seleccionado, segun la URL */
  private get selected(): User | undefined {
    if (this.isList || this.isNew) return undefined;
    return this.users.find(byId(parseInt(this.$route.params.id, 10)));
  }

  /** Determina si es un alta de usuario, segun la URL */
  private get isNew() {
    return routerService.isCurrent(this.$route, routerService.usuarioNuevo());
  }

  /** Determina si no hay usuario seleccionado, segun la URL */
  private get isList() {
    return routerService.isCurrent(this.$route, routerService.usuarios());
  }

  /** Handler cuando se selecciona un usuario para ver/editar */
  private onItemSelected(row: User) {
    routerService.goToUsuario(row);
  }

  /** Handler cuando se desea crear un nuevo usuario */
  private onNewClick() {
    routerService.goToNuevoUsuario();
  }

  /** Handler cuando se desea guardar un usuario (alta o modificacion) */
  private async onSave(user: UserWithPassword) {
    try {
      let saved: User | null = null;
      this.loading = true;

      // Crea o modifica
      if (user.id) {
        saved = await userApi.update(user);
      } else {
        saved = await userApi.create(user);
      }

      // Si es el usuario actual, se modifican sus datos en la sesion,
      // para que se vean reflejados en la Toolbar.
      if (sessionStore.user?.id === saved.id) {
        sessionStore.updateUserInfo(saved);
      }

      // Se actualiza el listado
      await this.loadUsers();

      if (saved) {
        if (this.isNew) routerService.goToUsuario(saved);
      } else {
        routerService.goToUsuarios();
      }
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
