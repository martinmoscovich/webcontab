import Home from '../views/Home.vue';
import InformeDiarioView from '@/views/InformeDiarioView.vue';
import InformeMayorView from '@/views/InformeMayorView.vue';
import InformeBalanceView from '@/views/InformeBalanceView.vue';
import AsientoView from '@/views/AsientoView.vue';
import CategoriaView from '@/views/CategoriaView.vue';
import CuentaView from '@/views/CuentaView.vue';
import LoginView from '@/views/LoginView.vue';
import ProvinciasView from '@/views/ProvinciasView.vue';
import InflacionView from '@/views/InflacionView.vue';
import MonedasView from '@/views/MonedasView.vue';
import UsuariosView from '@/views/UsuariosView.vue';
import { SecureRouteConfig } from './RouteSecurity';
import ListaOrganizacionesView from '@/views/ListaOrganizacionesView.vue';
import ListaEjerciciosView from '@/views/ListaEjerciciosView.vue';
import AdminAjustesView from '@/views/AdminAjustesView.vue';
import PefilView from '@/views/PerfilView.vue';

export default [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      path: ['Admin', 'Home'],
      auth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { path: ['Ingresar'] }
  },
  {
    path: '/categorias/:id?',
    name: 'CategoriaView',
    component: CategoriaView,
    meta: { path: ['Categoria'], auth: true }
  },
  {
    path: '/cuentas',
    redirect: { name: 'CategoriaView' }
  },
  {
    path: '/cuentas/:id',
    name: 'CuentaView',
    component: CuentaView,
    meta: { path: ['Cuenta'], auth: true }
  },
  {
    path: '/informes/diario',
    name: 'InformeDiarioView',
    component: InformeDiarioView,
    meta: { path: ['Informes'], auth: true }
  },
  {
    path: '/informes/mayor',
    name: 'InformeMayorView',
    component: InformeMayorView,
    meta: { path: ['Informes'], auth: true }
  },
  {
    path: '/informes/balance',
    name: 'InformeBalanceView',
    component: InformeBalanceView,
    meta: { path: ['Informes'], auth: true }
  },
  {
    path: '/asientos/nuevo',
    name: 'NuevoAsiento',
    component: AsientoView,
    meta: { path: ['Asientos'], auth: true }
  },
  {
    path: '/asientos/:id',
    name: 'DetalleAsiento',
    component: AsientoView,
    meta: { path: ['Asientos'], auth: true }
  },
  {
    path: '/admin/provincias',
    name: 'ListaProvincias',
    component: ProvinciasView,
    meta: { auth: ['ADMIN'] }
  },
  {
    path: '/admin/monedas',
    name: 'ListaMonedas',
    component: MonedasView,
    meta: { auth: ['ADMIN'] }
  },
  {
    path: '/admin/monedas/nueva',
    name: 'NuevaMoneda',
    component: MonedasView,
    meta: { path: ['Monedas'], auth: ['ADMIN'] }
  },
  {
    path: '/admin/monedas/:id',
    name: 'DetalleMoneda',
    component: MonedasView,
    meta: { path: ['Monedas'], auth: ['ADMIN'] }
  },
  {
    path: '/admin/inflacion',
    name: 'Inflacion',
    component: InflacionView,
    meta: { auth: ['ADMIN'] }
  },
  {
    path: '/admin/usuarios',
    name: 'ListaUsuarios',
    component: UsuariosView,
    meta: { auth: ['ADMIN'] }
  },
  {
    path: '/admin/usuarios/nuevo',
    name: 'NuevoUsuario',
    component: UsuariosView,
    meta: { path: ['Usuarios'], auth: ['ADMIN'] }
  },
  {
    path: '/admin/usuarios/:id',
    name: 'DetalleUsuario',
    component: UsuariosView,
    meta: { path: ['Usuarios'], auth: ['ADMIN'] }
  },
  {
    path: '/ejercicios',
    name: 'ListaEjercicios',
    component: ListaEjerciciosView,
    meta: { auth: true }
  },
  {
    path: '/admin/organizaciones',
    name: 'ListaOrganizaciones',
    component: ListaOrganizacionesView,
    meta: { auth: ['ADMIN'] }
  },
  {
    path: '/admin/organizaciones/nueva',
    name: 'NuevaOrganizacion',
    component: ListaOrganizacionesView,
    meta: { path: ['Organizaciones'], auth: ['ADMIN'] }
  },
  {
    path: '/admin/organizaciones/:id',
    name: 'DetalleOrganizacion',
    component: ListaOrganizacionesView,
    meta: { path: ['Organizaciones'], auth: ['ADMIN'] }
  },
  {
    path: '/admin/ajustes',
    name: 'AdminAjustes',
    component: AdminAjustesView,
    meta: { path: ['Ajustes'], auth: ['ADMIN'] }
  },
  {
    path: '/perfil',
    name: 'Perfil',
    component: PefilView,
    meta: { auth: true }
  }

  // {
  //   path: "/about",
  //   name: "About",
  //   // route level code-splitting
  //   // this generates a separate chunk (about.[hash].js) for this route
  //   // which is lazy-loaded when the route is visited.
  //   component: () =>
  //     import(/* webpackChunkName: "about" */ "../views/About.vue")
  // }
] as SecureRouteConfig[];
