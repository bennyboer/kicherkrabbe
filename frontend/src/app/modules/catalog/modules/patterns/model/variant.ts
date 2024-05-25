import { Money, none, Option, some, someOrNone } from '../../../../../util';
import { SizeRange } from './size-range';

export class PatternVariant {
  readonly id: string;
  readonly name: string;
  readonly description: Option<string>;
  readonly sizes: SizeRange[];

  private constructor(props: {
    id: string;
    name: string;
    description: Option<string>;
    sizes: SizeRange[];
  }) {
    this.id = someOrNone(props.id).orElseThrow('Variant ID is required');
    this.name = someOrNone(props.name).orElseThrow('Variant name is required');
    this.description = props.description;
    this.sizes = someOrNone(props.sizes).orElseThrow(
      'Variant sizes are required',
    );
  }

  static of(props: {
    id: string;
    name: string;
    description?: string;
    sizes: SizeRange[];
  }): PatternVariant {
    return new PatternVariant({
      id: props.id,
      name: props.name,
      description: someOrNone(props.description),
      sizes: props.sizes,
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
