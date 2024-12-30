import { RateLimit } from './rate-limit';
import { validateProps } from '../../../../../util';

export class RateLimits {
  readonly perMail: RateLimit;
  readonly perIp: RateLimit;
  readonly overall: RateLimit;

  private constructor(props: { perMail: RateLimit; perIp: RateLimit; overall: RateLimit }) {
    validateProps(props);

    this.perMail = props.perMail;
    this.perIp = props.perIp;
    this.overall = props.overall;
  }

  static of(props: { perMail: RateLimit; perIp: RateLimit; overall: RateLimit }): RateLimits {
    return new RateLimits(props);
  }
}
