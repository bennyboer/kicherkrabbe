import { Eq } from './equals';
import { someOrNone } from '@kicherkrabbe/shared';

export class Currency implements Eq<Currency> {
  readonly name: string;
  readonly symbol: string;

  private constructor(props: { name: string; symbol: string }) {
    this.name = someOrNone(props.name).orElseThrow('Currency name is required');
    this.symbol = someOrNone(props.symbol).orElseThrow('Currency short form is required');
  }

  static of(props: { name: string; symbol: string }): Currency {
    return new Currency(props);
  }

  static euro(): Currency {
    return Currency.of({ name: 'EUR', symbol: '€' });
  }

  differentFrom(currency: Currency): boolean {
    return !this.equals(currency);
  }

  equals(other: Currency): boolean {
    return this.name === other.name;
  }
}
