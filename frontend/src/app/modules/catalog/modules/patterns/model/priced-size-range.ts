import { Money, Option, someOrNone } from '../../../../../util';

export class PricedSizeRange {
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
    this.from = someOrNone(props.from).orElseThrow(
      'Size range from is required',
    );
    this.to = props.to;
    this.unit = props.unit;
    this.price = someOrNone(props.price).orElseThrow(
      'Size range price is required',
    );
  }

  static of(props: {
    from: number;
    to?: number | null;
    unit?: string | null;
    price: Money;
  }): PricedSizeRange {
    return new PricedSizeRange({
      from: props.from,
      to: someOrNone(props.to),
      unit: someOrNone(props.unit),
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

  includes(size: number): boolean {
    return this.to
      .map((to) => size >= this.from && size <= to)
      .orElse(size === this.from);
  }
}
