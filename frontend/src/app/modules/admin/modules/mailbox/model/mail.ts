import { Sender } from './sender';
import { validateProps } from '../../../../../util';
import { someOrNone } from '../../../../shared/modules/option';

export class Mail {
  readonly id: string;
  readonly subject: string;
  readonly content: string;
  readonly sender: Sender;
  readonly receivedAt: Date;
  readonly read: boolean;

  private constructor(props: {
    id: string;
    subject: string;
    content: string;
    sender: Sender;
    receivedAt: Date;
    read: boolean;
  }) {
    validateProps(props);

    this.id = props.id;
    this.subject = props.subject;
    this.content = props.content;
    this.sender = props.sender;
    this.receivedAt = props.receivedAt;
    this.read = props.read;
  }

  static of(props: {
    id: string;
    subject: string;
    content?: string;
    sender: Sender;
    receivedAt: Date;
    read?: boolean;
  }): Mail {
    return new Mail({
      id: props.id,
      subject: props.subject,
      content: someOrNone(props.content).orElse(''),
      sender: props.sender,
      receivedAt: props.receivedAt,
      read: someOrNone(props.read).orElse(false),
    });
  }

  isUnread(): boolean {
    return !this.read;
  }
}
