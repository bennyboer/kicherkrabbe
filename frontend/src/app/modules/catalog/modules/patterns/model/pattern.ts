import { PatternVariant } from './variant';
import { PatternExtra } from './extra';
import {
  Money,
  none,
  Option,
  some,
  someOrNone,
  validateProps,
} from '../../../../../util';
import { PatternAttribution } from './attribution';

export type CategoryId = string;
export type ImageId = string;

export class Pattern {
  readonly id: string;
  readonly name: string;
  readonly attribution: PatternAttribution;
  readonly categories: Set<CategoryId>;
  readonly images: ImageId[];
  readonly variants: PatternVariant[];
  readonly extras: PatternExtra[];

  private constructor(props: {
    id: string;
    name: string;
    attribution: PatternAttribution;
    categories: Set<CategoryId>;
    images: ImageId[];
    variants: PatternVariant[];
    extras: PatternExtra[];
  }) {
    validateProps(props);

    this.id = props.id;
    this.name = props.name;
    this.attribution = props.attribution;
    this.categories = props.categories;
    this.images = props.images;
    this.variants = props.variants;
    this.extras = props.extras;
  }

  static of(props: {
    id: string;
    name: string;
    attribution?: PatternAttribution;
    categories?: Set<CategoryId>;
    images?: ImageId[];
    variants?: PatternVariant[];
    extras?: PatternExtra[];
  }): Pattern {
    return new Pattern({
      id: props.id,
      name: props.name,
      attribution: someOrNone(props.attribution).orElse(
        PatternAttribution.of({}),
      ),
      categories: someOrNone(props.categories).orElse(new Set<CategoryId>()),
      images: someOrNone(props.images).orElse([]),
      variants: someOrNone(props.variants).orElse([]),
      extras: someOrNone(props.extras).orElse([]),
    });
  }

  getStartingPrice(): Money {
    const prices = this.variants.map((variant) => variant.getStartingPrice());

    if (prices.length === 0) {
      return Money.zero();
    }

    return prices.reduce(
      (acc, price) => (acc.isLessThan(price) ? acc : price),
      prices[0],
    );
  }

  getFormattedSizeRange(): string {
    const smallestSize = this.getSmallestSize();
    const largestSize = this.getLargestSize();

    const sizeUnit = this.getSizeUnit()
      .map((unit) => ` ${unit}`)
      .orElse('');

    return largestSize
      .map((l) => {
        if (l === smallestSize) {
          return `${smallestSize}${sizeUnit}`;
        }

        return `${smallestSize} - ${l}${sizeUnit}`;
      })
      .orElse(`ab ${smallestSize}${sizeUnit}`);
  }

  isAvailableInSize(size: number): boolean {
    return this.variants.some((variant) => variant.isAvailableInSize(size));
  }

  private getSmallestSize(): number {
    const sizes = this.variants.map((variant) => variant.getSmallestSize());

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size < acc ? size : acc), sizes[0]);
  }

  private getLargestSize(): Option<number> {
    const sizes = this.variants.map((variant) => variant.getLargestSize());

    if (sizes.length === 0) {
      return none();
    }

    return some(
      sizes
        .flatMap((size) => size.map((s) => [s]).orElse([]))
        .reduce((acc, size) => (size > acc ? size : acc), sizes[0].orElse(0)),
    );
  }

  private getSizeUnit(): Option<string> {
    const units = this.variants.flatMap((variant) =>
      variant.sizes.map((size) => size.unit.orElse('')),
    );

    if (units.length === 0) {
      return none();
    }

    const sameUnitForAllSizes = units.every((unit) => unit === units[0]);
    if (sameUnitForAllSizes) {
      return some(units[0]);
    }

    return none();
  }
}
