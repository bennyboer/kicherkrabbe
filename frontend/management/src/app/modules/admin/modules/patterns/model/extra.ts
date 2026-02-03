import { Eq, Money, validateProps } from '../../../../../util';
import { someOrNone } from '@kicherkrabbe/shared';

export class PatternExtra implements Eq<PatternExtra> {
  readonly name: string;
  readonly price: Money;

  private constructor(props: { name: string; price: Money }) {
    validateProps(props);

    this.name = props.name;
    this.price = props.price;
  }

  static of(props: { name: string; price?: Money }): PatternExtra {
    return new PatternExtra({
      name: props.name,
      price: someOrNone(props.price).orElse(Money.zero()),
    });
  }

  equals(other: PatternExtra): boolean {
    return this.name === other.name && this.price.equals(other.price);
  }
}
