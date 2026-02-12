import { validateProps } from '@kicherkrabbe/shared';
import { LinkType } from './link-type';

export class Link {
  readonly type: LinkType;
  readonly id: string;
  readonly name: string;

  private constructor(props: { type: LinkType; id: string; name: string }) {
    validateProps(props);

    this.type = props.type;
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { type: LinkType; id: string; name: string }): Link {
    return new Link(props);
  }
}
