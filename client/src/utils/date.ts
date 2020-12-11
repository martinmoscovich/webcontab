import { isNullOrUndefined } from '@/utils/general';

const MONTHS = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre'
];

/**
 * Parsea una fecha en formato "dd-mm-yyyy" o "dd/mm/yyyy".
 * Tambien soporta 2 digitos para el anio
 */
export function parseDate(str: string): Date | null {
  let parts: string[];
  if (str.includes('-')) {
    parts = str.split('-');
  } else if (str.includes('/')) {
    parts = str.split('/');
  } else {
    return null;
  }

  if (parts.length !== 3) return null;

  let year = parseInt(parts[2], 10);
  if (year < 100) year += 2000;

  return new Date(year, parseInt(parts[1], 10) - 1, parseInt(parts[0], 10));
}

/**
 * Parsea una hora en formato "hh:mm:ss"
 * @param str
 */
export function parseTime(str: undefined): undefined;
export function parseTime(str: null): null;
export function parseTime(str: string): Date;
export function parseTime(str: string | null | undefined): Date | null | undefined {
  if (isNullOrUndefined(str)) return str;
  let parts: string[];
  if (str.includes(':')) {
    parts = str.split(':');
  } else {
    return null;
  }

  if (parts.length !== 3) return null;

  return new Date(0, 0, 0, parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
}

/**
 * Parsea la fecha que viene del server, en formato "yyyy-mm-dd".
 * @param str
 */
export function parseServerDate(str: undefined): undefined;
export function parseServerDate(str: null): null;
export function parseServerDate(str: string): Date;
export function parseServerDate(str: string | null | undefined): Date | null | undefined {
  if (isNullOrUndefined(str)) return str;
  let parts: string[];
  if (str.includes('-')) {
    parts = str.split('-');
  } else {
    return null;
  }

  if (parts.length !== 3) return null;

  return new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
}

/**
 * Formatea una fecha en formato "dd/mm/yyyy"
 * @param date
 */
export function formatDate(date?: Date) {
  if (!date) return '';
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date
    .getDate()
    .toString()
    .padStart(2, '0');

  return `${day}/${month}/${date.getFullYear()}`;
}

/**
 * Formatea una hora en formato "hh:mm:ss"
 * @param date
 */
export function formatTime(date?: Date) {
  if (!date) return '';
  const hour = date
    .getHours()
    .toString()
    .padStart(2, '0');
  const min = date
    .getMinutes()
    .toString()
    .padStart(2, '0');
  const sec = date
    .getSeconds()
    .toString()
    .padStart(2, '0');

  return `${hour}:${min}:${sec}`;
}

/**
 * Formatea la fecha el formato "dd mmm yy"
 * @param date
 * @param opts
 */
export function prettyFormatDate(date?: Date, opts: { shortMonth?: boolean } = {}) {
  if (!date) return '';

  let month = MONTHS[date.getMonth()];
  if (opts.shortMonth) month = month.substring(0, 3);

  const year = date
    .getFullYear()
    .toString()
    .substring(2);

  return `${date.getDate()} ${month} ${year}`;
}

/**
 * Formatea la fecha para enviarla al server en formato "yyyy-mm-dd"
 * @param date
 */
export function formatDateForServer(date: Date): string;
export function formatDateForServer(date: undefined): undefined;
export function formatDateForServer(date?: Date) {
  if (!date) return date;
  const month = (date.getMonth() + 1).toString().padStart(2, '0');

  const day = date
    .getDate()
    .toString()
    .padStart(2, '0');

  return `${date.getFullYear()}-${month}-${day}`;
}

/**
 * Determina si la fecha 1 es igual a la 2
 * @param date1
 * @param date2
 */
export function isEqualDate(date1?: Date | null, date2?: Date | null): boolean {
  return date1?.getTime() == date2?.getTime();
}

/**
 * Determina si la fecha 1 es anterior a la 2
 * @param date1
 * @param date2
 */
export function isBefore(date1: Date, date2: Date): boolean {
  return date1.getTime() < date2.getTime();
}

/**
 * Determina si la fecha 1 es posterior a la 2
 * @param date1
 * @param date2
 */
export function isAfter(date1: Date, date2: Date): boolean {
  return date1.getTime() > date2.getTime();
}

/**
 * Determina si la fecha 1 es igual o anterior a la 2
 * @param date1
 * @param date2
 */
export function isEqualOrBefore(date1: Date, date2: Date): boolean {
  return !isAfter(date1, date2);
}

/**
 * Determina si la fecha 1 es igual o posterior a la 2
 * @param date1
 * @param date2
 */
export function isEqualOrAfter(date1: Date, date2: Date): boolean {
  return !isBefore(date1, date2);
}

/**
 * Agrega los segundos indicados a la fecha especificada
 */
export function addSeconds(date: Date, seconds: number): Date {
  return new Date(date.getTime() + seconds * 1000);
}

/**
 * Agrega los minutos indicados a la fecha especificada
 */
export function addMinutes(date: Date, minutes: number): Date {
  return addSeconds(date, minutes * 60);
}

/**
 * Agrega las horas indicadas a la fecha especificada
 */
export function addHours(date: Date, hours: number): Date {
  return addMinutes(date, hours * 60);
}

/**
 * Agrega los dias indicados a la fecha especificada
 */
export function addDays(date: Date, days: number): Date {
  return addHours(date, days * 24);
}

/**
 * Agrega los meses indicados a la fecha especificada.
 * Se usa el constructor ya que los meses pueden tener distinta cantidad de dias.
 */
export function addMonths(date: Date, months: number): Date {
  return new Date(
    date.getFullYear(),
    date.getMonth() + months,
    date.getDate(),
    date.getHours(),
    date.getMinutes(),
    date.getSeconds(),
    date.getMilliseconds()
  );
}

/**
 * Agrega los anios indicados a la fecha especificada.
 * Se usa el constructor ya que los anios bisiestos tienen distinta cantidad de dias
 */
export function addYears(date: Date, years: number): Date {
  return new Date(
    date.getFullYear() + years,
    date.getMonth(),
    date.getDate(),
    date.getHours(),
    date.getMinutes(),
    date.getSeconds(),
    date.getMilliseconds()
  );
}
