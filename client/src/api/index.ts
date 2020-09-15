import { HttpClient } from '@/core/ajax/HttpClient';
import { CuentaApi } from './CuentaApi';
import { AsientoApi } from './AsientoApi';
import { CategoriaApi } from './CategoriaApi';
import { SessionApi } from './SessionApi';
import { OrganizacionApi } from './OrganizacionApi';
import { SimpleApi } from './SimpleApi';
import { Provincia } from '@/model/Provincia';
import { Moneda } from '@/model/Moneda';
import { InformeApi } from '@/api/InformeApi';
import { AdminApi } from '@/api/AdminApi';
import { ImportAPI } from '@/api/ImportApi';
import { UserApi } from '@/api/UserApi';
import { LoginApi } from '@/api/LoginApi';
import { InflacionMes, mapInflacionFromServer } from '@/model/InflacionMes';

// API publica
const publicClient = HttpClient.create({});
export const loginApi = new LoginApi(publicClient);

const httpClient = HttpClient.create({
  baseURL: '/api',
  withCredentials: true,
  delay: 0 //2000
});

export const cuentaApi = new CuentaApi(httpClient);
export const categoriaApi = new CategoriaApi(httpClient);
export const asientoApi = new AsientoApi(httpClient);
export const sessionApi = new SessionApi(httpClient);
export const organizacionApi = new OrganizacionApi(httpClient);
export const informeApi = new InformeApi(httpClient);
export const adminApi = new AdminApi(httpClient);
export const userApi = new UserApi(httpClient);
export const importApi = new ImportAPI(httpClient);
export const provinciaApi = new SimpleApi<Provincia>(httpClient, { baseUrl: '/provincias' });
export const monedaApi = new SimpleApi<Moneda>(httpClient, { baseUrl: '/monedas' });
export const inflacionApi = new SimpleApi<InflacionMes>(httpClient, {
  baseUrl: '/admin/inflacion',
  mapper: mapInflacionFromServer
});
