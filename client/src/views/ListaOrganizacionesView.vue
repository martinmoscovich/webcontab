<template>
  <section class="section is-main-section">
    <!-- Toolbar -->
    <PageNavBar cuentaSearch :title="title" />

    <div class="columns is-centered">
      <!-- Lista -->
      <div class="column column is-one-third-desktop is-one-quarter-fullhd">
        <OrganizacionesList :items="organizaciones" @selected="onItemSelected" @enter="onEnter" @new="onNewClick" />
      </div>

      <div class="column is-expand">
        <!-- Form -->
        <CardComponentWithActions title="Detalles" icon="ballot" dense>
          <OrganizacionForm
            class="pa-3 "
            ref="form"
            :value="selected"
            :disabled="isList"
            :loading="loading"
            @save="onSave"
            @delete="onDelete"
          />
        </CardComponentWithActions>

        <!-- Miembros -->
        <CardMiembros v-if="!isList && !isNew" :organizacion="selected" :loading="loading" />
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import OrganizacionesList from '@/components/admin/organizacion/OrganizacionesList.vue';
import OrganizacionForm from '@/components/admin/organizacion/OrganizacionForm.vue';
import CardMiembros from '@/components/admin/organizacion/CardMiembros.vue';
import { routerService } from '../service';
import { Organizacion } from '../model/Organizacion';
import { organizacionStore, sessionStore } from '../store';

/** Pagina de ABM de Organizaciones */
@Component({
  components: { OrganizacionesList, OrganizacionForm, CardMiembros }
})
export default class ListaOrganizacionesView extends Vue {
  private mounted() {
    this.loadOrgs();
  }

  /** Carga las organizacione del server */
  private loadOrgs() {
    organizacionStore.list({ refresh: true });
  }

  /** Titulo del toolbar */
  private get title() {
    // Si no hay org seleccionada
    if (this.isList) return 'Organizaciones';

    // Si es nueva
    if (this.isNew) return 'Nueva';

    // Mostrar el nombre de la seleccionada
    return this.selected?.nombre ?? '';
  }

  /** Organizaciones cargadas */
  private get organizaciones() {
    return organizacionStore.lista.items;
  }

  /** Indica si se estan cargando */
  private get loading() {
    return organizacionStore.lista.status.loading;
  }

  /** Obtiene la organizacion seleccionada de la URL, si existe */
  private get selected(): Organizacion | undefined {
    if (this.isList || this.isNew) return undefined;
    return organizacionStore.find(parseInt(this.$route.params.id, 10));
  }

  /** Indica si se esta creando una organizacion nueva, segun la URL */
  private get isNew() {
    return routerService.isCurrent(this.$route, routerService.nuevaOrganizacion());
  }

  /** Indica que no hay ninguna organizacion seleccionada, segun la URL */
  private get isList() {
    return routerService.isCurrent(this.$route, routerService.organizaciones());
  }

  /** Handler cuando se selecciona una organizacion, se muestra el form */
  private onItemSelected(row: Organizacion) {
    routerService.goToOrganizacion(row);
  }

  /** Handler cuando se desea ingresar en una organizacion */
  private async onEnter(row: Organizacion) {
    if (sessionStore.enOrganizacion) {
      // Si hay una seleccionada, se sale de esa y entra en la nueva
      await sessionStore.salirDeOrganizacion({ nueva: row });
    } else {
      // Si no, se entra en la nueva
      await sessionStore.seleccionarOrganizacion(row);
    }

    // Al ingresar en una org, se limpia la cache, hay que volver a obtener las organizaciones
    this.loadOrgs();
  }

  /** Handler cuando se hace click en "Nueva" */
  private onNewClick() {
    routerService.goToNuevaOrganizacion();
  }

  /** Handler para guardar los cambios en una organizacion */
  private async onSave(org: Organizacion) {
    // Se guarda
    await organizacionStore.save(org);
    // Si hubo error, no se hace nada
    if (organizacionStore.lista.status.error) return;

    // Si la que se guardo era nueva, se muestra su info
    if (organizacionStore.lastSaved) {
      if (this.isNew) {
        routerService.goToOrganizacion(organizacionStore.lastSaved);
      }
    } else {
      routerService.goToOrganizaciones();
    }
  }

  /** Handler para borrar una organizacion */
  private async onDelete(org: Organizacion) {
    const esOrgActual = org.id === sessionStore.organizacion?.id;

    // Borrar
    await organizacionStore.eliminar(org);

    // Si fallo, no hacer nada
    if (organizacionStore.lista.status.error) return;

    // Si la organizacion que se borro era la actual, salir
    if (esOrgActual) sessionStore.salirDeOrganizacion();
  }
}
</script>

<style lang="scss" scoped></style>
