import { validateProps } from '@kicherkrabbe/shared';

export class Money {
  readonly amount: number;
  readonly currency: string;

  private constructor(props: { amount: number; currency: string }) {
    validateProps(props);

    this.amount = props.amount;
    this.currency = props.currency;
  }

  static of(props: { amount: number; currency: string }): Money {
    return new Money({
      amount: props.amount,
      currency: props.currency,
    });
  }

  static euro(amountInCents: number): Money {
    return new Money({
      amount: amountInCents,
      currency: 'EUR',
    });
  }

  toDisplayString(): string {
    const euros = (this.amount / 100).toFixed(2).replace('.', ',');
    return `${euros} \u20AC`;
  }
}
