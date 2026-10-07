import { validateProps } from '@kicherkrabbe/shared';

export type EmbroideryId = string;
export type EmbroideryCategoryId = string;
export type ImageId = string;

export class EmbroideryStatus {
  readonly label: string;
  readonly color: string;

  private constructor(props: { label: string; color: string }) {
    this.label = props.label;
    this.color = props.color;
  }

  static readonly DRAFT = new EmbroideryStatus({ label: 'Entwurf', color: '#9e9e9e' });
  static readonly PUBLISHED = new EmbroideryStatus({ label: 'Veröffentlicht', color: '#4caf50' });
}

export class Embroidery {
  readonly id: EmbroideryId;
  readonly version: number;
  readonly published: boolean;
  readonly featured: boolean;
  readonly name: string;
  readonly image: ImageId;
  readonly categories: Set<EmbroideryCategoryId>;
  readonly createdAt: Date;

  private constructor(props: {
    id: EmbroideryId;
    version: number;
    published: boolean;
    featured: boolean;
    name: string;
    image: ImageId;
    categories: Set<EmbroideryCategoryId>;
    createdAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.published = props.published;
    this.featured = props.featured;
    this.name = props.name;
    this.image = props.image;
    this.categories = props.categories;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: EmbroideryId;
    version: number;
    published: boolean;
    featured: boolean;
    name: string;
    image: ImageId;
    categories: Set<EmbroideryCategoryId>;
    createdAt: Date;
  }): Embroidery {
    return new Embroidery({
      id: props.id,
      version: props.version,
      published: props.published,
      featured: props.featured,
      name: props.name,
      image: props.image,
      categories: props.categories,
      createdAt: props.createdAt,
    });
  }

  get status(): EmbroideryStatus {
    return this.published ? EmbroideryStatus.PUBLISHED : EmbroideryStatus.DRAFT;
  }

  rename(version: number, name: string): Embroidery {
    return new Embroidery({
      ...this,
      version,
      name,
    });
  }

  publish(version: number): Embroidery {
    return new Embroidery({
      ...this,
      version,
      published: true,
    });
  }

  unpublish(version: number): Embroidery {
    return new Embroidery({
      ...this,
      version,
      published: false,
    });
  }

  feature(version: number): Embroidery {
    return new Embroidery({
      ...this,
      version,
      featured: true,
    });
  }

  unfeature(version: number): Embroidery {
    return new Embroidery({
      ...this,
      version,
      featured: false,
    });
  }

  updateImage(version: number, image: ImageId): Embroidery {
    return new Embroidery({
      ...this,
      version,
      image,
    });
  }

  updateCategories(version: number, categories: Set<EmbroideryCategoryId>): Embroidery {
    return new Embroidery({
      ...this,
      version,
      categories,
    });
  }
}
