import { IdModel } from '@/model/IdModel';
import { BaseSimpleStore } from '@/store/BaseSimpleStore';
import { Module } from 'vuex-class-modules';

/**
 * Store que se basa en BaseSimpleStore pero no agrega nueva funcionalidad.
 * Se parametriza en el constructor.
 */
@Module
export class SimpleStore<T extends IdModel> extends BaseSimpleStore<T> {}
