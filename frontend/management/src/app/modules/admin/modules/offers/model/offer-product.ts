import { validateProps } from '@kicherkrabbe/shared';

export class OfferProduct {
  readonly id: string;
  readonly number: string;

  private constructor(props: { id: string; number: string }) {
    validateProps(props);

    this.id = props.id;
    this.number = props.number;
  }

  static of(props: { id: string; number: string }): OfferProduct {
    return new OfferProduct({
      id: props.id,
      number: props.number,
    });
  }
}
