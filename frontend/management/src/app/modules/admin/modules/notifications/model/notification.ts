import { Origin } from './origin';
import { Target } from './target';
import { Channel } from './channel';
import { validateProps } from '../../../../../util';
import { someOrNone } from '@kicherkrabbe/shared';

export class Notification {
  readonly id: string;
  readonly version: number;
  readonly origin: Origin;
  readonly target: Target;
  readonly channels: Channel[];
  readonly title: string;
  readonly message: string;
  readonly sentAt: Date;

  private constructor(props: {
    id: string;
    version: number;
    origin: Origin;
    target: Target;
    channels: Channel[];
    title: string;
    message: string;
    sentAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.origin = props.origin;
    this.target = props.target;
    this.channels = props.channels;
    this.title = props.title;
    this.message = props.message;
    this.sentAt = props.sentAt;
  }

  static of(props: {
    id: string;
    version?: number;
    origin: Origin;
    target: Target;
    channels: Channel[];
    title: string;
    message: string;
    sentAt: Date;
  }): Notification {
    return new Notification({
      id: props.id,
      version: someOrNone(props.version).orElse(0),
      origin: props.origin,
      target: props.target,
      channels: props.channels,
      title: props.title,
      message: props.message,
      sentAt: props.sentAt,
    });
  }
}
