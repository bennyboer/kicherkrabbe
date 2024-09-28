import { Money, validateProps } from '../../../../../util';
import { someOrNone } from '../../../../shared/modules/option';

export class PatternExtra {
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
}
