<template>
  <CardComponentWithActions title="Usuarios" icon="ballot" dense>
    <template v-slot:actions>
      <!-- Busqueda de usuarios para agregar -->
      <UsuarioSearch @input="onUsuarioSelected" :exclude="miembrosIds" />
    </template>

    <!-- Lista de miembros -->
    <MiembrosList :miembros="miembros" mostarAcciones class="pa-2" @change="save" @quitar="onQuitar" />

    <!-- Loader -->
    <b-loading :is-full-page="false" :active="loading" />
  </CardComponentWithActions>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import MiembrosList from './MiembrosList.vue';
import { Organizacion } from '@/model/Organizacion';
import { Member } from '@/model/admin/Member';
import { notificationService } from '@/service';
import { logError } from '@/utils/log';
import { organizacionApi } from '@/api';
import { User } from '@/model/User';

/**
 * Panel de ABM de miembros de una organizacion.
 * Permite agregar (usando busqueda) y quitar.
 */
@Component({ components: { MiembrosList } })
export default class CardMiembros extends Vue {
  @Prop()
  organizacion: Organizacion;

  /** Indica que se esta cargando la organizacion */
  @Prop({ type: Boolean })
  loading: boolean;

  /** Miembros de la organizacion obtenidos del server */
  private miembros: Member[] = [];

  /** Indica que se estan buscando los miembros, agregando o quitando uno */
  private loadingMiembros = false;

  private mounted() {
    this.loadMembers();
  }

  /**
   * Handler cuando cambia la organizacion.
   */
  @Watch('organizacion')
  private onOrganizacionChange() {
    this.loadMembers();
  }

  /** Obtienen los ids de los miembros para excluirlos de la busqueda al agregar */
  private get miembrosIds() {
    return this.miembros.map(m => m.user.id);
  }

  /**
   * Busca los miembros de la organizacion actual en el server.
   */
  private async loadMembers() {
    // Si no hay org, la lista esta vacia
    if (!this.organizacion) {
      this.miembros = [];
      return;
    }

    // Se buscan los miembros
    try {
      this.loadingMiembros = true;
      this.miembros = await organizacionApi.findMiembros(this.organizacion);
    } catch (e) {
      notificationService.error(e);
      logError('buscando miembros', e);
    } finally {
      this.loadingMiembros = false;
    }
  }

  /** Handler cuando se selecciona un usuario del campo de busqueda para agregar en la org */
  private onUsuarioSelected(user: User) {
    this.save({ user, organizacionId: this.organizacion.id, rol: 'USER', readonly: false });
  }

  /**
   * Agrega un miembro en la organizacion o modifica su rol.
   * Luego actualiza la lista
   */
  private async save(item: Member) {
    await organizacionApi.addMember(item);
    this.loadMembers();
  }

  /** Quita un miembro de la organizacion */
  private async onQuitar(item: Member) {
    try {
      this.loadingMiembros = true;
      await organizacionApi.removeMember(item);
      this.loadMembers();
    } catch (e) {
      notificationService.error(e);
      logError('eliminando miembro', e);
    } finally {
      this.loadingMiembros = false;
    }
  }
}
</script>
