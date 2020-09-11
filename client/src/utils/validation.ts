import SimpleVueValidation from 'simple-vue-validator';
import { isDefined } from './general';
const Validator = SimpleVueValidation.Validator;

interface DefaultOpts {
  required?: boolean;
}
interface NumericOpts extends DefaultOpts {
  min?: number;
  max?: number;
  greaterThan?: number;
  lowerThan?: number;
}
interface TextOpts extends DefaultOpts {
  min?: number;
  max?: number;
}

function numericValidation(value: string, opts: NumericOpts) {
  let result = Validator.value(value ?? null);
  if (opts.required) result = result.required();
  if (isDefined(opts.min)) result = result.greaterThanOrEqualTo(opts.min);
  if (isDefined(opts.max)) result = result.lessThanOrEqualTo(opts.max);
  if (isDefined(opts.greaterThan)) result = result.greaterThan(opts.greaterThan);
  if (isDefined(opts.lowerThan)) result = result.lessThan(opts.lowerThan);

  return result;
}

function textValidation(value: string, opts: TextOpts) {
  let result = Validator.value(value ?? null);
  if (opts.required) result = result.required();
  if (isDefined(opts.min)) result = result.minLength(opts.min);
  if (isDefined(opts.max)) result = result.maxLength(opts.max);

  return result;
}

export function field(value: string) {
  return Validator.value(value ?? null);
}

export function required(value: string) {
  return Validator.value(value ?? null).required();
}

export function text(opts: TextOpts) {
  return (value: string) => textValidation(value, opts);
}

export function number(opts: NumericOpts) {
  return (value: string) => numericValidation(value, opts).float();
}

export function integer(opts: NumericOpts) {
  return (value: string) => numericValidation(value, opts).integer();
}
export function email(value: string) {
  return Validator.value(value ?? null).email();
}

export const VALIDATION_MESSAGES = {
  error: 'Error',
  required: 'Requerido',
  float: 'Debe ser un número',
  integer: 'Debe ser un entero',
  number: 'Debe ser un número',
  lessThan: 'Debe ser menor a {0}',
  lessThanOrEqualTo: 'Debe ser menor o igual a {0}',
  greaterThan: 'Debe ser mayor a {0}',
  greaterThanOrEqualTo: 'Debe ser mayor o igual a {0}',
  // between: 'Must be between {0} y {1}',
  size: 'Debe tener {0} items',
  // length: 'Debe tener {0} caracteres de longitud',
  // minLength: 'Must have at least {0} characters.',
  // maxLength: 'Must have up to {0} characters.',
  // lengthBetween: 'Length must between {0} and {1}.',
  // in: 'Must be {0}.',
  // notIn: 'Must not be {0}.',
  // match: 'Not matched.',
  // regex: 'Invalid format.',
  digit: 'Debe tener solo dígitos',
  // email: 'Invalid email.',
  // url: 'Invalid url.',
  optionCombiner: function(options: unknown[]) {
    if (options.length > 2) {
      options = [options.slice(0, options.length - 1).join(', '), options[options.length - 1]];
    }
    return options.join(' o ');
  }
};
