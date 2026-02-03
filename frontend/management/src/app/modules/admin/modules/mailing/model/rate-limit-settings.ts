import { validateProps } from '../../../../../util';
import { someOrNone } from '@kicherkrabbe/shared';

export class RateLimitSettings {
  readonly durationInMs: number;
  readonly limit: number;

  private constructor(props: { durationInMs: number; limit: number }) {
    validateProps(props);

    this.durationInMs = props.durationInMs;
    this.limit = props.limit;
  }

  static of(props: { durationInMs?: number; limit?: number }): RateLimitSettings {
    return new RateLimitSettings({
      durationInMs: someOrNone(props.durationInMs).orElse(24 * 60 * 60 * 1000),
      limit: someOrNone(props.limit).orElse(100),
    });
  }

  updateRateLimit(durationInMs: number, newLimit: number): RateLimitSettings {
    return new RateLimitSettings({
      durationInMs,
      limit: newLimit,
    });
  }
}
