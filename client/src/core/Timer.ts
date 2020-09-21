/**
 * Configuracion del timer
 */
interface TimerConfig<T> {
  /** En o cada cuanto tiempo (en ms) debe hacerse la llamada */
  interval: number;

  /**
   * Handler del timer.
   *
   * Si devuelve una Promise y es de tipo "delay", el intervalo
   * comienza a correr luego de que se cumpla la promise.
   */
  onTime: (name?: string, ctx?: T) => void | Promise<void>;

  /**
   * Tipo de timer:
   *  - once: se ejecuta una sola vez
   *  - rate: se ejecuta indefinidamente cada [intervalo] ms
   *  - delay: se ejecuta indefinidamente, esperando [intervalo] ms
   * entre el final de una invocacion y el comienzo de la siguiente.
   */
  type?: 'once' | 'rate' | 'delay';

  /**
   * Nombre del timer (opcional)
   * Sirve para loguear y se lo recibe en el handler
   */
  name?: string;

  /** Contexto opcional que se envia al handler */
  context?: T;
}

/**
 * Timer que permite esperar un intervalo para ejecutar una funcion o bien
 * ejecutarla cada cierto tiempo recurrentemente.
 */
export class Timer<T = void> {
  /** Handler del setTimeout que permite cancelarlo */
  private handle: number | null = null;

  constructor(public config: TimerConfig<T>) {
    if (!config.type) config.type = 'once';
  }

  /** Comienza el timer */
  start() {
    this.reset();
  }

  /** Detiene el timer */
  stop() {
    this.cancel(true);
  }

  /** Reinicia el timer */
  reset() {
    this.doReset(true);
  }

  private cancel(log: boolean) {
    if (this.handle) {
      if (log) console.warn('Se cancela %s', this.toString());
      clearTimeout(this.handle);
      this.handle = null;
    }
  }

  private doReset(log: boolean) {
    if (log) console.warn('Se %s %s', this.handle ? 'reinicia' : 'comienza', this.toString());

    // Si existe el timer, se cancela
    this.cancel(false);

    // Se genera un nuevo timer con el tiempo indicado
    this.handle = setTimeout(() => this.run(), this.config.interval);
  }

  toString() {
    const name = this.config.name ? "'" + this.config.name + "'" : '';
    return `Timer ${name} [${this.config.type}: ${this.config.interval}ms]`;
  }

  /**
   * Ejecuta el handler
   * Segun el tipo de timer, se encarga de reiniciar
   */
  private async run() {
    console.warn('Se disparo %s', this.toString());

    if (this.config.type === 'once') {
      // Si es de unica vez, se limpia el timer y ejecuta el handler
      this.cancel(false);
      this.config.onTime(this.config.name, this.config.context);
    } else if (this.config.type === 'rate') {
      // Si es de intervalo fijo, se reinicia el timer y luego se ejecuta el handler
      this.doReset(false);
      this.config.onTime(this.config.name, this.config.context);
    } else {
      // Si debe esperar la ejecucion, primero ejecuta, espera y luego lo reinicia
      try {
        await this.config.onTime(this.config.name, this.config.context);
        this.doReset(false);
      } catch (e) {
        console.error('Error en el Handler, no se continua %s', this.toString());
        throw e;
      }
    }
  }
}
