import { Component, Prop, Vue } from 'vue-property-decorator';

/**
 * Item que indica la cantidad de elementos importados y el total
 */
@Component
export default class ImportTaskSummaryItem extends Vue {
  @Prop()
  label: string;

  @Prop()
  value: number;

  @Prop()
  total: number;

  render() {
    // if (!this.value && !this.total) return null;
    return (
      <div class="level-item has-text-centered">
        <div>
          <p class="heading">{this.label}</p>
          <p class="title">
            {this.value}
            {this.total ? <span> / {this.total}</span> : null}
          </p>
        </div>
      </div>
    );
  }
}
