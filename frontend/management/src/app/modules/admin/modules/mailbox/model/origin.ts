import { validateProps } from '@kicherkrabbe/shared';

export enum InternalOriginType {
  INQUIRY = 'INQUIRY',
}

export class OriginType {
  readonly internal: InternalOriginType;
  readonly label: string;

  private constructor(props: { internal: InternalOriginType; label: string }) {
    validateProps(props);

    this.internal = props.internal;
    this.label = props.label;
  }

  static inquiry(): OriginType {
    return new OriginType({
      internal: InternalOriginType.INQUIRY,
      label: 'Kontaktanfrage',
    });
  }
}

export const INQUIRY = OriginType.inquiry();

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
