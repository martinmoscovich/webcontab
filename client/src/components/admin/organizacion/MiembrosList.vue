<template>
  <nav class="panel">
    <!-- Mensaje de que no hay miembros -->
    <div v-if="miembros.length === 0">
      <p class="subtitle has-text-centered pa-3">Aún no hay usuarios</p>
    </div>

    <!-- Item Miembro -->
    <div v-else class="panel-block" v-for="item in miembros" :key="item.user.id">
      <span class="panel-icon">
        <b-icon icon="account" custom-size="mdi-24px" size="is-small" />
      </span>
      {{ item.user.name }}
      <div style="flex-grow: 1" />

      <!-- Rol -->
      <b-field class="mb-0 mr-3">
        <b-select :value="item.rol" :disabled="item.readonly" @input="onRolChange(item, $event)">
          <option value="READ_ONLY">Solo Lectura</option>
          <option value="USER">Usuario</option>
          <option value="ADMIN">Administrador</option>
        </b-select>
      </b-field>

      <!-- Boton Quitar -->
      <b-button
        :disabled="item.readonly"
        @click.stop="onQuitar(item)"
        class="ml-1"
        size="is-small"
        type="is-danger"
        icon-right="delete"
      ></b-button>
    </div>
  </nav>
</template>
<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Member, Rol } from '@/model/admin/Member';

/** Lista de miembros de una organizacion */
@Component
export default class MiembrosList extends Vue {
  /** Miembros de la org */
  @Prop()
  miembros: Member[];

  /** Handler cuando se pide quitar un miembro */
  private onQuitar(item: Member) {
    this.$emit('quitar', item);
  }

  /** Handler cuando se pide cambiar el rol de un miembro */
  private onRolChange(item: Member, newRol: Rol) {
    this.$emit('change', { ...item, rol: newRol });
  }
}
</script>

<style lang="scss"></style>
