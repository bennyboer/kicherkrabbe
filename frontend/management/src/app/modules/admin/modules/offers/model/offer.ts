import { Link } from './link';
import { FabricComposition } from './fabric-composition';
import { Notes } from './notes';
import { Pricing } from './pricing';
import { OfferProduct } from './offer-product';
import { Option, someOrNone, none, validateProps } from '@kicherkrabbe/shared';

type OfferId = string;
type ImageId = string;

export class OfferStatus {
  readonly label: string;
  readonly color: string;

  private constructor(props: { label: string; color: string }) {
    this.label = props.label;
    this.color = props.color;
  }

  static readonly DRAFT = new OfferStatus({ label: 'Entwurf', color: '#9e9e9e' });
  static readonly PUBLISHED = new OfferStatus({ label: 'Veröffentlicht', color: '#4caf50' });
  static readonly RESERVED = new OfferStatus({ label: 'Reserviert', color: '#ff9800' });
  static readonly ARCHIVED = new OfferStatus({ label: 'Archiviert', color: '#757575' });
}

export class Offer {
  readonly id: OfferId;
  readonly version: number;
  readonly product: OfferProduct;
  readonly images: ImageId[];
  readonly links: Link[];
  readonly fabricComposition: FabricComposition;
  readonly pricing: Pricing;
  readonly notes: Notes;
  readonly published: boolean;
  readonly reserved: boolean;
  readonly createdAt: Date;
  readonly archivedAt: Option<Date>;

  private constructor(props: {
    id: OfferId;
    version: number;
    product: OfferProduct;
    images: ImageId[];
    links: Link[];
    fabricComposition: FabricComposition;
    pricing: Pricing;
    notes: Notes;
    published: boolean;
    reserved: boolean;
    createdAt: Date;
    archivedAt: Option<Date>;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.product = props.product;
    this.images = props.images;
    this.links = props.links;
    this.fabricComposition = props.fabricComposition;
    this.pricing = props.pricing;
    this.notes = props.notes;
    this.published = props.published;
    this.reserved = props.reserved;
    this.createdAt = props.createdAt;
    this.archivedAt = props.archivedAt;
  }

  static of(props: {
    id: OfferId;
    version?: number;
    product: OfferProduct;
    images?: ImageId[];
    links?: Link[];
    fabricComposition: FabricComposition;
    pricing: Pricing;
    notes?: Notes;
    published?: boolean;
    reserved?: boolean;
    createdAt: Date;
    archivedAt?: Date | null;
  }): Offer {
    return new Offer({
      id: props.id,
      version: someOrNone(props.version).orElse(0),
      product: props.product,
      images: someOrNone(props.images).orElse([]),
      links: someOrNone(props.links).orElse([]),
      fabricComposition: props.fabricComposition,
      pricing: props.pricing,
      notes: someOrNone(props.notes).orElse(Notes.empty()),
      published: someOrNone(props.published).orElse(false),
      reserved: someOrNone(props.reserved).orElse(false),
      createdAt: props.createdAt,
      archivedAt: someOrNone(props.archivedAt),
    });
  }

  get status(): OfferStatus {
    if (this.archivedAt.isSome()) {
      return OfferStatus.ARCHIVED;
    }
    if (this.reserved) {
      return OfferStatus.RESERVED;
    }
    if (this.published) {
      return OfferStatus.PUBLISHED;
    }
    return OfferStatus.DRAFT;
  }

  updateImages(version: number, images: ImageId[]): Offer {
    return new Offer({
      ...this,
      version,
      images,
    });
  }

  updateNotes(version: number, notes: Notes): Offer {
    return new Offer({
      ...this,
      version,
      notes,
    });
  }

  updatePricing(version: number, pricing: Pricing): Offer {
    return new Offer({
      ...this,
      version,
      pricing,
    });
  }

  publish(version: number): Offer {
    return new Offer({
      ...this,
      version,
      published: true,
    });
  }

  unpublish(version: number): Offer {
    return new Offer({
      ...this,
      version,
      published: false,
    });
  }

  reserve(version: number): Offer {
    return new Offer({
      ...this,
      version,
      reserved: true,
    });
  }

  unreserve(version: number): Offer {
    return new Offer({
      ...this,
      version,
      reserved: false,
    });
  }
}
