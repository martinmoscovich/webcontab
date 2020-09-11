/**
 * Devuelve el texto indicado con la primer letra en mayuscula
 * @param text
 */
export function capitalize(text: string) {
  if (!text) return text;
  if (text.length === 1) return text.toUpperCase();
  return text.charAt(0).toUpperCase() + text.substring(1);
}
