import { FabricTypeId } from './fabric-type';

export class FabricTypeAvailability {
  readonly typeId: FabricTypeId;
  readonly inStock: boolean;

  private constructor(props: { typeId: FabricTypeId; inStock: boolean }) {
    this.typeId = props.typeId;
    this.inStock = props.inStock;
  }

  static of(props: { typeId: FabricTypeId; inStock: boolean }): FabricTypeAvailability {
    return new FabricTypeAvailability(props);
  }
}
