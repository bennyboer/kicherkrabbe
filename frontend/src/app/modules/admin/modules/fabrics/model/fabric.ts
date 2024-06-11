import { ImageId } from './image';
import { ColorId } from './color';
import { TopicId } from './topic';
import { FabricTypeAvailability } from './availability';

export type FabricId = string;

export class Fabric {
  readonly id: FabricId;
  readonly version: number;
  readonly name: string;
  readonly image: ImageId;
  readonly colors: Set<ColorId>;
  readonly topics: Set<TopicId>;
  readonly availability: FabricTypeAvailability[];
  readonly published: boolean;
  readonly createdAt: Date;

  private constructor(props: {
    id: FabricId;
    version: number;
    name: string;
    image: ImageId;
    colors: Set<ColorId>;
    topics: Set<TopicId>;
    availability: FabricTypeAvailability[];
    published: boolean;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.image = props.image;
    this.colors = props.colors;
    this.topics = props.topics;
    this.availability = props.availability;
    this.published = props.published;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: FabricId;
    version: number;
    name: string;
    image: ImageId;
    colors: Set<ColorId>;
    topics: Set<TopicId>;
    availability: FabricTypeAvailability[];
    published: boolean;
    createdAt: Date;
  }): Fabric {
    return new Fabric(props);
  }
}
