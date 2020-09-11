import { Vue, Component, Prop } from 'vue-property-decorator';

/**
 * Label (span) que se puede editar usando "contenteditable" y se confirma presionando Enter
 */
@Component
export default class EditableLabel extends Vue {
  /** Valor del Label */
  @Prop()
  value: string;

  /** Handler cuando se presiona una tecla, usando para confirmar usando ENTER */
  private onKeyDown(e: KeyboardEvent) {
    if (e.keyCode === 13) {
      e.preventDefault();
      (this.$el as HTMLElement).blur();
    }
  }

  /** Handler cuando termina la edicion */
  private onBlur() {
    this.$emit('input', this.$el.textContent);
  }

  /** Render */
  private render() {
    return (
      <span contenteditable onKeydown={this.onKeyDown} onBlur={this.onBlur}>
        {this.value}
      </span>
    );
  }
}
