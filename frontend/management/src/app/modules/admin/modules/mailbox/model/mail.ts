import { Sender } from './sender';
import { validateProps } from '../../../../../util';
import { someOrNone } from '@kicherkrabbe/shared';
import { Origin } from './origin';

export class Mail {
  readonly id: string;
  readonly version: number;
  readonly origin: Origin;
  readonly sender: Sender;
  readonly subject: string;
  readonly content: string;
  readonly receivedAt: Date;
  readonly read: boolean;

  private constructor(props: {
    id: string;
    version: number;
    origin: Origin;
    sender: Sender;
    subject: string;
    content: string;
    receivedAt: Date;
    read: boolean;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.origin = props.origin;
    this.sender = props.sender;
    this.subject = props.subject;
    this.content = props.content;
    this.receivedAt = props.receivedAt;
    this.read = props.read;
  }

  static of(props: {
    id: string;
    version: number;
    origin: Origin;
    sender: Sender;
    subject: string;
    content?: string;
    receivedAt: Date;
    read?: boolean;
  }): Mail {
    return new Mail({
      id: props.id,
      version: props.version,
      origin: props.origin,
      sender: props.sender,
      subject: props.subject,
      content: someOrNone(props.content).orElse(''),
      receivedAt: props.receivedAt,
      read: someOrNone(props.read).orElse(false),
    });
  }

  markAsRead(version: number): Mail {
    return new Mail({
      ...this,
      version,
      read: true,
    });
  }

  markAsUnread(version: number): Mail {
    return new Mail({
      ...this,
      version,
      read: false,
    });
  }

  isRead(): boolean {
    return this.read;
  }

  isUnread(): boolean {
    return !this.isRead();
  }
}
