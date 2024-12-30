import { validateProps } from '../../../../../util';
import { Option, someOrNone } from '../../../../shared/modules/option';

export class Sender {
  readonly name: string;
  readonly mail: string;
  readonly phone: Option<string>;

  private constructor(props: { name: string; mail: string; phone: Option<string> }) {
    validateProps(props);

    this.name = props.name;
    this.mail = props.mail;
    this.phone = props.phone;
  }

  static of(props: { name: string; mail: string; phone?: string | null }): Sender {
    return new Sender({
      name: props.name,
      mail: props.mail,
      phone: someOrNone(props.phone),
    });
  }
}
