declare module 'v-click-outside' {
  import { PluginObject } from 'vue/types/umd';
  const a: PluginObject<unknown>;
  export default a;
}

declare module '@femessage/log-viewer' {
  import { Component } from 'vue/types/umd';
  const LogViewer: Component;
  export default LogViewer;
}

/**
 * Tipos para el Validador
 */
declare module 'simple-vue-validator' {
  interface Validator {
    value(value: string): Validator;

    required(): Validator;
    isEmpty(value: unknown): Validator;

    email(): Validator;
    regex(regex: string, message: string): Validator;
    digit(): Validator;

    length(n: number): Validator;
    minLength(n: number): Validator;
    maxLength(n: number): Validator;
    match(other: string | undefined): Validator;

    integer(): Validator;
    float(): Validator;
    greaterThan(n: number): Validator;
    greaterThanOrEqualTo(n: number): Validator;
    lessThan(n: number): Validator;
    lessThanOrEqualTo(n: number): Validator;

    size(n: number): Validator;

    custom(fn: CustomValidation): Validator;
  }

  export type CustomValidation = (...value: string[]) => string | undefined;

  interface SimpleVueValidation {
    Validator: Validator;
    extendTemplates(templates: Partial<Record<keyof Validator, string>>): void;
  }
  export type ValidatorFn = (value: string, other?: string) => Validator;

  export interface Validation {
    cache?: true | 'all' | 'last';
    debounce?: number;
    validator: ValidatorFn;
  }

  export interface ValidationBag {
    hasError(field?: string): boolean;
    firstError(field?: string): string;
    allErrors(field?: string): string[];
    countErrors(field?: string): number;
    isValidating(field?: string): boolean;
    isPassed(field?: string): boolean;
    isTouched(field?: string): boolean;
    reset(): void;
    touchedRecords(): unknown[];
  }

  import { PluginObject } from 'vue/types/umd';
  const cls: SimpleVueValidation & PluginObject<unknown>;
  export default cls;
}
