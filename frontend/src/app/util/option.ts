export const some = <T>(value: T | null | undefined) => Option.some(value);
export const someOrNone = <T>(value: T | null | undefined) =>
  Option.someOrNone(value);
export const none = <T>() => Option.none<T>();

export class Option<T> {
  private readonly value: T | null;

  private constructor(value: T | null) {
    this.value = value;
  }

  static some<T>(value: T | null | undefined): Option<T> {
    if (value === null || value === undefined) {
      throw new Error(
        'Expected value to be non-null and non-undefined, but got null or undefined',
      );
    }

    return new Option<T>(value);
  }

  static none<T>(): Option<T> {
    return new Option<T>(null);
  }

  static someOrNone<T>(value: T | null | undefined): Option<T> {
    if (value === null || value === undefined) {
      return none<T>();
    }

    return some(value);
  }

  isSome(): boolean {
    return this.value !== null;
  }

  isNone(): boolean {
    return !this.isSome();
  }

  map<U>(mapper: (value: T) => U): Option<U> {
    if (this.isNone()) {
      return none<U>();
    }

    return someOrNone(mapper(this.orElseThrow()));
  }

  flatMap<U>(mapper: (value: T) => Option<U>): Option<U> {
    if (this.isNone()) {
      return none<U>();
    }

    return mapper(this.orElseThrow());
  }

  filter(predicate: (value: T) => boolean): Option<T> {
    if (this.isNone()) {
      return none<T>();
    }

    if (predicate(this.orElseThrow())) {
      return some(this.orElseThrow());
    }

    return none<T>();
  }

  ifSome(consumer: (value: T) => void): void {
    if (this.isSome()) {
      consumer(this.orElseThrow());
    }
  }

  ifSomeOrElse(ifSomeFn: (value: T) => void, orElseFn: () => void) {
    if (this.isSome()) {
      ifSomeFn(this.orElseThrow());
    } else {
      orElseFn();
    }
  }

  orElse(other: T): T {
    if (this.isNone()) {
      return other;
    }

    return this.orElseThrow();
  }

  orElseGet(other: () => T): T {
    if (this.isNone()) {
      return other();
    }

    return this.orElseThrow();
  }

  orElseThrow(message?: string): T {
    if (this.isNone()) {
      throw new Error(
        some(message).orElse('Expected value to be non-null and non-undefined'),
      );
    }

    return this.value as T;
  }

  or(option: Option<T>): Option<T> {
    if (this.isSome()) {
      return this;
    }

    return option;
  }

  equals(option: Option<T>): boolean {
    if (this.isNone() && option.isNone()) {
      return true;
    }

    if (this.isSome() && option.isSome()) {
      return this.orElseThrow() === option.orElseThrow();
    }

    return false;
  }
}
