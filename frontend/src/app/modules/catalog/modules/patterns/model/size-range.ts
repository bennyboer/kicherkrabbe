import { Money, Option } from '../../../../../util';

export class SizeRange {
  readonly from: number;
  readonly to: number;
  readonly price: Money;

  private constructor(props: { from: number; to: number; price: Money }) {
    this.from = Option.someOrNone(props.from).orElseThrow(
      'Size range from is required',
    );
    this.to = Option.someOrNone(props.to).orElseThrow(
      'Size range to is required',
    );
    this.price = Option.someOrNone(props.price).orElseThrow(
      'Size range price is required',
    );
  }

  static of(props: { from: number; to: number; price: Money }): SizeRange {
    return new SizeRange(props);
  }

  formatted(): string {
    if (this.from === this.to) {
      return this.from.toString();
    }

    return `${this.from} - ${this.to}`;
  }
}
