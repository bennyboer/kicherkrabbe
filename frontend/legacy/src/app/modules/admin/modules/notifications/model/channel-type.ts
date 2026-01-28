import { validateProps } from '../../../../../util';

export enum InternalChannelType {
  EMAIL = 'EMAIL',
  TELEGRAM = 'TELEGRAM',
}

export class ChannelType {
  readonly internal: InternalChannelType;
  readonly label: string;

  private constructor(props: { internal: InternalChannelType; label: string }) {
    validateProps(props);

    this.internal = props.internal;
    this.label = props.label;
  }

  static mail(): ChannelType {
    return new ChannelType({
      internal: InternalChannelType.EMAIL,
      label: 'E-Mail',
    });
  }

  static telegram(): ChannelType {
    return new ChannelType({
      internal: InternalChannelType.TELEGRAM,
      label: 'Telegram',
    });
  }
}

export const EMAIL = ChannelType.mail();
export const TELEGRAM = ChannelType.telegram();
export const CHANNEL_TYPES = [EMAIL, TELEGRAM];
