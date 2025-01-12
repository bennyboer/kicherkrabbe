import { FabricCompositionItem } from './fabric-composition-item';
import { validateProps } from '../../../../../util';
import { FabricType } from './fabric-type';

export enum FabricCompositionValidationError {
  NO_ITEMS = 'NO_ITEMS',
  DUPLICATE_FABRIC_TYPES = 'DUPLICATE_FABRIC_TYPES',
  INVALID_PERCENTAGE_SUM = 'INVALID_PERCENTAGE_SUM',
}

export class FabricComposition {
  readonly items: FabricCompositionItem[];

  private constructor(props: { items: FabricCompositionItem[] }) {
    validateProps(props);

    this.items = props.items;
    FabricComposition.validateItems(this.items);
  }

  static of(props: { items: FabricCompositionItem[] }): FabricComposition {
    return new FabricComposition({
      items: props.items,
    });
  }

  getSortedByPercentage(): FabricCompositionItem[] {
    return [...this.items].sort((a, b) => b.percentage - a.percentage);
  }

  private static validateItems(items: FabricCompositionItem[]): void {
    if (items.length === 0) {
      throw new Error(FabricCompositionValidationError.NO_ITEMS);
    }

    const uniqueFabricTypes = new Set<FabricType>();
    for (const item of items) {
      if (uniqueFabricTypes.has(item.fabricType)) {
        throw new Error(FabricCompositionValidationError.DUPLICATE_FABRIC_TYPES);
      }

      uniqueFabricTypes.add(item.fabricType);
    }

    const sum = items.reduce((acc, item) => acc + item.percentage, 0);
    if (sum !== 100) {
      throw new Error(FabricCompositionValidationError.INVALID_PERCENTAGE_SUM);
    }
  }
}
