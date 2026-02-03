import { validateProps } from '../../../../../util';
import { none, Option, someOrNone } from '@kicherkrabbe/shared';

export class MailgunSettings {
  readonly apiToken: Option<string>;

  private constructor(props: { apiToken: Option<string> }) {
    validateProps(props);

    this.apiToken = props.apiToken;
  }

  static of(props: { apiToken?: string }): MailgunSettings {
    return new MailgunSettings({
      apiToken: someOrNone(props.apiToken),
    });
  }

  updateApiToken(apiToken: string): MailgunSettings {
    return new MailgunSettings({
      apiToken: someOrNone(apiToken),
    });
  }

  clearApiToken(): MailgunSettings {
    return new MailgunSettings({
      apiToken: none(),
    });
  }
}
