/**
 * Genera una promise que se cumple luego de los milisegundos especificados.
 *
 * Util durante el desarrollo para simular, por ejemplo, latencia de red.
 *
 * @param {number} ms
 * @param {boolean} [fail=false] determina si luego del retardo, la promise debe cumplirse o fallar.
 * @returns
 */
export function delay(ms: number, fail = false): Promise<void> {
  return new Promise((resolve, reject) => {
    setTimeout(() => (fail ? reject() : resolve()), ms);
  });
}

/**
 * Recibe una promise y devuelve otra que falla si la promise original no se resuelve antes de los ms especificados.
 * Si se resuelve o falla antes, devuelve el valor de la misma.
 *
 * @param promise
 * @param ms
 */
export function promiseTimeout<T>(promise: Promise<T>, ms: number) {
  const timeout = delay(ms).then(() => {
    throw new Error(`Time out: ${ms} ms`);
  });
  return Promise.race([promise, timeout]);
}
