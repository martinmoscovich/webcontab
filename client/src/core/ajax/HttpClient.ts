import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

import { delay } from '@/utils/delay';
import { RemoteError, ConnectionError, ApplicationError, WebContabError, isNotAuthenticated } from './error';
import { sessionStore } from '@/store';
import { notificationService } from '@/service';

/**
 * Crea un interceptor de request que agrega un delay al proceso
 *
 * @param {number} ms milisegundos de delay a agergar
 * @returns el interceptor
 */
function createDelayInterceptor(ms: number) {
  return (config: AxiosRequestConfig) => delay(ms).then(() => config);
}

export const HttpClient = {
  /**
   * Crea un Cliente HTTP de Axios.
   * En caso de especificar configuracion extra, se agregan los interceptors correspondientes.
   *
   * @param {HttpClientConfig} config Configuracion que se pasa a Axios y agrega `identityProvider` y `delay`.
   * @returns instancia de axios (cliente Ajax)
   */
  create(config: HttpClientConfig) {
    // Se crea el cliente
    const client: AxiosInstance = axios.create(config);

    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    client.interceptors.response.use(r => r, adaptErrors);

    // Si se especifico delay, se agrega el interceptor
    if (config.delay) client.interceptors.request.use(createDelayInterceptor(config.delay));

    return client;
  }
};

/**
 * Configuracion del cliente HTTP.
 *
 * Contiene los atributos de configuracion de Axios y:
 *  - `delay`
 *  - `identityProvider`
 *
 * @export
 * @interface HttpClientConfig
 * @extends {AxiosRequestConfig}
 */
export interface HttpClientConfig extends AxiosRequestConfig {
  /**
   * Delay en milisegundos que se espera antes de devolver la respuesta.
   *
   * Util para **Desarrollo**, de manera de simular latencia de red.
   *
   * **No usar en produccion!**.
   *
   * Deshabilitado por default (delay = 0).
   */
  delay?: number;
}

function handleGlobalErrors(error: WebContabError): WebContabError {
  if (isNotAuthenticated(error)) {
    if (sessionStore.authenticated && error.code === 'ejercicio_no_seleccionado') {
      notificationService.warn('Debe seleccionar un ejercicio');
      sessionStore.salirDeEjercicio({ keepPath: true });
    } else if (!sessionStore.authenticated && sessionStore.status.loading) {
      // Error de login, se debe usar devolver el error
      console.warn('Error al loguearse');
      return error;
    } else {
      notificationService.warn('Problema de autenticación, ingrese de nuevo');
      sessionStore.logout({ keepPath: true });
    }
  }

  return error;
}

/**
 * Interceptor de errores que adapta los errores de Axios a las
 * excepciones PJN.
 *
 * @param {*} error response erroneo de axios
 * @returns la nueva excepcion
 */
function adaptErrors(error: AxiosError) {
  let wcError;
  if (error.response) {
    // El request se envio y el servidor respondio con un status > 299
    const response: AxiosResponse = error.response;

    wcError = new RemoteError(response.status, response.data.code, response.data.description);
    console.error(
      'ERROR en la respuesta. URL: %s, Status: %d, Headers: %s -> Error generado: %o',
      error.config.url,
      error.response.status,
      error.response.headers,
      wcError
    );
  } else if (error.request) {
    // El request se envio pero no se recibio respuesta del server
    // `error.request` es una instancia de XMLHttpRequest en el navegador
    wcError = new ConnectionError();
    console.error(
      'Error de conexion. URL: %s, XMLHttpRequest: %o -> Error generado: %o',
      error.config.url,
      error.request,
      wcError
    );
  } else {
    // Un error ocurrio al generar el request
    wcError = new ApplicationError('Error al generar el request');
    console.error(
      'Error al generar el request "%s". Config: %o -> Error generado: %o',
      error.message,
      error.config,
      wcError
    );
  }

  console.error(error.config);

  // Se lanza la excepcion creada en lugar de la original
  return Promise.reject(handleGlobalErrors(wcError));
}
