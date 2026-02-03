export declare const some: <T>(value: T | null | undefined) => Option<T>;
export declare const someOrNone: <T>(value: T | null | undefined) => Option<T>;
export declare const none: <T>() => Option<T>;
export declare class Option<T> {
    private readonly value;
    private constructor();
    static some<T>(value: T | null | undefined): Option<T>;
    static none<T>(): Option<T>;
    static someOrNone<T>(value: T | null | undefined): Option<T>;
    isSome(): boolean;
    isNone(): boolean;
    map<U>(mapper: (value: T) => U): Option<U>;
    flatMap<U>(mapper: (value: T) => Option<U>): Option<U>;
    filter(predicate: (value: T) => boolean): Option<T>;
    ifSome(consumer: (value: T) => void): void;
    ifNone(consumer: () => void): void;
    ifSomeOrElse(ifSomeFn: (value: T) => void, orElseFn: () => void): void;
    orElse(other: T): T;
    orElseNull(): T | null | undefined;
    orElseGet(other: () => T): T;
    orElseTry(supplier: () => Option<T>): Option<T>;
    unwrap(): T | null;
    orElseThrow(message?: string): T;
    or(option: Option<T>): Option<T>;
    equals(option: Option<T>): boolean;
    private static isEqualsType;
}
//# sourceMappingURL=option.d.ts.map