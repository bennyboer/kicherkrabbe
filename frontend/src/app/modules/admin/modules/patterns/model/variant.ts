import { Money, someOrNone, validateProps } from '../../../../../util';
import { PricedSizeRange } from './priced-size-range';

export class PatternVariant {
  readonly name: string;
  readonly sizes: PricedSizeRange[];

  private constructor(props: { name: string; sizes: PricedSizeRange[] }) {
    validateProps(props);

    this.name = props.name;
    this.sizes = props.sizes;
  }

  static of(props: {
    name: string;
    sizes?: PricedSizeRange[];
  }): PatternVariant {
    return new PatternVariant({
      name: props.name,
      sizes: someOrNone(props.sizes).orElse([]),
    });
  }

  getStartingPrice(): Money {
    const prices = this.sizes.map((size) => size.price);

    if (prices.length === 0) {
      return Money.zero();
    }

    return prices.reduce(
      (acc, price) => (acc.isLessThan(price) ? acc : price),
      prices[0],
    );
  }
}
