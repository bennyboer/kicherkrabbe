import { someOrNone, validateProps } from '@kicherkrabbe/shared';
import { DateRange } from './date-range';

export class Statistics {
  readonly dateRange: DateRange;
  readonly totalRequests: number;

  private constructor(props: { dateRange: DateRange; totalRequests: number }) {
    validateProps(props);

    this.dateRange = props.dateRange;
    this.totalRequests = props.totalRequests;
  }

  static of(props: { dateRange: DateRange; totalRequests?: number }): Statistics {
    return new Statistics({
      ...props,
      totalRequests: someOrNone(props.totalRequests).orElse(0),
    });
  }
}
