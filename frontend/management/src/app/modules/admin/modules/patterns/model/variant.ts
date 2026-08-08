import { Eq, Money } from '../../../../../util';
import { PricedSizeRange } from './priced-size-range';
import { none, Option, some, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class PatternVariant implements Eq<PatternVariant> {
  readonly id: string;
  readonly name: string;
  readonly sizes: PricedSizeRange[];

  private constructor(props: { name: string; sizes: PricedSizeRange[] }) {
    validateProps(props);

    this.id = crypto.randomUUID();
    this.name = props.name;
    this.sizes = props.sizes;
  }

  static of(props: { name: string; sizes?: PricedSizeRange[] }): PatternVariant {
    return new PatternVariant({
      name: props.name,
      sizes: someOrNone(props.sizes).orElse([]),
    });
  }

  withName(name: string): PatternVariant {
    return PatternVariant.of({
      ...this,
      name,
    });
  }

  withSizes(sizes: PricedSizeRange[]): PatternVariant {
    return PatternVariant.of({
      ...this,
      sizes,
    });
  }

  getFormattedSizeRange(): string {
    return this.getSmallestSize()
      .flatMap((smallest) =>
        this.getLargestSize().map((largest) => (smallest === largest ? `${smallest}` : `${smallest} - ${largest}`)),
      )
      .orElse('-');
  }

  getFormattedPriceRange(): string {
    const lowestPrice = this.getLowestPrice();
    const highestPrice = this.getHighestPrice();

    if (lowestPrice.isEqualTo(highestPrice)) {
      return lowestPrice.formatted();
    }

    return `${lowestPrice.formatted()} - ${highestPrice.formatted()}`;
  }

  equals(other: PatternVariant): boolean {
    return (
      this.name === other.name &&
      this.sizes.length === other.sizes.length &&
      this.sizes.every((size, index) => size.equals(other.sizes[index]))
    );
  }

  private getLowestPrice(): Money {
    const prices = this.sizes.map((size) => size.price);

    if (prices.length === 0) {
      return Money.zero();
    }

    return prices.reduce((acc, price) => (acc.isLessThan(price) ? acc : price), prices[0]);
  }

  private getHighestPrice(): Money {
    const prices = this.sizes.map((size) => size.price);

    if (prices.length === 0) {
      return Money.zero();
    }

    return prices.reduce((acc, price) => (acc.isGreaterThan(price) ? acc : price), prices[0]);
  }

  private getSmallestSize(): Option<number> {
    const sizes = this.sizes.flatMap((size) => size.from.map((from) => [from]).orElse([]));

    if (sizes.length === 0) {
      return none();
    }

    return some(sizes.reduce((acc, size) => (size < acc ? size : acc), sizes[0]));
  }

  private getLargestSize(): Option<number> {
    const sizes = this.sizes.flatMap((size) =>
      size.from.map((from) => [size.to.orElse(from)]).orElse([] as number[]),
    );

    if (sizes.length === 0) {
      return none();
    }

    return some(sizes.reduce((acc, size) => (size > acc ? size : acc), sizes[0]));
  }
}
