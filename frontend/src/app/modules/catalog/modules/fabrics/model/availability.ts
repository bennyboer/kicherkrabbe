import { Type } from './type';

export class Availability {
  readonly types: TypeAvailability[];

  private constructor(props: { types: TypeAvailability[] }) {
    this.types = props.types;
  }

  static of(props: { types: TypeAvailability[] }): Availability {
    return new Availability({
      types: props.types,
    });
  }

  isAvailableInAnyType(): boolean {
    return this.types.some((typeAvailability) => typeAvailability.inStock);
  }
}

export class TypeAvailability {
  readonly type: Type;
  readonly inStock: boolean;

  private constructor(props: { type: Type; inStock: boolean }) {
    this.type = props.type;
    this.inStock = props.inStock;
  }

  static of(props: { type: Type; inStock: boolean }): TypeAvailability {
    return new TypeAvailability({
      type: props.type,
      inStock: props.inStock,
    });
  }
}
