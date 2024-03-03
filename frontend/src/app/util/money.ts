import { Currency } from './currency';
import { Option } from './option';

export class Money {
  readonly value: number;
  readonly currency: Currency;

  private constructor(props: { value: number; currency: Currency }) {
    this.value = Option.someOrNone(props.value).orElse(0);
    this.currency = Option.someOrNone(props.currency).orElseThrow(
      'Currency is required',
    );
  }

  static of(props: { value: number; currency: Currency }): Money {
    return new Money(props);
  }

  static euro(value: number): Money {
    return Money.of({ value, currency: Currency.euro() });
  }

  static zero(): Money {
    return Money.of({ value: 0, currency: Currency.euro() });
  }

  add(money: Money): Money {
    if (this.currency.differentFrom(money.currency)) {
      throw new Error('Currencies must match');
    }

    return new Money({
      ...this,
      value: this.value + money.value,
    });
  }

  subtract(money: Money): Money {
    if (this.currency.differentFrom(money.currency)) {
      throw new Error('Currencies must match');
    }

    return new Money({
      ...this,
      value: this.value - money.value,
    });
  }

  multiply(factor: number): Money {
    return new Money({
      ...this,
      value: this.value * factor,
    });
  }

  divide(divisor: number): Money {
    if (divisor === 0) {
      throw new Error('Divisor must not be 0');
    }

    return new Money({
      ...this,
      value: this.value / divisor,
    });
  }

  isZero(): boolean {
    return this.value === 0;
  }

  isPositive(): boolean {
    return this.value >= 0;
  }

  isNegative(): boolean {
    return !this.isPositive();
  }

  isEqualTo(other: Money): boolean {
    if (this.currency.differentFrom(other.currency)) {
      throw new Error('Currencies must match');
    }

    return this.value === other.value;
  }

  isLessThan(other: Money): boolean {
    if (this.currency.differentFrom(other.currency)) {
      throw new Error('Currencies must match');
    }

    return this.value < other.value;
  }

  isGreaterThan(other: Money): boolean {
    if (this.currency.differentFrom(other.currency)) {
      throw new Error('Currencies must match');
    }

    return this.value > other.value;
  }

  isLessThanOrEqualTo(other: Money): boolean {
    return this.isLessThan(other) || this.isEqualTo(other);
  }

  isGreaterThanOrEqualTo(other: Money): boolean {
    return this.isGreaterThan(other) || this.isEqualTo(other);
  }

  toNatural(): number {
    return this.value / 100;
  }

  formatted(): string {
    return `${this.toNatural().toFixed(2).replace('.', ',')} ${this.currency.symbol}`;
  }
}
