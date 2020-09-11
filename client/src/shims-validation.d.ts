/**
 * Augment the typings of Vue.js
 */
import Vue from 'vue';
import { Validation, ValidatorFn, ValidationBag } from 'simple-vue-validator';

declare module 'vue/types/vue' {
  interface Vue {
    validation: ValidationBag;

    $validate(field: string): Promise<boolean>;
    $validate(fields: string[]): Promise<boolean>;
    $validate(fields?: string[] | string): Promise<boolean>;
  }
}

declare module 'vue/types/options' {
  interface ComponentOptions<V extends Vue> {
    validators?: Record<string, Validation | ValidatorFn>;
  }
}
