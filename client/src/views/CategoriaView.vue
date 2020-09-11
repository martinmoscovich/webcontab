<template>
  <section class="section is-main-section" v-if="categoria">
    <!-- Toolbar -->
    <PageNavBar cuentaSearch :title="categoria.descripcion">
      <ToolbarExcelButton v-if="!loading" :url="exportUrl" />
    </PageNavBar>

    <!-- Breadcrumb de categorias -->
    <div class="columns">
      <div class="column">
        <CategoriaHeader :item="categoria" />
      </div>
    </div>

    <!-- Tabs para cambiar entre subcuentas y ABM -->
    <div class="columns">
      <div v-if="!isRoot" class="tabs column">
        <ul style="border: none">
          <li :class="{ 'is-active': activeSection === 'subcuentas' }">
            <a @click="activeSection = 'subcuentas'">Subcuentas</a>
          </li>
          <li :class="{ 'is-active': activeSection === 'form' }">
            <a @click="activeSection = 'form'">{{ readonly ? 'Datos' : 'Editar' }}</a>
          </li>
        </ul>
      </div>

      <!-- Botonera para exportar y crear categorias/cuentas -->
      <div v-if="activeSection === 'subcuentas' && !readonly" class="column is-right" style="flex-grow: 0">
        <CategoriaButtons :exportUrl="exportUrl" :cuentaAllowed="!isRoot && !firstLevel" @new="onNewClick" />
      </div>
    </div>

    <!-- Card de subcuentas -->
    <template v-if="activeSection === 'subcuentas'">
      <CardComponent class="has-table has-mobile-sort-spaced">
        <Subcuentas
          :categoria="categoria"
          @cuentaSelected="onCuentaSelected"
          @categoriaSelected="onCategoriaSelected"
        />
      </CardComponent>
    </template>

    <!-- Form de Modificacion y Baja -->
    <div v-else-if="activeSection === 'form'" class="columns is-centered">
      <CategoriaForm
        :value="categoria"
        :readonly="readonly"
        @save="onCategoriaSave"
        @delete="onCategoriaDelete"
        class="column is-half-fullhd is-three-quarters-widescreen is-four-fifths-tablet form-card "
      />
    </div>

    <!-- Modal para crear cuentas o categorias nuevas -->
    <ModalNuevaCuenta
      :show="!!childFormShow"
      :tipo="childFormShow"
      :categoria="categoria"
      @save="onNewChild"
      @hide="childFormShow = null"
    />
  </section>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import { Categoria, CuentaOCategoria } from '@/model/Cuenta';
import Subcuentas from '@/components/categorias/Subcuentas.vue';
import CategoriaHeader from '@/components/categorias/CategoriaHeader.vue';
import CategoriaForm from '@/components/categorias/CategoriaForm.vue';
import CategoriaButtons from '@/components/categorias/CategoriaButtons.vue';
import ModalNuevaCuenta from '@/components/cuentas/ModalNuevaCuenta.vue';
import { routerService, notificationService } from '@/service';
import { sessionStore, cuentaStore } from '@/store';
import { categoriaApi } from '@/api';

/**
 * Pagina de Categoria.
 * Incluye lista de Subcategorias / cuentas y ABM
 */
@Component({
  components: {
    CategoriaHeader,
    CategoriaForm,
    Subcuentas,
    CategoriaButtons,
    ModalNuevaCuenta
  }
})
export default class CategoriaView extends Vue {
  /** Id de la categoria a mostrar */
  private categoriaId: number | null = null;

  /** Indica si se muestran las subcuentas o el form */
  private activeSection: 'subcuentas' | 'form' = 'subcuentas';

  /** Indica si se muestra el modal de alta y que entidad se esta creando */
  private childFormShow: 'cuenta' | 'categoria' | null = null;

  private async mounted() {
    this.loadCategoria();
  }

  @Watch('$route')
  private onRouteChange() {
    this.loadCategoria();
  }

  /** Indica si es categoria raiz */
  private get isRoot() {
    return this.categoria?.id === null;
  }

  private get firstLevel() {
    if (!this.categoria || !this.categoria.codigo) return true;
    return !this.categoria.codigo.includes('.');
  }

  /** Indica si el usuario solo tiene rol de lectura (no puede modificar) */
  private get readonly() {
    return sessionStore.readonly;
  }

  /** URL donde se obtiene el plan de cuentas en Excel a partir de esta categoria */
  private get exportUrl() {
    return categoriaApi.getExportarPlanUrl(this.categoriaId);
  }

  private get loading() {
    return cuentaStore.cuentas.status.loading;
  }

  /** Obtiene la categoria cacheada a partir del id */
  private get categoria() {
    if (this.categoriaId) {
      const cat = cuentaStore.findCategoria(this.categoriaId);
      // if (cat) {
      //   if (cat.categoriaId && !cat.path) return null;
      // }
      return cat;
    } else {
      // Categorias Root
      // Se crea un placeholder para contenerlas
      return {
        id: null,
        imputable: false,
        numero: null,
        codigo: null,
        resultado: false,
        activa: true,
        descripcion: '',
        path: [],
        children: cuentaStore.root
      };
    }
  }

  /** Carga la categoria a partir del id en la URL (o las root) */
  private async loadCategoria() {
    this.categoriaId = parseInt(this.$route.params.id, 10);
    if (this.categoriaId) {
      cuentaStore.findCategoriaById({
        id: this.categoriaId,
        path: true,
        children: true
      });
    } else {
      cuentaStore.getRootCategories();
    }
    this.activeSection = 'subcuentas';
  }

  /** Handler boton "Nueva" */
  private onNewClick(tipo: 'cuenta' | 'categoria') {
    this.childFormShow = tipo;
  }

  /** Cuando se guardan los cambios de la categoria */
  private async onCategoriaSave(cat: Categoria) {
    if (!cat) return;
    await cuentaStore.actualizar(cat);
    notificationService.info('La categoria fue guardada');
  }

  /** Cuando se pide borrar la categoria */
  private async onCategoriaDelete(cat: Categoria) {
    if (!cat) return;
    // Se guarda el id del padre antes de borrarla
    const categoria = cat.categoriaId;

    // Se borra la categoria
    await cuentaStore.borrar(cat);

    // Se navega al padre o a la raiz
    routerService.goToCategoria(categoria ? { id: categoria } : null);
  }

  /** Handler del alta de subcategoria o subcuenta */
  async onNewChild(c: CuentaOCategoria) {
    if (!c) return;

    // Se crea la cuenta
    await cuentaStore.crear(c);

    notificationService.info(`La ${c.imputable ? 'cuenta' : 'categoria'} fue guardada`);

    // Se oculta el form
    this.childFormShow = null;
  }

  /** Se selecciona una cuenta hija */
  private onCuentaSelected(cuenta: { id: number }) {
    routerService.goToCuenta(cuenta);
  }

  /** Se selecciona una categoria hija */
  private onCategoriaSelected(categoria: { id: number } | null) {
    routerService.goToCategoria(categoria);
  }
}
</script>

<style lang="scss" scoped>
.form-card {
  background-color: #f2f2f2;
  padding: 1em 2em;
  margin-top: 30px;
}
</style>
