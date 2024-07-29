import { Money, Option, someOrNone, validateProps } from '../../../../../util';

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
    validateProps(props);

    this.from = props.from;
    this.to = props.to;
    this.unit = props.unit;
    this.price = props.price;
  }

  static of(props: {
    from?: number;
    to?: number;
    unit?: string;
    price?: Money;
  }): PricedSizeRange {
    return new PricedSizeRange({
      from: someOrNone(props.from).orElse(0),
      to: someOrNone(props.to),
      unit: someOrNone(props.unit),
      price: someOrNone(props.price).orElse(Money.zero()),
    });
  }
}
