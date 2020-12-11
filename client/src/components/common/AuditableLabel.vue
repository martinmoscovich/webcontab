<template>
  <!-- Loader -->
  <div v-if="loading" class="wc-is-loading" style="margin: 0 0 0 auto"></div>

  <!-- Info de auditoria -->
  <span v-else class="has-text-grey" style="font-size: 0.8rem" v-html="texto" />
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Auditable } from '@/model/Auditable';
import { userStore } from '@/store';
import { isEqualDate, formatDate, formatTime } from '@/utils/date';

/** Label que muestra la info de fecha y usuario de creacion y modificacion */
@Component
export default class AuditableLabel extends Vue {
  @Prop()
  model: Auditable;

  mounted() {
    this.onCreationUserChange();
    this.onUpdateUserChange();
  }

  /**
   * Watch cuando cambia el usuario de creacion
   */
  @Watch('model.creationUser')
  onCreationUserChange() {
    if (!this.model || !this.model.creationUser) return;

    // Se busca en la cache o en el server
    userStore.findById({ id: this.model.creationUser.id, refresh: false });
  }

  /**
   * Watch cuando cambia el usuario de modificacion
   */
  @Watch('model.updateUser')
  onUpdateUserChange() {
    if (!this.model || !this.model.updateUser || this.model.creationUser?.id === this.model.updateUser.id) return;

    // Se busca en la cache o en el server
    userStore.findById({ id: this.model.updateUser.id, refresh: false });
  }

  /** Texto HTML a mostrar */
  get texto() {
    // Si esta cargando o no tiene info, no mostrar nada
    if (this.loading || (!this.hasCreationInfo && !this.hasUpdateInfo)) return '';

    // Si hay creacion pero no actualizacion o son iguales, se muestra el de creacion solo
    if (this.creationEqualsUpdate || (this.hasCreationInfo && !this.hasUpdateInfo)) return this.createdHtml;

    if (this.hasCreationInfo && this.hasUpdateInfo) {
      // Si hay de creacion y update y son el mismo usuario se muestra en una sola linea
      if (this.creationUser?.id === this.updateUser?.id) {
        return `Creado el ${this.getDateTime(this.model.creationDate)} y modificado el ${this.getDateTime(
          this.model.updateDate
        )} por <strong>${this.creationUser?.username}</strong>`;
      } else {
        // Si son distintos se muestran por separado en 2 lineas
        return `${this.createdHtml}<br />${this.updatedHtml}`;
      }
    } else if (!this.hasCreationInfo) {
      // Si solo hay de actualizacion, se muestra ese
      return this.updatedHtml;
    }
    return '';
  }

  /** Texto de creacion */
  private get createdHtml() {
    return `Creado el ${this.getDateTime(this.model.creationDate)} por <strong>${this.creationUser?.username}</strong>`;
  }

  /** Texto de modificacion */
  private get updatedHtml() {
    return `Modificado el ${this.getDateTime(this.model.updateDate)} por <strong>${this.updateUser?.username}</strong>`;
  }

  /** Formato de fecha y hora */
  private getDateTime(date: Date | null) {
    if (!date) return '';
    return `<strong>${formatDate(date)}</strong> a las <strong>${formatTime(date)}</strong>`;
  }

  get hasCreationInfo() {
    return this.creationUser && this.model.creationDate;
  }

  get hasUpdateInfo() {
    return this.updateUser && this.model.updateDate;
  }

  get creationEqualsUpdate() {
    return (
      this.hasCreationInfo &&
      this.hasUpdateInfo &&
      this.updateUser?.id === this.creationUser?.id &&
      isEqualDate(this.model.creationDate, this.model.updateDate)
    );
  }

  get loading() {
    return userStore.lista.status.loading;
  }

  /** Busca el usuario en el store de usuarios */
  get creationUser() {
    if (!this.model.creationUser) return null;
    return userStore.find(this.model.creationUser.id);
  }

  /** Busca el usuario en el store de usuarios */
  get updateUser() {
    if (!this.model.updateUser) return null;
    return userStore.find(this.model.updateUser.id);
  }
}
</script>
