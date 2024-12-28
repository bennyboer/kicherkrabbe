import { validateProps } from '../../../../../util';

export class Telegram {
  readonly chatId: string;

  private constructor(props: { chatId: string }) {
    validateProps(props);

    this.chatId = props.chatId;
  }

  static of(props: { chatId: string }): Telegram {
    return new Telegram({
      chatId: props.chatId,
    });
  }
}
