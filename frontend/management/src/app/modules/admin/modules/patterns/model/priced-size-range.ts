import { Eq, Money } from '../../../../../util';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class PricedSizeRange implements Eq<PricedSizeRange> {
  readonly id: string;
  readonly from: Option<number>;
  readonly to: Option<number>;
  readonly unit: Option<string>;
  readonly price: Money;

  private constructor(props: { from: Option<number>; to: Option<number>; unit: Option<string>; price: Money }) {
    validateProps(props);

    this.id = crypto.randomUUID();
    this.from = props.from;
    this.to = props.to;
    this.unit = props.unit;
    this.price = props.price;
  }

  static of(props: { from?: number | null; to?: number | null; unit?: string | null; price?: Money }): PricedSizeRange {
    return new PricedSizeRange({
      from: someOrNone(props.from),
      to: someOrNone(props.to),
      unit: someOrNone(props.unit),
      price: someOrNone(props.price).orElse(Money.zero()),
    });
  }

  withFrom(from?: number | null): PricedSizeRange {
    return new PricedSizeRange({
      ...this,
      from: someOrNone(from),
    });
  }

  withTo(to?: number | null): PricedSizeRange {
    return new PricedSizeRange({
      ...this,
      to: someOrNone(to),
    });
  }

  withUnit(unit?: string | null): PricedSizeRange {
    return new PricedSizeRange({
      ...this,
      unit: someOrNone(unit),
    });
  }

  withPrice(price?: Money): PricedSizeRange {
    return someOrNone(price)
      .map(
        (newPrice) =>
          new PricedSizeRange({
            ...this,
            price: newPrice,
          }),
      )
      .orElse(this);
  }

  equals(other: PricedSizeRange): boolean {
    return (
      this.from.equals(other.from) &&
      this.to.equals(other.to) &&
      this.unit.equals(other.unit) &&
      this.price.equals(other.price)
    );
  }
}
