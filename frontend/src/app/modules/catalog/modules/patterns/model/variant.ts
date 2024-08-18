import {
  Money,
  none,
  Option,
  some,
  someOrNone,
  validateProps,
} from '../../../../../util';
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

  getSmallestSize(): number {
    const sizes = this.sizes.map((size) => size.from);

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size < acc ? size : acc), sizes[0]);
  }

  getLargestSize(): Option<number> {
    const allSizesAreDefined = this.sizes.every((size) => size.to.isSome());
    if (!allSizesAreDefined) {
      return none();
    }

    const sizes = this.sizes.map((size) => size.to.orElse(size.from));

    if (sizes.length === 0) {
      return some(0);
    }

    return some(
      sizes.reduce((acc, size) => (size > acc ? size : acc), sizes[0]),
    );
  }

  isAvailableInSize(size: number): boolean {
    return this.sizes.some((variantSize) => variantSize.includes(size));
  }
}
