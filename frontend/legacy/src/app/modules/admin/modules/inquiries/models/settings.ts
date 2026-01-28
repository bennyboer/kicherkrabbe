import { RateLimits } from './rate-limits';
import { validateProps } from '../../../../../util';
import { someOrNone } from '../../../../shared/modules/option';

export class Settings {
  readonly enabled: boolean;
  readonly rateLimits: RateLimits;

  private constructor(props: { enabled: boolean; rateLimits: RateLimits }) {
    validateProps(props);

    this.enabled = props.enabled;
    this.rateLimits = props.rateLimits;
  }

  static of(props: { enabled?: boolean; rateLimits: RateLimits }): Settings {
    return new Settings({
      enabled: someOrNone(props.enabled).orElse(false),
      rateLimits: props.rateLimits,
    });
  }
}
