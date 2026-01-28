import { InternalLinkType, LinkType } from './link-type';
import { validateProps } from '../../../../../util';

export class Link {
  readonly type: LinkType;
  readonly name: string;
  readonly id: string;

  private constructor(props: { type: LinkType; name: string; id: string }) {
    validateProps(props);

    this.type = props.type;
    this.name = props.name;
    this.id = props.id;
  }

  static of(props: { type: LinkType; name: string; id: string }): Link {
    return new Link({
      type: props.type,
      name: props.name,
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
