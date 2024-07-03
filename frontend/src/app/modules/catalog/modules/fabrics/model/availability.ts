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
  readonly typeId: string;
  readonly inStock: boolean;

  private constructor(props: { typeId: string; inStock: boolean }) {
    this.typeId = props.typeId;
    this.inStock = props.inStock;
  }

  static of(props: { typeId: string; inStock: boolean }): TypeAvailability {
    return new TypeAvailability({
      typeId: props.typeId,
      inStock: props.inStock,
    });
  }
}
