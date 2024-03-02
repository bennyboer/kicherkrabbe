import { Money, Option } from '../../../../../util';

export class PatternExtra {
  readonly name: string;
  readonly price: Money;

  private constructor(props: { name: string; price: Money }) {
    this.name = Option.someOrNone(props.name).orElseThrow(
      'Extra name is required',
    );
    this.price = Option.someOrNone(props.price).orElseThrow(
      'Extra price is required',
    );
  }

  static of(props: { name: string; price: Money }): PatternExtra {
    return new PatternExtra({
      name: props.name,
      price: props.price,
    });
  }
}
