import { InternalLinkType, LinkType } from './link-type';
import { validateProps } from '../../../../../util';

export class Link {
  readonly type: LinkType;
  readonly id: string;

  private constructor(props: { type: LinkType; id: string }) {
    validateProps(props);

    this.type = props.type;
    this.id = props.id;
  }

  static of(props: { type: LinkType; id: string }): Link {
    return new Link({
      type: props.type,
      id: props.id,
    });
  }

  toHref(): string {
    switch (this.type.internal) {
      case InternalLinkType.PATTERN:
        return `/admin/patterns/${this.id}`;
      case InternalLinkType.FABRIC:
        return `/admin/fabrics/${this.id}`;
      default:
        throw new Error(`Unknown link type: ${this.type.internal}`);
    }
  }
}
