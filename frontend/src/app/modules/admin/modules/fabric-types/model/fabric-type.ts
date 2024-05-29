export class FabricType {
  readonly id: string;
  readonly version: number;
  readonly name: string;
  readonly createdAt: Date;

  private constructor(props: {
    id: string;
    version: number;
    name: string;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: string;
    version: number;
    name: string;
    createdAt: Date;
  }): FabricType {
    return new FabricType({
      id: props.id,
      version: props.version,
      name: props.name,
      createdAt: props.createdAt,
    });
  }
}
