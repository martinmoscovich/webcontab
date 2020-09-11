import { delay } from './delay';
import { Dictionary } from 'vue-router/types/router';

const FOCUSABLE_SELECTOR =
  'a[href], area[href], ' +
  'input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), ' +
  'iframe, object, embed, *[tabindex]:not([tabindex="-1"]), *[contenteditable]';

export function parseQueryString(raw: Dictionary<string | (string | null)[]>): Dictionary<string> {
  const qs: Dictionary<string> = {};
  Object.entries(raw).forEach(([k, v]) => {
    if (v !== undefined && v !== null) {
      if (Array.isArray(v)) {
        if (v[0]) qs[k] = v[0] as string;
      } else {
        qs[k] = v;
      }
    }
  });
  return qs;
}

/**
 * Tipo que representa un elemento que se puede enfocar.
 */
export type Focusable = { focus: () => void };

/**
 * Hace foco sobre un componente, esperando opcionalmente los ms que se especifican
 * @param element a enfocar
 * @param delayMs delay opcional.
 */
export function focus(element: Focusable, delayMs?: number) {
  if (delayMs !== undefined) {
    delay(delayMs).then(() => element.focus());
  } else {
    element.focus();
  }
}

export function findNextFocusable(from: HTMLElement, offset: number): HTMLElement {
  const nodes = Array.from(document.querySelectorAll(FOCUSABLE_SELECTOR)).filter(
    n => !n.getAttribute('tabindex') || parseInt(n.getAttribute('tabindex') ?? '') >= 0
  );
  // Logica radio button
  // if ($from[0].tagName === "INPUT" && $from[0].type === "radio" && $from[0].name !== "") {
  //     var name = internal.escapeSelectorName($from[0].name);

  //     $focusable = $focusable
  //         .not("input[type=radio][name=" + name + "]")
  //         .add($from);
  // }

  const currentIndex = nodes.indexOf(from);

  let nextIndex = (currentIndex + offset) % nodes.length;
  if (nextIndex <= -1) {
    nextIndex = nodes.length + nextIndex;
  }

  return nodes[nextIndex] as HTMLElement;
}

export function focusOnNext(
  opts: {
    from?: HTMLElement;
    offset?: number;
    delay?: number;
    cb?: (elem: HTMLElement) => void;
  } = {}
) {
  const nextElem = findNextFocusable(opts.from || (document.activeElement as HTMLElement), opts.offset || 1);
  if (opts.cb) return opts.cb(nextElem);
  focus(nextElem, opts.delay || 0);
}
