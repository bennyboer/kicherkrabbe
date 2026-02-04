import { validateProps } from '@kicherkrabbe/shared';

export enum InternalOriginType {
  MAIL = 'MAIL',
}

export class OriginType {
  readonly internal: InternalOriginType;

  private constructor(props: { internal: InternalOriginType }) {
    validateProps(props);

    this.internal = props.internal;
  }

  static mail(): OriginType {
    return new OriginType({
      internal: InternalOriginType.MAIL,
    });
  }
}

export const MAIL = OriginType.mail();
