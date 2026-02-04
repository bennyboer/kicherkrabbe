import { Channel } from './channel';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';

export class ActivatableChannel {
  readonly active: boolean;
  readonly channel: Channel;

  private constructor(props: { active: boolean; channel: Channel }) {
    validateProps(props);

    this.active = props.active;
    this.channel = props.channel;
  }

  static of(props: { active?: boolean; channel: Channel }): ActivatableChannel {
    return new ActivatableChannel({
      active: someOrNone(props.active).orElse(false),
      channel: props.channel,
    });
  }
}
