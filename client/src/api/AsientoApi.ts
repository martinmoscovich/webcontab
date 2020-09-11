import { queryString, toEntity, toPage, searchOptionsToQuerystring } from '@/core/ajax/helpers';
import { SearchOptions } from '@/core/ajax/model';
import Page from '@/core/Page';
import { AsientoDTO, mapAsientoFromServer, UpstreamAsientoDTO } from '@/model/AsientoDTO';
import { Periodo } from '@/model/Periodo';
import { AxiosInstance } from 'axios';
import { Dictionary } from 'vue-router/types/router';
import { parseServerDate } from '@/utils/date';

const BASE_URL = '/asientos';

/** Filtro para asientos */
export interface AsientosSearchFilter extends Partial<Periodo> {
  /** Numero minimo de asiento a buscar */
  min?: number;

  /** Numero maximo de asiento a buscar */
  max?: number;
}

/** Query Params para busqueda de asientos */
export interface AsientosSearchOptions extends SearchOptions<AsientosSearchFilter> {
  /** Indica si se deben incluir las imputaciones de cada asiento */
  imputaciones?: boolean;
}

/**
 * API que maneja los asientos
 */
export class AsientoApi {
  constructor(private http: AxiosInstance) {}

  /**
   * Crea un nuevo asiento
   * @param asiento datos del asiento y sus imputaciones
   */
  crear(asiento: UpstreamAsientoDTO): Promise<AsientoDTO> {
    return this.http.post(BASE_URL, asiento).then(toEntity(mapAsientoFromServer));
  }

  /**
   * Actualiza un asiento existente y sus imputaciones
   * @param asiento datos del asiento y sus imputaciones
   */
  actualizar(asiento: UpstreamAsientoDTO): Promise<AsientoDTO> {
    return this.http.put(`${BASE_URL}/${asiento.id}`, asiento).then(toEntity(mapAsientoFromServer));
  }

  /**
   * Elimina un asiento
   * @param asiento
   */
  borrar(asiento: AsientoDTO): Promise<void> {
    return this.http.delete(`${BASE_URL}/${asiento.id}`);
  }

  /**
   * Obtiene un asiento por id
   * @param id id del asiento
   * @param opts indica si se deben traer las imputaciones
   */
  getById(id: number, opts?: { imputaciones: boolean }): Promise<AsientoDTO> {
    return this.http.get(`${BASE_URL}/${id}${queryString(opts)}`).then(toEntity(mapAsientoFromServer));
  }

  /**
   * Obtiene la ultima fecha de asiento del ejercicio.
   *
   * Esta fecha es la mas cercana a la actual y no necesariamente es del ultimo asiento cargado.
   */
  getUltimaFechaDeAsiento(): Promise<Date | null> {
    return this.http.get(`${BASE_URL}/last/date`).then(toEntity(parseServerDate));
  }

  /**
   * Busca una pagina de asientos en base al filtro
   * @param opts datos de paginacion, filtro de asientos e indicacion de si traer las imputaciones
   */
  search(opts: AsientosSearchOptions): Promise<Page<AsientoDTO>> {
    const qs: Dictionary<string> = searchOptionsToQuerystring(opts);
    if (opts.imputaciones) qs.imputaciones = 'true';

    return this.http.get(`${BASE_URL}/${queryString(qs)}`).then(toPage(mapAsientoFromServer));
  }

  /**
   * Obtiene el ultimo asiento creado para el ejercicio
   */
  // async getLast(): Promise<AsientoDTO | null> {
  //   try {
  //     Se pone el awair para que haga use el try-catch
  //     return await this.http.get(`${BASE_URL}/last`).then(toEntity(mapAsientoFromServer));
  //   } catch (e) {
  //     Si es not found, devolver null (no es error)
  //     if (isNotFound(e)) return null;

  //     Cualquier otro error, lanzarlo
  //     throw e;
  //   }
  // }
}
