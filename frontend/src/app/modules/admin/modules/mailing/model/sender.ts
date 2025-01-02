import { validateProps } from '../../../../../util';

export class Sender {
  readonly mail: string;

  private constructor(props: { mail: string }) {
    validateProps(props);

    this.mail = props.mail;
  }

  static of(props: { mail: string }): Sender {
    return new Sender({
      mail: props.mail,
    });
  }
}
