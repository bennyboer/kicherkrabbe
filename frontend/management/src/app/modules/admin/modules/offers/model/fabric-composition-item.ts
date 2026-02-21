import { FabricType } from './fabric-type';
import { validateProps } from '@kicherkrabbe/shared';

export class FabricCompositionItem {
  readonly fabricType: FabricType;
  readonly percentage: number;

  private constructor(props: { fabricType: FabricType; percentage: number }) {
    validateProps(props);

    this.fabricType = props.fabricType;
    this.percentage = props.percentage;
  }

  static of(props: { fabricType: FabricType; percentage: number }): FabricCompositionItem {
    return new FabricCompositionItem({
      fabricType: props.fabricType,
      percentage: props.percentage,
    });
  }
}
