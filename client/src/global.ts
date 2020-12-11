import Vue from 'vue';

// Modulos externos
import Buefy from 'buefy';
import PortalVue from 'portal-vue';
import SimpleVueValidation from 'simple-vue-validator';
import vClickOutside from 'v-click-outside';
import VueCurrencyInput from 'vue-currency-input';

// Componentes a registrar globalmente
import HeroBar from '@/components/dashboard/HeroBar.vue';
import CardComponent from '@/components/dashboard/CardComponent.vue';
import CardComponentWithActions from '@/components/dashboard/CardComponentWithActions.vue';

import NavBarItem from '@/components/layout/navbar/NavBarItem.vue';
import NavBarButton from '@/components/layout/navbar/NavBarButton.vue';
import NavBarTitle from '@/components/layout/navbar/NavBarTitle.vue';
import NavBarBackButton from '@/components/layout/navbar/NavBarBackButton.vue';
import PageNavBar from '@/components/layout/navbar/PageNavBar.vue';
import ToolbarExcelButton from '@/components/layout/navbar/ToolbarExcelButton.vue';

import Field from '@/components/common/Field';
import CuentaSearch from '@/components/cuentas/CuentaSearch.vue';
import UsuarioSearch from '@/components/admin/UsuarioSearch.vue';
import MonedaSelect from '@/components/common/MonedaSelect.vue';
import PeriodoInput from '@/components/common/PeriodoInput.vue';
import MediaObject from '@/components/common/MediaObject.vue';
import AuditableLabel from '@/components/common/AuditableLabel.vue';

// Se registran los plugins
Vue.use(SimpleVueValidation);
Vue.use(vClickOutside);
Vue.use(PortalVue);
Vue.use(Buefy);
// "de" es la unica en la que andan bien los separadores
Vue.use(VueCurrencyInput, { globalOptions: { locale: 'de' } });

SimpleVueValidation.extendTemplates({ required: 'Requerido' });

/* Se registran los componentes globales */
Vue.component('HeroBar', HeroBar);
Vue.component('CardComponent', CardComponent);
Vue.component('CardComponentWithActions', CardComponentWithActions);

Vue.component('NavBarItem', NavBarItem);
Vue.component('NavBarButton', NavBarButton);
Vue.component('NavBarTitle', NavBarTitle);
Vue.component('NavBarBackButton', NavBarBackButton);
Vue.component('PageNavBar', PageNavBar);
Vue.component('ToolbarExcelButton', ToolbarExcelButton);

Vue.component('Field', Field);
Vue.component('CuentaSearch', CuentaSearch);
Vue.component('UsuarioSearch', UsuarioSearch);
Vue.component('MonedaSelect', MonedaSelect);
Vue.component('PeriodoInput', PeriodoInput);
Vue.component('MediaObject', MediaObject);
Vue.component('AuditableLabel', AuditableLabel);
