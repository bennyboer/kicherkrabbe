import { validateProps } from '@kicherkrabbe/shared';

export class RateLimit {
  readonly maxRequests: number;
  readonly durationInMillis: number;

  private constructor(props: { maxRequests: number; durationInMillis: number }) {
    validateProps(props);

    this.maxRequests = props.maxRequests;
    this.durationInMillis = props.durationInMillis;
  }

  static fullDay(maxRequests: number): RateLimit {
    return new RateLimit({
      maxRequests,
      durationInMillis: 24 * 60 * 60 * 1000,
    });
  }
}
