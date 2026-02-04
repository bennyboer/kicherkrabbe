import { validateProps } from '@kicherkrabbe/shared';

export class DateRange {
  readonly from: Date;
  readonly to: Date;

  private constructor(props: { from: Date; to: Date }) {
    validateProps(props);

    this.from = props.from;
    this.to = props.to;
  }

  static of(props: { from: Date; to: Date }): DateRange {
    return new DateRange(props);
  }
}
