import { validateProps } from '@kicherkrabbe/shared';

export class Receiver {
  readonly mail: string;

  private constructor(props: { mail: string }) {
    validateProps(props);

    this.mail = props.mail;
  }

  static of(props: { mail: string }): Receiver {
    return new Receiver({
      mail: props.mail,
    });
  }
}
