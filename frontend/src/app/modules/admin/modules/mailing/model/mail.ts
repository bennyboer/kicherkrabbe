import { Sender } from './sender';
import { Receiver } from './receiver';
import { validateProps } from '../../../../../util';

export class Mail {
  readonly id: string;
  readonly version: number;
  readonly sender: Sender;
  readonly receivers: Receiver[];
  readonly subject: string;
  readonly text: string;
  readonly sentAt: Date;

  private constructor(props: {
    id: string;
    version: number;
    sender: Sender;
    receivers: Receiver[];
    subject: string;
    text: string;
    sentAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.sender = props.sender;
    this.receivers = props.receivers;
    this.subject = props.subject;
    this.text = props.text;
    this.sentAt = props.sentAt;
  }

  static of(props: {
    id: string;
    version: number;
    sender: Sender;
    receivers: Receiver[];
    subject: string;
    text: string;
    sentAt: Date;
  }): Mail {
    return new Mail({
      id: props.id,
      version: props.version,
      sender: props.sender,
      receivers: props.receivers,
      subject: props.subject,
      text: props.text,
      sentAt: props.sentAt,
    });
  }

  getCommaSeparatedReceiverMails(): string {
    return this.receivers.map((receiver) => receiver.mail).join(', ');
  }
}
