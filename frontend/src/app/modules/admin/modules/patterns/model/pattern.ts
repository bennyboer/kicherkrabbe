import {PatternVariant} from "./variant";
import {PatternExtra} from "./extra";
import {PatternAttribution} from "./attribution";
import {validateProps} from "../../../../../util";
import {Option, someOrNone} from "../../../../shared/modules/option";

export type PatternId = string;
export type PatternCategoryId = string;
export type ImageId = string;

export class Pattern {
  readonly id: PatternId;
  readonly version: number;
  readonly published: boolean;
  readonly name: string;
  readonly number: string;
  readonly description: Option<string>;
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
    number: string;
    description: Option<string>;
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
    this.number = props.number;
    this.description = props.description;
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
    number: string;
    description?: string | null;
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
      number: props.number,
      description: someOrNone(props.description)
        .map((d) => d.trim())
        .filter((d) => d.length > 0),
      attribution: props.attribution,
      categories: props.categories,
      images: props.images,
      variants: props.variants,
      extras: props.extras,
      createdAt: props.createdAt,
    });
  }
}
