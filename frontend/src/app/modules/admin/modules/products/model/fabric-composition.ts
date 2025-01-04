import { FabricCompositionItem } from './fabric-composition-item';
import { validateProps } from '../../../../../util';

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
      throw new Error('Fabric composition must contain at least one item');
    }

    const sum = items.reduce((acc, item) => acc + item.percentage, 0);
    if (sum !== 100) {
      throw new Error('Sum of fabric composition item percentages must be 100');
    }
  }
}
