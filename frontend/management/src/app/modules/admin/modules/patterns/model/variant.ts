import { Eq, Money, validateProps } from '../../../../../util';
import { PricedSizeRange } from './priced-size-range';
import { someOrNone } from '../../../../shared/modules/option';

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
    const smallestSize = this.getSmallestSize();
    const largestSize = this.getLargestSize();

    if (smallestSize === largestSize) {
      return `${smallestSize}`;
    }

    return `${smallestSize} - ${largestSize}`;
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

  private getSmallestSize(): number {
    const sizes = this.sizes.map((size) => size.from);

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size < acc ? size : acc), sizes[0]);
  }

  private getLargestSize(): number {
    const sizes = this.sizes.map((size) => size.to.orElse(size.from));

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size > acc ? size : acc), sizes[0]);
  }
}
