import { PatternVariant } from './variant';
import { PatternExtra } from './extra';
import { Image, Money, Option } from '../../../../../util';

export class Pattern {
  readonly id: string;
  readonly name: string;
  readonly images: Image[];
  readonly variants: PatternVariant[];
  readonly extras: PatternExtra[];
  readonly attribution: Option<string>;

  private constructor(props: {
    id: string;
    name: string;
    images: Image[];
    variants: PatternVariant[];
    extras: PatternExtra[];
    attribution: Option<string>;
  }) {
    this.id = Option.someOrNone(props.id).orElseThrow('Pattern ID is required');
    this.name = Option.someOrNone(props.name).orElseThrow(
      'Pattern name is required',
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
    this.attribution = Option.someOrNone(props.attribution).orElse(
      Option.none(),
    );
  }

  static of(props: {
    id: string;
    name: string;
    images: Image[];
    variants: PatternVariant[];
    extras: PatternExtra[];
    attribution?: string;
  }): Pattern {
    return new Pattern({
      id: props.id,
      name: props.name,
      images: props.images,
      variants: props.variants,
      extras: props.extras,
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

  getSmallestSize(): number {
    const sizes = this.variants.map((variant) => variant.getSmallestSize());

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size < acc ? size : acc), sizes[0]);
  }

  getLargestSize(): number {
    const sizes = this.variants.map((variant) => variant.getLargestSize());

    if (sizes.length === 0) {
      return 0;
    }

    return sizes.reduce((acc, size) => (size > acc ? size : acc), sizes[0]);
  }
}
