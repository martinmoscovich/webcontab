import { MONTHS } from '@/utils/date';
import { Component, Vue } from 'vue-property-decorator';
import { CreateElement } from 'vue/types/umd';

/**
 * DatePicker en español
 */
@Component
export default class DatePicker extends Vue {
  render(h: CreateElement) {
    const props = {
      placeholder: 'Seleccione...',
      dayNames: ['D', 'L', 'Ma', 'Mi', 'J', 'V', 'S'],
      monthNames: MONTHS,
      ...this.$props
    };

    return h(
      'b-datepicker',
      {
        props,
        on: this.$listeners,
        attrs: this.$attrs
      },
      this.$slots.default
    );
  }
}
