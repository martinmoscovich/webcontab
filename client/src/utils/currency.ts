/** Formatea un numero con separador de miles y decimales */
export function formatNumber(value: string | number, decimals = 2, thoudsansSep = '.', decimalSep = ',') {
  if (typeof value === 'string') value = parseFloat(value);
  const re = '\\d(?=(\\d{3})+' + (decimals > 0 ? '\\D' : '$') + ')',
    num = value.toFixed(Math.max(0, ~~decimals));

  return (decimalSep ? num.replace('.', decimalSep) : num).replace(new RegExp(re, 'g'), '$&' + (thoudsansSep || ','));
}

/**
 * Formatea un valor a currency. Implica formatear el numero con 2 decimales y agregar el simbolo de la moneda
 * @param value  valor a formatear
 * @param currency simbolo de la moneda
 */
export function formatCurrency(value: string | number, currency = '$') {
  return currency + formatNumber(value, 2, '.', ',');
}

/**
 * Redondea un valor con 2 decimales
 * @param value
 */
export function twoDecimals(value: number) {
  return Math.round(value * 100) / 100;
}
