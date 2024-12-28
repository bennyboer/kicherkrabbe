import { Option } from '../../../../shared/modules/option';
import { Telegram } from './telegram';
import { ChannelType, EMAIL, TELEGRAM } from './channel-type';
import { validateProps } from '../../../../../util';

export class Channel {
  readonly type: ChannelType;
  readonly mail: Option<string>;
  readonly telegram: Option<Telegram>;

  private constructor(props: {
    type: ChannelType;
    mail: Option<string>;
    telegram: Option<Telegram>;
  }) {
    validateProps(props);

    this.type = props.type;
    this.mail = props.mail;
    this.telegram = props.telegram;
  }

  static mail(mail: string): Channel {
    return new Channel({
      type: EMAIL,
      mail: Option.some(mail),
      telegram: Option.none(),
    });
  }

  static telegram(telegram: Telegram): Channel {
    return new Channel({
      type: TELEGRAM,
      mail: Option.none(),
      telegram: Option.some(telegram),
    });
  }
}
