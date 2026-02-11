import { someOrNone, validateProps } from '@kicherkrabbe/shared';
import { Link } from './link';
import { LinkType } from './link-type';

export type HighlightId = string;

export class Highlight {
  readonly id: HighlightId;
  readonly version: number;
  readonly imageId: string;
  readonly links: Link[];
  readonly published: boolean;
  readonly sortOrder: number;
  readonly createdAt: Date;

  private constructor(props: {
    id: HighlightId;
    version: number;
    imageId: string;
    links: Link[];
    published: boolean;
    sortOrder: number;
    createdAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.imageId = props.imageId;
    this.links = props.links;
    this.published = props.published;
    this.sortOrder = props.sortOrder;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: HighlightId;
    version?: number;
    imageId: string;
    links: Link[];
    published: boolean;
    sortOrder: number;
    createdAt: Date;
  }): Highlight {
    return new Highlight({
      ...props,
      version: someOrNone(props.version).orElse(0),
    });
  }

  togglePublished(version: number): Highlight {
    return new Highlight({
      ...this,
      version,
      published: !this.published,
    });
  }

  updateImage(version: number, imageId: string): Highlight {
    return new Highlight({
      ...this,
      version,
      imageId,
    });
  }

  updateSortOrder(version: number, sortOrder: number): Highlight {
    return new Highlight({
      ...this,
      version,
      sortOrder,
    });
  }

  addLink(version: number, link: Link): Highlight {
    return new Highlight({
      ...this,
      version,
      links: [...this.links, link],
    });
  }

  removeLink(version: number, type: LinkType, id: string): Highlight {
    return new Highlight({
      ...this,
      version,
      links: this.links.filter((link) => !(link.type === type && link.id === id)),
    });
  }
}
