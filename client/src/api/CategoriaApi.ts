import { queryString, toList, toEntity } from '@/core/ajax/helpers';
import { Categoria, CuentaOCategoria, mapCuentaOCategoria, mapCategoria } from '@/model/Cuenta';
import { AxiosInstance } from 'axios';

const BASE_URL = '/categorias';

export class CategoriaApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Busca una categoria por id
   * @param id id de la categoria
   * @param opts indica si se debe traer el path
   */
  getById(id: number, opts: { path?: boolean } = {}): Promise<Categoria> {
    return this.http.get(`${BASE_URL}/${id}${queryString(opts)}`).then(toEntity(mapCategoria));
  }

  /**
   * Crea una nueva categoria
   * @param cat
   */
  crear(cat: Categoria): Promise<Categoria> {
    return this.http.post(BASE_URL, cat).then(toEntity(mapCategoria));
  }

  /**
   * Actualiza una categoria
   * @param categoria
   */
  update(categoria: Categoria) {
    // Datos modificables de una categoria
    const payload: Partial<Categoria> = {
      descripcion: categoria.descripcion,
      activa: categoria.activa,
      resultado: categoria.resultado
    };
    return this.http.put(`${BASE_URL}/${categoria.id}`, payload).then(toEntity(mapCategoria));
  }

  /**
   * Elimina una categoria
   * @param cuenta
   */
  borrar(cuenta: Categoria): Promise<void> {
    return this.http.delete(`${BASE_URL}/${cuenta.id}`);
  }

  /**
   * Busca una categoria por parte del codigo o descripcion.
   * Se usa en el autocomplete
   * @param query
   */
  search(query: string): Promise<Categoria[]> {
    return this.http.get(BASE_URL + queryString({ query })).then(toList(mapCategoria));
  }

  /** Obtiene las categorias raiz de la organizacion */
  getRootCategories(): Promise<Categoria[]> {
    return this.http.get(BASE_URL + queryString({ root: true })).then(toList(mapCategoria));
  }

  /**
   * Obtiene los hijos directos de una categoria
   * @param id
   */
  getChildren(id: number): Promise<Array<CuentaOCategoria>> {
    return this.http.get(`${BASE_URL}/${id}/hijos`).then(toList(mapCuentaOCategoria));
  }

  /**
   * Genera la URL para pedir la generacion del plan de cuentas
   * @param id si se indica, solo se generara el plan a partir de esta categoria.
   */
  getExportarPlanUrl(id: number | null) {
    let url = this.http.defaults.baseURL + BASE_URL;
    if (id) url += '/' + id;
    return url + '/xls';
  }
}
