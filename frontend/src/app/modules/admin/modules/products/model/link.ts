import { LinkType } from './link-type';
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
}
