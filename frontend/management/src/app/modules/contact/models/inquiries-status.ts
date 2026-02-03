import { validateProps } from '../../../util';
import { someOrNone } from '@kicherkrabbe/shared';

export class InquiriesStatus {
  readonly enabled: boolean;

  private constructor(props: { enabled: boolean }) {
    validateProps(props);

    this.enabled = props.enabled;
  }

  static of(props: { enabled?: boolean }): InquiriesStatus {
    return new InquiriesStatus({
      enabled: someOrNone(props.enabled).orElse(false),
    });
  }
}
