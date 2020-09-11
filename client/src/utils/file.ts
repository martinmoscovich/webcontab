/**
 * Formatea un tamanio de archivo de su valor numerico en bytes, a un string que incluye la unidad mas apropiada.
 *
 * @param bytes numero a formatear.
 * @param decimals numero de decimales a utilizar.
 */
export function formatFileSize(bytes: number, decimals = 2) {
  if (bytes < 1024) return bytes.toFixed(decimals) + ' B';

  const kb = bytes / 1024;
  if (kb < 1024) return kb.toFixed(decimals) + ' KB';

  const mb = kb / 1024;
  if (mb < 1024) return mb.toFixed(decimals) + ' MB';

  const gb = mb / 1024;
  if (gb < 1024) return gb.toFixed(decimals) + ' GB';

  const tb = gb / 1024;
  if (tb < 1024) return tb.toFixed(decimals) + ' TB';

  return '';
}

/**
 * Muestra el dialog de descarga para un blob
 * @param blob contenido del archivo a descargar
 * @param filename nombre del archivo
 */
export function download(blob: Blob, filename: string) {
  const elem = document.createElement('a');
  elem.href = URL.createObjectURL(blob);
  elem.download = filename;
  elem.click();
}
