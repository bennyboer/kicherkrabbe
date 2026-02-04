import { Link } from './link';
import { FabricComposition } from './fabric-composition';
import { Notes } from './notes';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';
import { LinkType } from './link-type';

type ProductId = string;
type ImageId = string;
type ProductNumber = string;

export class Product {
  readonly id: ProductId;
  readonly version: number;
  readonly number: ProductNumber;
  readonly images: ImageId[];
  readonly links: Link[];
  readonly fabricComposition: FabricComposition;
  readonly notes: Notes;
  readonly producedAt: Date;
  readonly createdAt: Date;

  private constructor(props: {
    id: ProductId;
    version: number;
    number: ProductNumber;
    images: ImageId[];
    links: Link[];
    fabricComposition: FabricComposition;
    notes: Notes;
    producedAt: Date;
    createdAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.number = props.number;
    this.images = props.images;
    this.links = props.links;
    this.fabricComposition = props.fabricComposition;
    this.notes = props.notes;
    this.producedAt = props.producedAt;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: ProductId;
    version?: number;
    number: ProductNumber;
    images?: ImageId[];
    links?: Link[];
    fabricComposition: FabricComposition;
    notes?: Notes;
    producedAt: Date;
    createdAt: Date;
  }): Product {
    return new Product({
      id: props.id,
      version: someOrNone(props.version).orElse(0),
      number: props.number,
      images: someOrNone(props.images).orElse([]),
      links: someOrNone(props.links).orElse([]),
      fabricComposition: props.fabricComposition,
      notes: someOrNone(props.notes).orElse(Notes.empty()),
      producedAt: props.producedAt,
      createdAt: props.createdAt,
    });
  }

  addLink(version: number, link: Link): Product {
    const updatedLinks = [...this.links, link];
    updatedLinks.sort((a, b) => a.type.label.localeCompare(b.type.label, 'de-de', { numeric: true }));

    return new Product({
      ...this,
      version,
      links: updatedLinks,
    });
  }

  removeLink(version: number, type: LinkType, id: string): Product {
    return new Product({
      ...this,
      version,
      links: this.links.filter((link) => !(link.type === type && link.id === id)),
    });
  }

  updateFabricComposition(version: number, fabricComposition: FabricComposition): Product {
    return new Product({
      ...this,
      version,
      fabricComposition,
    });
  }

  updateNotes(version: number, notes: Notes): Product {
    return new Product({
      ...this,
      version,
      notes,
    });
  }

  updateProducedAt(version: number, date: Date): Product {
    return new Product({
      ...this,
      version,
      producedAt: date,
    });
  }

  updateImages(version: number, images: ImageId[]): Product {
    return new Product({
      ...this,
      version,
      images,
    });
  }
}
