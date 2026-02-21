import { FabricCompositionItem } from './fabric-composition-item';
import { validateProps } from '@kicherkrabbe/shared';

export class FabricComposition {
  readonly items: FabricCompositionItem[];

  private constructor(props: { items: FabricCompositionItem[] }) {
    validateProps(props);

    this.items = props.items;
  }

  static of(props: { items: FabricCompositionItem[] }): FabricComposition {
    return new FabricComposition({
      items: props.items,
    });
  }

  getSortedByPercentage(): FabricCompositionItem[] {
    return [...this.items].sort((a, b) => b.percentage - a.percentage);
  }
}
