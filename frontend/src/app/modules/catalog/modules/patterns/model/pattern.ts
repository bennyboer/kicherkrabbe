import { PatternVariant } from './variant';
import { PatternExtra } from './extra';
import {
  Image,
  Money,
  none,
  Option,
  some,
  someOrNone,
} from '../../../../../util';
import { Category } from './category';

export class Pattern {
  readonly id: string;
  readonly name: string;
  readonly categories: Set<Category>;
  readonly previewImage: Image;
  readonly images: Image[];
  readonly variants: PatternVariant[];
  readonly extras: PatternExtra[];
  readonly originalPatternName: Option<string>;
  readonly attribution: Option<string>;

  private constructor(props: {
    id: string;
    name: string;
    categories: Set<Category>;
    previewImage: Image;
    images: Image[];
    variants: PatternVariant[];
    extras: PatternExtra[];
    originalPatternName: Option<string>;
    attribution: Option<string>;
  }) {
    this.id = someOrNone(props.id).orElseThrow('Pattern ID is required');
    this.name = someOrNone(props.name).orElseThrow('Pattern name is required');
    this.categories = someOrNone(props.categories).orElse(new Set());
    this.previewImage = someOrNone(props.previewImage).orElseThrow(
      'Pattern preview image is required',
    );
    this.images = someOrNone(props.images).orElseThrow(
      'Pattern images are required',
    );
    this.variants = someOrNone(props.variants).orElseThrow(
      'Pattern variants are required',
    );
    this.extras = someOrNone(props.extras).orElseThrow(
      'Pattern extras are required',
    );
    this.originalPatternName = someOrNone(props.originalPatternName).orElse(
      none(),
    );
    this.attribution = someOrNone(props.attribution).orElse(none());
  }

  static of(props: {
    id: string;
    name: string;
    categories: Set<Category>;
    previewImage: Image;
    images: Image[];
    variants: PatternVariant[];
    extras?: PatternExtra[];
    originalPatternName?: string;
    attribution?: string;
  }): Pattern {
    return new Pattern({
      id: props.id,
      name: props.name,
      categories: props.categories,
      previewImage: props.previewImage,
      images: props.images,
      variants: props.variants,
      extras: someOrNone(props.extras).orElse([]),
      originalPatternName: someOrNone(props.originalPatternName),
      attribution: someOrNone(props.attribution),
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
