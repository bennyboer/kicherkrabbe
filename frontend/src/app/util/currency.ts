import { Option } from './option';

export class Currency {
  readonly name: string;
  readonly symbol: string;

  private constructor(props: { name: string; symbol: string }) {
    this.name = Option.someOrNone(props.name).orElseThrow(
      'Currency name is required',
    );
    this.symbol = Option.someOrNone(props.symbol).orElseThrow(
      'Currency short form is required',
    );
  }

  static of(props: { name: string; symbol: string }): Currency {
    return new Currency(props);
  }

  static euro(): Currency {
    return Currency.of({ name: 'EUR', symbol: '€' });
  }

  equalTo(currency: Currency): boolean {
    return this.name === currency.name;
  }

  differentFrom(currency: Currency): boolean {
    return !this.equalTo(currency);
  }
}
