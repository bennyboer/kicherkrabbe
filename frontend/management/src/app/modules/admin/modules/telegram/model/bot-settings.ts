import { none, Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class BotSettings {
  readonly apiToken: Option<string>;

  private constructor(props: { apiToken: Option<string> }) {
    validateProps(props);

    this.apiToken = props.apiToken;
  }

  static of(props: { apiToken?: string }): BotSettings {
    return new BotSettings({
      apiToken: someOrNone(props.apiToken),
    });
  }

  updateApiToken(newToken: string): BotSettings {
    return new BotSettings({
      ...this,
      apiToken: someOrNone(newToken),
    });
  }

  clearApiToken(): BotSettings {
    return new BotSettings({
      ...this,
      apiToken: none(),
    });
  }
}
