import { validateProps } from '@kicherkrabbe/shared';
import { RateLimitSettings } from './rate-limit-settings';
import { MailgunSettings } from './mailgun-settings';

export class Settings {
  readonly version: number;
  readonly rateLimit: RateLimitSettings;
  readonly mailgun: MailgunSettings;

  private constructor(props: { version: number; rateLimit: RateLimitSettings; mailgun: MailgunSettings }) {
    validateProps(props);

    this.version = props.version;
    this.rateLimit = props.rateLimit;
    this.mailgun = props.mailgun;
  }

  static of(props: { version: number; rateLimit: RateLimitSettings; mailgun: MailgunSettings }): Settings {
    return new Settings({
      version: props.version,
      rateLimit: props.rateLimit,
      mailgun: props.mailgun,
    });
  }

  updateMailgunApiToken(version: number, apiToken: string): Settings {
    return new Settings({
      ...this,
      version,
      mailgun: this.mailgun.updateApiToken(apiToken),
    });
  }

  clearMailgunApiToken(version: number): Settings {
    return new Settings({
      ...this,
      version,
      mailgun: this.mailgun.clearApiToken(),
    });
  }

  updateRateLimit(version: number, durationInMs: number, newLimit: number): Settings {
    return new Settings({
      ...this,
      version,
      rateLimit: this.rateLimit.updateRateLimit(durationInMs, newLimit),
    });
  }
}
