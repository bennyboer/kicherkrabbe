import { PatternVariant } from './variant';
import { PatternExtra } from './extra';
import { PatternAttribution } from './attribution';
import { validateProps } from '../../../../../util';

export type PatternId = string;
export type PatternCategoryId = string;
export type ImageId = string;

export class Pattern {
  readonly id: PatternId;
  readonly version: number;
  readonly published: boolean;
  readonly name: string;
  readonly attribution: PatternAttribution;
  readonly categories: Set<PatternCategoryId>;
  readonly images: ImageId[];
  readonly variants: PatternVariant[];
  readonly extras: PatternExtra[];
  readonly createdAt: Date;

  private constructor(props: {
    id: PatternId;
    version: number;
    published: boolean;
    name: string;
    attribution: PatternAttribution;
    categories: Set<PatternCategoryId>;
    images: ImageId[];
    variants: PatternVariant[];
    extras: PatternExtra[];
    createdAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.published = props.published;
    this.name = props.name;
    this.attribution = props.attribution;
    this.categories = props.categories;
    this.images = props.images;
    this.variants = props.variants;
    this.extras = props.extras;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: PatternId;
    version: number;
    published: boolean;
    name: string;
    attribution: PatternAttribution;
    categories: Set<PatternCategoryId>;
    images: ImageId[];
    variants: PatternVariant[];
    extras: PatternExtra[];
    createdAt: Date;
  }): Pattern {
    return new Pattern({
      id: props.id,
      version: props.version,
      published: props.published,
      name: props.name,
      attribution: props.attribution,
      categories: props.categories,
      images: props.images,
      variants: props.variants,
      extras: props.extras,
      createdAt: props.createdAt,
    });
  }
}