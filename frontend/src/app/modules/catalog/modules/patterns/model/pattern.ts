import { PatternVariant } from './variant';
import { PatternExtra } from './extra';
import { Image, Money, Option } from '../../../../../util';
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
    this.id = Option.someOrNone(props.id).orElseThrow('Pattern ID is required');
    this.name = Option.someOrNone(props.name).orElseThrow(
      'Pattern name is required',
    );
    this.categories = Option.someOrNone(props.categories).orElse(new Set());
    this.previewImage = Option.someOrNone(props.previewImage).orElseThrow(
      'Pattern preview image is required',
    );
    this.images = Option.someOrNone(props.images).orElseThrow(
      'Pattern images are required',
    );
    this.variants = Option.someOrNone(props.variants).orElseThrow(
      'Pattern variants are required',
    );
    this.extras = Option.someOrNone(props.extras).orElseThrow(
      'Pattern extras are required',
    );
    this.originalPatternName = Option.someOrNone(
      props.originalPatternName,
    ).orElse(Option.none());
    this.attribution = Option.someOrNone(props.attribution).orElse(
      Option.none(),
    );
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
      extras: Option.someOrNone(props.extras).orElse([]),
      originalPatternName: Option.someOrNone(props.originalPatternName),
      attribution: Option.someOrNone(props.attribution),
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
      return Option.none();
    }

    return Option.some(
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
      return Option.none();
    }

    const sameUnitForAllSizes = units.every((unit) => unit === units[0]);
    if (sameUnitForAllSizes) {
      return Option.some(units[0]);
    }

    return Option.none();
  }
}
