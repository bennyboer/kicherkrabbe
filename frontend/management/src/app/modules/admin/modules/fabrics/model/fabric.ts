import { ImageId } from './image';
import { ColorId } from './color';
import { TopicId } from './topic';
import { FabricTypeAvailability } from './availability';
import { Option } from '@kicherkrabbe/shared';

export type FabricId = string;

export class Fabric {
  readonly id: FabricId;
  readonly version: number;
  readonly name: string;
  readonly kind: Option<string>;
  readonly image: Option<ImageId>;
  readonly exampleImages: ImageId[];
  readonly colors: Set<ColorId>;
  readonly topics: Set<TopicId>;
  readonly availability: FabricTypeAvailability[];
  readonly published: boolean;
  readonly featured: boolean;
  readonly createdAt: Date;

  private constructor(props: {
    id: FabricId;
    version: number;
    name: string;
    kind: Option<string>;
    image: Option<ImageId>;
    exampleImages: ImageId[];
    colors: Set<ColorId>;
    topics: Set<TopicId>;
    availability: FabricTypeAvailability[];
    published: boolean;
    featured: boolean;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.kind = props.kind;
    this.image = props.image;
    this.exampleImages = props.exampleImages;
    this.colors = props.colors;
    this.topics = props.topics;
    this.availability = props.availability;
    this.published = props.published;
    this.featured = props.featured;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: FabricId;
    version: number;
    name: string;
    kind: Option<string>;
    image: Option<ImageId>;
    exampleImages: ImageId[];
    colors: Set<ColorId>;
    topics: Set<TopicId>;
    availability: FabricTypeAvailability[];
    published: boolean;
    featured: boolean;
    createdAt: Date;
  }): Fabric {
    return new Fabric(props);
  }

  isPatterned(): boolean {
    return this.kind.map((k) => k === 'PATTERNED').orElse(true);
  }

  isSolidColor(): boolean {
    return this.kind.map((k) => k === 'SOLID_COLOR').orElse(false);
  }
}
