import { Money, Option } from '../../../../../util';
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
    this.id = Option.someOrNone(props.id).orElseThrow('Variant ID is required');
    this.name = Option.someOrNone(props.name).orElseThrow(
      'Variant name is required',
    );
    this.description = props.description;
    this.sizes = Option.someOrNone(props.sizes).orElseThrow(
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
      description: Option.someOrNone(props.description),
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
      return Option.none();
    }

    const sizes = this.sizes.map((size) => size.to.orElse(size.from));

    if (sizes.length === 0) {
      return Option.some(0);
    }

    return Option.some(
      sizes.reduce((acc, size) => (size > acc ? size : acc), sizes[0]),
    );
  }

  isAvailableInSize(size: number): boolean {
    return this.sizes.some((variantSize) => variantSize.includes(size));
  }
}
