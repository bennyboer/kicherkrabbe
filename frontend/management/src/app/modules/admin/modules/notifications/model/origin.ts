import { OriginType } from './origin-type';
import { validateProps } from '@kicherkrabbe/shared';

export class Origin {
  readonly type: OriginType;
  readonly id: string;

  private constructor(props: { type: OriginType; id: string }) {
    validateProps(props);

    this.type = props.type;
    this.id = props.id;
  }

  static of(props: { type: OriginType; id: string }): Origin {
    return new Origin({
      type: props.type,
      id: props.id,
    });
  }
}
