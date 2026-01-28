export type FabricTypeId = string;

export class FabricType {
  readonly id: FabricTypeId;
  readonly name: string;

  private constructor(props: { id: FabricTypeId; name: string }) {
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { id: FabricTypeId; name: string }): FabricType {
    return new FabricType(props);
  }
}
