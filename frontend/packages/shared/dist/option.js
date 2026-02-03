export const some = (value) => Option.some(value);
export const someOrNone = (value) => Option.someOrNone(value);
export const none = () => Option.none();
export class Option {
    value;
    constructor(value) {
        this.value = value;
    }
    static some(value) {
        if (value === null || value === undefined) {
            throw new Error('Expected value to be non-null and non-undefined, but got null or undefined');
        }
        return new Option(value);
    }
    static none() {
        return new Option(null);
    }
    static someOrNone(value) {
        if (value === null || value === undefined) {
            return none();
        }
        return some(value);
    }
    isSome() {
        return this.value !== null;
    }
    isNone() {
        return !this.isSome();
    }
    map(mapper) {
        if (this.isNone()) {
            return none();
        }
        return someOrNone(mapper(this.orElseThrow()));
    }
    flatMap(mapper) {
        if (this.isNone()) {
            return none();
        }
        return mapper(this.orElseThrow());
    }
    filter(predicate) {
        if (this.isNone()) {
            return none();
        }
        if (predicate(this.orElseThrow())) {
            return some(this.orElseThrow());
        }
        return none();
    }
    ifSome(consumer) {
        if (this.isSome()) {
            consumer(this.orElseThrow());
        }
    }
    ifNone(consumer) {
        if (this.isNone()) {
            consumer();
        }
    }
    ifSomeOrElse(ifSomeFn, orElseFn) {
        if (this.isSome()) {
            ifSomeFn(this.orElseThrow());
        }
        else {
            orElseFn();
        }
    }
    orElse(other) {
        if (this.isNone()) {
            return other;
        }
        return this.orElseThrow();
    }
    orElseNull() {
        if (this.isNone()) {
            return null;
        }
        return this.orElseThrow();
    }
    orElseGet(other) {
        if (this.isNone()) {
            return other();
        }
        return this.orElseThrow();
    }
    orElseTry(supplier) {
        if (this.isNone()) {
            return supplier();
        }
        return this;
    }
    unwrap() {
        if (this.isNone()) {
            return null;
        }
        return this.orElseThrow();
    }
    orElseThrow(message) {
        if (this.isNone()) {
            throw new Error(some(message).orElse('Expected value to be non-null and non-undefined'));
        }
        return this.value;
    }
    or(option) {
        if (this.isSome()) {
            return this;
        }
        return option;
    }
    equals(option) {
        if (this.isNone() && option.isNone()) {
            return true;
        }
        if (this.isSome() && option.isSome()) {
            const a = this.orElseThrow();
            const b = option.orElseThrow();
            if (Option.isEqualsType(a) && Option.isEqualsType(b)) {
                return a.equals(b);
            }
            else {
                return a === b;
            }
        }
        return false;
    }
    static isEqualsType(value) {
        try {
            return 'equals' in value;
        }
        catch (e) {
            return false;
        }
    }
}
