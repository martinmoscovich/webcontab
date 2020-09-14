import { sessionApi, loginApi } from '@/api';
import { LoadingStatus } from '@/core/ui/loading';
import { Ejercicio } from '@/model/Ejercicio';
import { User } from '@/model/User';
import { notificationService, routerService } from '@/service';
import { Action, Module, Mutation, VuexModule } from 'vuex-class-modules';
import { organizacionStore, initAuthenticated } from '@/store';
import { Organizacion } from '@/model/Organizacion';
import { Session } from '@/model/Session';
import { logError } from '@/utils/log';
import { isDefined } from '@/utils/general';
import { Rol } from '@/model/admin/Member';
import { resetStore } from '@/store';
import { isEqualOrAfter } from '@/utils/date';

/**
 * Estado de sesion.
 * DTO que se cachea, solo usando los ids
 */
interface SesionState {
  user: User;
  ejercicioId: number | null;
  organizacionId: number | null;
  roles: string[];
}

/**
 * Store que maneja la sesion de usuario
 */
@Module
export class SessionStore extends VuexModule {
  /** Indica si se cargo la sesion (puede estar logueado o no) */
  appStarted = false;

  /** Indica si es la primera vez que se usa la aplicacion (no existen usuarios) */
  firstUse = false;

  /** Contiene los datos de la sesion */
  sesion: SesionState | null = null;

  status: LoadingStatus = {
    error: false,
    loading: false
  };

  /** Vuelve el store a su estado inicial */
  @Mutation
  reset() {
    this.sesion = null;
    this.appStarted = false;
    this.status = {
      error: false,
      loading: false
    };
  }

  /** Obtiene el usuario logueado en la sesion o null si es anonimo */
  get user() {
    if (!this.sesion) return null;
    return this.sesion.user;
  }

  /** Indica si existe un usuario logueado en la sesion */
  get authenticated() {
    return !!this.user;
  }

  /** Indica si el usuario actual tiene el rol general especificado */
  get hasRole() {
    return (role: Rol) => {
      if (!this.sesion) return false;
      return this.sesion.roles.includes(role);
    };
  }

  /** Indica si el usuario actual tiene el rol especificado dentro de la organizacion */
  get hasOrgRole() {
    return (role: Rol) => {
      if (!this.sesion) return false;
      return this.sesion.roles.includes('ORG:' + role);
    };
  }

  /**
   * Obtiene la organizacion actual de la sesion o null si no se selecciono ninguna
   */
  get organizacion() {
    if (!this.sesion || !this.sesion.organizacionId) return null;
    const org = organizacionStore.find(this.sesion.organizacionId) ?? null;
    return org;
  }

  /**
   * Obtiene el ejercicio actual de la sesion o null si no se selecciono ninguno
   */
  get ejercicio() {
    if (!this.sesion || !this.sesion.ejercicioId) return null;
    return organizacionStore.findEjercicioByIdLocal(this.sesion.ejercicioId) ?? null;
  }

  /** Indica si se selecciono una organizacion en la sesion */
  get enOrganizacion() {
    return this.authenticated && isDefined(this.sesion?.organizacionId);
  }

  /** Indica si se selecciono un ejercicio en la sesion */
  get enEjercicio() {
    return this.enOrganizacion && isDefined(this.sesion?.ejercicioId);
  }

  /** Indica si el usuario solo tiene rol de lectura (no puede modificar) */
  get readonly() {
    return this.hasOrgRole('READ_ONLY');
  }

  /** Indica si el usuario actual no puede modificar asientos */
  get asientosReadonly() {
    // Si solo tiene rol de lectura, es true
    if (this.readonly) return true;

    if (this.ejercicio) {
      // Si puede modificar pero el ejercicio finalizo, tambien es true
      if (this.ejercicio.finalizado) return true;

      // Si no finalizo, pero la fecha confirmada es igual a la de finalizacion, no quedan fechas para modificar asientos
      const fechaConfirmada = this.ejercicio.fechaConfirmada;
      if (fechaConfirmada && isEqualOrAfter(fechaConfirmada, this.ejercicio.finalizacion)) return true;
    }

    // Caso contrario, puede modificar
    return false;
  }

  /**
   * Inicializa la sesion, buscando si el usuario esta logueado.
   * Al iniciar la app, puede que el usuario aun este logueado por cookie.
   */
  @Action
  async start() {
    try {
      this.setStatus({ loading: true });

      // Obtiene la sesion actual (que puede tener usuario o no)
      const session = await sessionApi.check();

      // Actualiza el store con la sesion
      this.updateSesion(session);

      // Si esta autenticado, buscar los datos iniciales segurizados
      if (this.authenticated) initAuthenticated();

      this.setStatus({ loading: false });

      // Se indica que la app ya hizo la carga inicial (quita el loader inicial)
      this.setStarted(true);
    } catch (e) {
      if (e.status === 409 && e.code === 'no_users') {
        // Fallo por primer uso, se indica que ya inicio pero es el primer uso
        console.warn('Primer uso');
        this.setFirstUse(true);
        this.setStatus({ loading: false });
        this.setStarted(true);
      } else if (e.status === 401 && e.code === 'usuario_no_autenticado') {
        // TODO puede ocurrir?
        this.setStatus({ loading: false });
        this.setStarted(true);
      } else {
        this.setStatus({ error: true });
      }
    }
  }

  /**
   * Autentica un usuario
   * @param credentials credenciales del usuario
   */
  @Action
  async login(credentials: { username: string; password: string }) {
    try {
      this.setStatus({ loading: true });

      // Si esta autenticado, primero se hace logout
      if (this.authenticated) await this.logout();

      // Se hace login en el server
      const session = await loginApi.login(credentials.username, credentials.password);

      // Se actualiza la sesion
      this.updateSesion(session);

      // Si esta autenticado, buscar los datos iniciales segurizados
      if (this.authenticated) initAuthenticated();

      // Se muestra mensaje de bienvenida
      notificationService.info('Bienvenido, ' + session.user?.name);

      this.setStatus({ loading: false });
    } catch (e) {
      // Si falla, se limpia cualquier dato de sesion existente
      this.removeSesion();
      this.setStatus({ error: true });
      throw e;
    }
  }

  /**
   * Selecciona una organizacion como activa en la sesion.
   * Esto cambia el contexto, incluyendo los roles
   * @param organizacion
   */
  @Action
  async seleccionarOrganizacion(organizacion: Organizacion) {
    try {
      this.setStatus({ loading: true });

      // Se limpia la sesion actuaal, sacando org, ejercicio y roles existentes
      this.updateSesion({
        user: this.user,
        organizacion: null,
        roles: [],
        ejercicio: null
      });

      // Se llama al server
      const session = await sessionApi.seleccionarOrganizacion(organizacion);

      // Se actualiza la sesion
      this.updateSesion(session);

      this.setStatus({ loading: false });
    } catch (e) {
      this.setStatus({ error: true });
      logError('seleccionar organizacion', e);
      notificationService.error('Error de comunicación');
    }
  }

  /**
   * Selecciona un ejercicio como activo en la sesion.
   * @param ejercicio
   */
  @Action
  async seleccionarEjercicio(ejercicio: Ejercicio) {
    try {
      this.setStatus({ loading: true });

      // Se remueve el ejercicio que estaba cargado (pero se mantiene org y roles)
      this.updateSesion({
        user: this.user,
        organizacion: this.organizacion,
        roles: this.sesion?.roles ?? [],
        ejercicio: null
      });

      // Se llama al server
      const session = await sessionApi.seleccionarEjercicio(ejercicio);

      // Se actualiza la sesion
      this.updateSesion(session);

      this.setStatus({ loading: false });
    } catch (e) {
      this.setStatus({ error: true });
      logError('seleccionar organizacion', e);
      notificationService.error('Error de comunicación');
    }
  }

  /**
   * Finaliza la sesion actual
   * @param opts indica si debe mantener el path actual luego del proximo login
   */
  @Action
  async logout(opts?: { keepPath?: boolean }) {
    try {
      this.setStatus({ loading: true });

      // Llama al server para terminar la sesion
      await sessionApi.logout();

      // Remueve la sesion local
      this.removeSesion();

      // Se redirige al usuario al login
      routerService.goToLogin({ redirect: opts?.keepPath });

      this.setStatus({ loading: false });
    } catch {
      this.setStatus({ error: true });
      console.error('Error al hacer logout');
      notificationService.error('Error de comunicación');
    }
  }

  /**
   * Remueve la organizacion activa de la sesion (y por lo tanto los roles),
   * manteniendo el usuario actual
   */
  @Action
  async salirDeOrganizacion(opts?: { nueva?: Organizacion }) {
    try {
      this.setStatus({ loading: true });

      // Se resetea el store incluyendo org, cuentas y asientos
      resetStore(true);

      // Si hay una org seleccionada
      if (this.enOrganizacion) {
        // Se llama al server
        const session = await sessionApi.salirDeOrganizacion();
        // Se actualiza la sesion
        this.updateSesion(session);
      } else {
        // Si no hay org seleccionada, no se hace nada
        console.warn('Se pidio salir de la organizacion pero no hay una organizacion activa');
      }

      if (!opts?.nueva) {
        // Si no se selecciono nueva, se vuelve al login (donde se mostrara seleccion de org)
        routerService.goToLogin();
      }

      this.setStatus({ loading: false });

      // Si se especifico una org nueva, se ingresa automaticamente
      if (opts?.nueva) {
        await this.seleccionarOrganizacion(opts.nueva);
      }
    } catch (e) {
      this.setStatus({ error: true });
      logError('salir de organzacion', e);
      notificationService.error('Error de comunicación');
    }
  }

  /**
   * Remueve el ejercicio activo de la sesion (manteniendo usuario, organizacion y roles)
   * @param opts indica si mantener el path luego de seleccionar el proximo y se puede especificar un ejercicio para ingresar automaticamente
   * luego de salir del actual
   */
  @Action
  async salirDeEjercicio(opts?: { keepPath?: boolean; nuevo?: Ejercicio }) {
    try {
      this.setStatus({ loading: true });

      // Se resetea el store de asientos
      resetStore(false);

      // Si hay ejercicio seleccionado
      if (this.enEjercicio) {
        // Se llama al server
        const session = await sessionApi.salirDeEjercicio();
        // Se actualiza la sesion
        this.updateSesion(session);

        if (!opts?.nuevo) {
          // Si no se selecciono nuevo, Se redirige al login
          routerService.goToLogin({ redirect: opts?.keepPath });
        }

        this.setStatus({ loading: false });
      } else {
        // Si no hay ejercicio seleccionado, no se hace nada
        console.warn('Se pidio salir de ejercicio pero no hay un ejericico activo');
      }

      // Si se especifico un ejercicio nuevo, se ingresa automaticamente
      if (opts?.nuevo) {
        await this.seleccionarEjercicio(opts.nuevo);
      }
    } catch {
      this.setStatus({ error: true });
      console.error('Error al salir de ejercicio');
      notificationService.error('Error de comunicación');
    }
  }

  /**
   * Actualiza los datos del usuario en la sesion.
   * Esto se utiliza cuando se cambian los datos del usuario logueado, para que se vean reflejados
   * @param user nuevos datos del usuario
   */
  @Action
  async updateUserInfo(user: User) {
    if (!user || !this.sesion || !this.user) return;

    // Se actualiza la sesion con los datos nuevos y la misma org, ejercicio y roles
    this.updateSesion({
      user,
      organizacion: this.organizacion,
      ejercicio: this.ejercicio,
      roles: this.sesion.roles
    });
  }

  @Mutation
  private setFirstUse(value: boolean) {
    this.firstUse = value;
  }
  @Mutation
  private setStatus(status: LoadingStatus) {
    this.status = status;
  }

  @Mutation
  private setStarted(value: boolean) {
    this.appStarted = value;
  }

  @Mutation
  private removeSesion() {
    this.sesion = null;
  }

  /** Actualiza los datos de la sesion actual */
  @Mutation
  private updateSesion({ user, organizacion, ejercicio, roles }: Session) {
    if (!user) {
      this.sesion = null;
    } else {
      this.sesion = {
        user,
        organizacionId: organizacion?.id ?? null,
        ejercicioId: ejercicio?.id ?? null,
        roles
      };

      // Si se ingreso a una organizacion, se la agrega al store de organizaciones
      if (organizacion) {
        organizacionStore.findByIdSuccess(organizacion);
      }

      // Si se ingreso a un ejercicio, se lo agrega al store de organizaciones
      if (ejercicio != null) {
        organizacionStore.agregarAStore(ejercicio);
      }

      // TODO Mejorar esto (quizas no deberia llegar la org en el ejercicico)
      if (this.ejercicio?.organizacion) {
        organizacionStore.findByIdSuccess(this.ejercicio.organizacion);
        this.ejercicio.organizacion = {
          id: this.ejercicio.organizacion.id
        } as Organizacion;
      }
    }
  }
}
