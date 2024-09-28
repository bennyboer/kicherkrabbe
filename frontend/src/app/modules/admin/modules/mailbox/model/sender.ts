import { validateProps } from '../../../../../util';

export class Sender {
  readonly name: string;
  readonly mail: string;

  private constructor(props: { name: string; mail: string }) {
    validateProps(props);

    this.name = props.name;
    this.mail = props.mail;
  }

  static of(props: { name: string; mail: string }): Sender {
    return new Sender({
      name: props.name,
      mail: props.mail,
    });
  }
}
