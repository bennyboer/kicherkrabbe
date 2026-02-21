import { Money } from './money';
import { PriceHistoryEntry } from './price-history-entry';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class Pricing {
  readonly price: Money;
  readonly discountedPrice: Option<Money>;
  readonly priceHistory: PriceHistoryEntry[];

  private constructor(props: { price: Money; discountedPrice: Option<Money>; priceHistory: PriceHistoryEntry[] }) {
    validateProps(props);

    this.price = props.price;
    this.discountedPrice = props.discountedPrice;
    this.priceHistory = props.priceHistory;
  }

  static of(props: {
    price: Money;
    discountedPrice?: Money | null;
    priceHistory?: PriceHistoryEntry[];
  }): Pricing {
    return new Pricing({
      price: props.price,
      discountedPrice: someOrNone(props.discountedPrice),
      priceHistory: someOrNone(props.priceHistory).orElse([]),
    });
  }

  updatePrice(version: number, price: Money): Pricing {
    return new Pricing({
      ...this,
      price,
    });
  }

  addDiscount(version: number, discountedPrice: Money): Pricing {
    return new Pricing({
      ...this,
      discountedPrice: someOrNone(discountedPrice),
    });
  }

  removeDiscount(version: number): Pricing {
    return new Pricing({
      ...this,
      discountedPrice: someOrNone<Money>(null),
    });
  }
}
