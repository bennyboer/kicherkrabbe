import { Money, Option } from '../../../../../util';

export class SizeRange {
  readonly from: number;
  readonly to: Option<number>;
  readonly unit: Option<string>;
  readonly price: Money;

  private constructor(props: {
    from: number;
    to: Option<number>;
    unit: Option<string>;
    price: Money;
  }) {
    this.from = Option.someOrNone(props.from).orElseThrow(
      'Size range from is required',
    );
    this.to = props.to;
    this.unit = props.unit;
    this.price = Option.someOrNone(props.price).orElseThrow(
      'Size range price is required',
    );
  }

  static of(props: {
    from: number;
    to?: number;
    unit?: string;
    price: Money;
  }): SizeRange {
    return new SizeRange({
      from: props.from,
      to: Option.someOrNone(props.to),
      unit: Option.someOrNone(props.unit),
      price: props.price,
    });
  }

  formattedRange(): string {
    return this.to
      .map((to) => {
        if (this.from === to) {
          return this.from.toString();
        }

        return `${this.from} - ${to}`;
      })
      .orElse(`ab ${this.from}`);
  }

  formatted(): string {
    const range = this.formattedRange();

    return this.unit.map((unit) => `${range} ${unit}`).orElse(range);
  }
}
